from abc import ABC, abstractmethod
import asyncio
from dataclasses import dataclass
from datetime import datetime, timedelta
import os
from pathlib import Path
from typing import Any, Optional
import aio_pika
import json
import logging
import random
import time
import uuid

# Try to load .env file if python-dotenv is available
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

HOURS_IN_A_DAY = 24.0
READINGS_PER_HOUR = 6.0
RABBIT_USER = os.getenv("RABBIT_USER", default="guest")
RABBIT_PASSWORD = os.getenv("RABBIT_PASSWORD", default="guest")
RABBIT_HOST = os.getenv("RABBIT_HOST", default="localhost")
RABBIT_PORT = os.getenv("RABBIT_PORT", default="5672")
TRACE_FILE = Path(".trace.json")

def load_trace() -> dict:
    if not TRACE_FILE.exists():
        return {}
    try:
        with TRACE_FILE.open("r", encoding="utf-8") as f:
            return json.load(f)
    except Exception:
        return {}

def save_trace(device_id: uuid.UUID, end_time: datetime) -> None:
    state = load_trace()
    state[str(device_id)] = end_time.isoformat()
    tmp = TRACE_FILE.with_suffix(".tmp")
    with tmp.open("w", encoding="utf-8") as f:
        json.dump(state, f, ensure_ascii=False, indent=2)
    tmp.replace(TRACE_FILE)


def is_day(time: datetime) -> bool:
    return (time.hour >= 8) and (time.hour < 20)


@dataclass
class Measurement:
    device_id: uuid.UUID
    measurement_value: float
    timestamp: datetime

    def to_json(self) -> dict[str, Any]:
        return {
            "device_id": str(self.device_id),
            "measurement_value": self.measurement_value,
            "timestamp": self.timestamp.isoformat(),
        }


@dataclass
class SensorConfig:
    device_id: uuid.UUID
    simulated_day_duration: int
    simulated_day_count: int
    max_consumption: float

    @property
    def simulated_generation_interval(self) -> float:
        return self.simulated_day_duration / HOURS_IN_A_DAY / READINGS_PER_HOUR

    @property
    def total_readings(self) -> int:
        return int(self.simulated_day_count * HOURS_IN_A_DAY * READINGS_PER_HOUR)

    @staticmethod
    def from_config_file(file_path: str) -> "SensorConfig":
        with open(file_path, "r") as f:
            conf = json.load(f)
            return SensorConfig(
                device_id=uuid.UUID(conf.get("device_id")),
                simulated_day_duration=int(conf.get("simulated_day_duration")),
                simulated_day_count=int(conf.get("simulated_day_count")),
                max_consumption=float(conf.get("max_consumption")),
            )


# Value generation using callable functions without ABC
def day_time_generator(limit: float) -> float:
    """Generate a value during daytime (closer to max consumption)."""
    low = limit - 5
    high = limit + 5
    return round(random.uniform(low, high), 2)


def night_time_generator(limit: float) -> float:
    """Generate a value during nighttime (lower consumption)."""
    low = limit - 15
    high = limit - 5
    return round(random.uniform(low, high), 2)


class ValueGenerator:
    """Value generator using a dictionary-based approach instead of ABC."""
    
    _generators = {
        "day": day_time_generator,
        "night": night_time_generator,
    }
    
    @staticmethod
    def get_by_time(time: datetime, limit: float) -> float:
        """Get generated value based on time of day."""
        period = "day" if is_day(time) else "night"
        generator = ValueGenerator._generators[period]
        return generator(limit)


class MeasurementProcessor(ABC):

    @abstractmethod
    async def process(self, measurement: Measurement) -> None:
        pass


class RabbitMeasurementProcessor(MeasurementProcessor):

    def __init__(
        self,
        user: str = RABBIT_USER,
        passwd: str = RABBIT_PASSWORD,
        host: str = RABBIT_HOST,
        port: str = RABBIT_PORT,
    ) -> None:
        self._url = f"amqp://{user}:{passwd}@{host}:{port}/"
        self._connection = None
        self._channel = None
        self._exchange_name = "device.data.exchange"
        self._routing_key = "device.data.routing.key"

    async def __aenter__(self):
        self._connection = await aio_pika.connect_robust(self._url)
        self._channel = await self._connection.channel()

        # Declare exchange
        self._exchange = await self._channel.declare_exchange(
            self._exchange_name,
            aio_pika.ExchangeType.TOPIC,
            durable=True
        )

        return self

    async def __aexit__(self, exc_type, exc, tb):
        if self._connection:
            await self._connection.close()

    async def process(self, measurement: Measurement) -> None:
        payload = json.dumps(measurement.to_json())
        message = aio_pika.Message(
            body=payload.encode(),
            content_type="application/json",
            delivery_mode=aio_pika.DeliveryMode.PERSISTENT
        )
        await self._exchange.publish(message, routing_key=self._routing_key)


class Sensor:
    _config: SensorConfig
    _time: datetime
    _logger: logging.Logger
    _processor: MeasurementProcessor

    def __init__(
        self,
        config: SensorConfig,
        processor: MeasurementProcessor,
        logger: logging.Logger = logging.getLogger("sd.s.main.sensor"),
        time: datetime = datetime.now(),
    ) -> None:
        self._config = config
        self._processor = processor
        self._logger = logger
        self._time = time

    async def start(self) -> None:
        reading_count = 0

        while reading_count < self._config.total_readings:
            await asyncio.sleep(self._config.simulated_generation_interval)
            self._time = self._time + timedelta(minutes=10)
            reading_count += 1
            value = ValueGenerator.get_by_time(
                self._time, self._config.max_consumption
            )

            self._logger.info(
                "STEP %d/%d | device_id=%s | max_consumption=%s | local_time=%s | generated=%s",
                reading_count,
                self._config.total_readings,
                self._config.device_id,
                self._config.max_consumption,
                self._time.isoformat(sep=" ", timespec="seconds"),
                value,
            )

            await self._processor.process(
                measurement=Measurement(
                    device_id=self._config.device_id,
                    measurement_value=value,
                    timestamp=self._time,
                )
            )


async def main() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    logger = logging.getLogger("sd.s.main")
    sensor: Optional[Sensor] = None

    try:
        sensor_config = SensorConfig.from_config_file("config.json")
        trace = load_trace()
        saved = trace.get(str(sensor_config.device_id))
        start_time = datetime.fromisoformat(saved) if saved else datetime.now()

        async with RabbitMeasurementProcessor() as measurement_processor:
            sensor = Sensor(sensor_config, measurement_processor, time=start_time)
            await sensor.start()
    except FileNotFoundError:
        logger.error("Config file not found")
    except json.JSONDecodeError as e:
        logger.error("Config file is not a valid json")
    except KeyError as e:
        logger.error("Missing required config field: %s", e)
    except KeyboardInterrupt:
        logging.info("Simulation interrupted by user (KeyboardInterrupt).")
    except Exception as e:
        logging.exception("Unhandled error during simulation: %s", e)
    else:
        logging.info("Simulation completed successfully")
    finally:
        try:
            if sensor is not None:
                save_trace(sensor._config.device_id, sensor._time)
                logger.info("Saved trace | device_id=%s | next_start_date=%s", sensor._config.device_id, sensor._time.isoformat())
        except Exception as e:
            logger.exception("Failed saving sensor end_date: %s", e)


if __name__ == "__main__":
    asyncio.run(main())

