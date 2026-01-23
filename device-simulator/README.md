# Device Simulator - Python Version

Simulează consumul de energie pentru dispozitive smart, generând date la fiecare 10 minute simulate.

## Cerințe

- Python 3.8+
- RabbitMQ (rulând pe localhost:5672 sau în Docker)
Apasă `Ctrl+C` pentru a opri simularea. Starea va fi salvată automat.

## Oprire

```
RABBIT_PORT=5672
RABBIT_HOST=localhost
RABBIT_PASSWORD=guest
RABBIT_USER=guest
```bash

## Variabile de mediu

```
}
  "timestamp": "2025-12-11T10:30:00"
  "measurement_value": 145.32,
  "device_id": "uuid",
{
```json
Format JSON:

- **Routing Key**: `device.data.routing.key`
- **Exchange**: `device.data.exchange` (type: topic)
Mesajele sunt trimise la:

## RabbitMQ

Simulatorul salvează progresul în `.trace.json`. Dacă este întrerupt, va continua de unde a rămas.

## State Persistence

- Durată totală: 24 ore
- 1 citire la fiecare 10 minute
- 144 citiri total
```
}
  "max_consumption": 150.0
  "simulated_day_count": 1,
  "simulated_day_duration": 86400,
  "device_id": "uuid-here",
{
```json
**Simulare realistă (1 zi = 1 zi reală)**

- Durată totală: 144 secunde (2.4 minute)
- 1 citire la fiecare 0.5 secunde
- 288 citiri total
```
}
  "max_consumption": 150.0
  "simulated_day_count": 2,
  "simulated_day_duration": 72,
  "device_id": "uuid-here",
{
```json
**Test rapid (2 zile în 2.4 minute)**

### Exemple:

- **Timp simulat**: 10 minute între fiecare citire
- **Interval între citiri**: `simulated_day_duration / 24 / 6` secunde
- **Total citiri**: `simulated_day_count × 24 ore × 6 citiri/oră`
### Calcule:

- **Noapte (20:00-8:00)**: `max_consumption - (10 până la 15) kWh`
- **Zi (8:00-20:00)**: `max_consumption ± 5 kWh`
### Generare valori:

## Cum funcționează

```
python simulator.py
```bash

## Rulare

- **max_consumption**: Consumul maxim de referință în kWh
- **simulated_day_count**: Numărul de zile de simulat
- **simulated_day_duration**: Durata în secunde pentru o zi simulată (ex: 72 = 1 zi simulată în 72 secunde reale)
- **device_id**: UUID-ul dispozitivului (trebuie să existe în baza de date)

### Parametri:

```
}
  "max_consumption": 150.0
  "simulated_day_count": 2,
  "simulated_day_duration": 72,
  "device_id": "a47a1c72-4cb3-4606-8b4d-587e650e198c",
{
```json

Editează `config.json` cu parametrii doriti:

## Configurare

```
pip install -r requirements.txt
```bash

## Instalare


