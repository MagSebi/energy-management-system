# Energy Management System (EMS) - Assignment 3

## Overview

This assignment extends the Energy Management System with **real-time communication**, **customer support chatbot**, and **scalable load balancing** for device data processing.

### New Components (Assignment 3)

1. **Customer Support (Chat) Microservice** - Provides interactive communication between clients and administrators with rule-based and AI-driven automated responses
2. **WebSocket Microservice** - Handles real-time transport of chat messages and system notifications
3. **Load Balancing Service** - Distributes device data across monitoring microservice replicas using consistent hashing

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (React)                      │
│         Chat Interface + Energy Monitoring              │
└────────────────┬──────────────────────┬──────────────────┘
                 │                      │
          ┌──────▼───────┐      ┌──────▼──────────┐
          │ Chat API     │      │ WebSocket API   │
          │ :8086        │      │ :8084           │
          └──────┬───────┘      └────────┬────────┘
                 │                       │
          ┌──────▼─────────────────────────────┐
          │       RabbitMQ Message Broker       │
          │    (chat.exchange, device.data)    │
          └─────────┬──────────────┬────────────┘
                    │              │
         ┌──────────▼──┐    ┌──────▼─────────────┐
         │ Chat Service│    │ Load Balancer      │
         │ (Rules + AI)│    │ (Consistent Hash)  │
         └─────────────┘    └──────┬──────────────┘
                                   │
                    ┌──────────────┴───────────────┐
                    │                              │
           ┌────────▼──────┐          ┌───────────▼─────┐
           │ Monitoring-1  │          │ Monitoring-2    │
           │ (Replica 1)   │          │ (Replica 2)     │
           └───────────────┘          └─────────────────┘
```

## Features Implemented

### 1. Customer Support (Chat) Microservice

#### Rule-Based Chatbot (12 Rules)
Messages are automatically matched against predefined rules:
- **program** → Support hours: L-V 09:00-18:00
- **orar** → Availability info
- **consum** → Guide to Energy Monitoring section
- **parola** → Password reset instructions
- **factura** → Invoice information
- **alerta** → Overconsumption alerts
- **device** → Adding new devices
- **cost** → Energy cost calculation
- **logout** → Logout instructions
- **profil** → Profile update guide
- **notificare** → Real-time notifications
- **suport** → Support contact info

#### AI-Driven Support (Optional)
When no rule matches:
1. Try to generate response using LLM (OpenAI/Mistral)
2. Fall back to admin forwarding if AI is disabled/unavailable

Configuration:
```properties
ai.api.type=${AI_API_TYPE:disabled}  # or "openai", "mistral"
ai.api.key=${AI_API_KEY:}
```

### 2. WebSocket Microservice

**Responsibilities:**
- Real-time delivery of chat messages from backend to frontend
- Streaming overconsumption alerts to visualization platform
- Persistent connections for bidirectional communication

**Endpoints:**
- `ws://localhost:8081/ws/chat?role=CLIENT&user_id=<UUID>` - Chat stream
- `ws://localhost:8081/ws/energy?device_id=<UUID>` - Energy data stream

**Features:**
- Automatic JSON serialization with Java 8 date/time support
- Concurrent session management per user
- Graceful connection handling

### 3. Load Balancing Service

**Purpose:** Distribute device data across multiple monitoring replicas for scalability

**Strategy:** Consistent Hashing
- Device data is hashed consistently to the same replica
- Ensures related messages are processed together
- Configurable replica keys

**Configuration:**
```properties
rabbitmq.routing.keys.ingest=device.data.ingest.1,device.data.ingest.2
```

**Flow:**
1. Devices publish to `device.data.exchange`
2. Load Balancer consumes from central queue
3. Selects replica using consistent hash of deviceId
4. Forwards to replica-specific ingest queue

## Prerequisites

- Docker & Docker Compose
- Available ports: 80, 5672 (RabbitMQ), 5432-5435 (PostgreSQL), 8084 (WebSocket), 8086 (Chat)

## Build and Run

### Start All Services
```bash
docker compose up --build
```

### Stop Services
```bash
docker compose down
```

### Stop and Remove Data
```bash
docker compose down -v
```

## Access Points

| Service | URL | Purpose |
|---------|-----|---------|
| Frontend | http://localhost:8081 | React UI with chat and monitoring |
| Traefik Dashboard | http://localhost:8089 | Reverse proxy dashboard |
| Chat API | http://localhost:8081/api/chat | Chat message endpoint |
| WebSocket Chat | ws://localhost:8081/ws/chat | Real-time chat stream |
| WebSocket Energy | ws://localhost:8081/ws/energy | Real-time energy data |
| RabbitMQ UI | http://localhost:15672 | Message broker management (guest/guest) |

## API Endpoints

### Chat Service

**Send Message**
```http
POST /api/chat/message
Content-Type: application/json

{
  "userId": "d2069db5-724a-461f-9156-64c204a236e6",
  "content": "Care este programul de suport?"
}
```

**Response Types:**
- `auto-replied` - Rule matched, automatic response sent
- `ai-replied` - AI generated response (if enabled)
- `forwarded` - Message forwarded to admin

**Health Check**
```http
GET /api/chat/health
```

## Docker Services

| Service | Port | Image | Purpose |
|---------|------|-------|---------|
| frontend2 | 80 | Node 20 Alpine | React frontend (Nginx) |
| chat-app | 8086 | Spring Boot 3.3.2 | Customer support microservice |
| websocket-app | 8084 | Spring Boot 3.3.2 | Real-time messaging |
| lb-app | 8090 | Spring Boot 3.3.2 | Load balancer |
| monitoring-app-1 | 8080 | Spring Boot 3.3.2 | Energy monitoring replica 1 |
| monitoring-app-2 | 8080 | Spring Boot 3.3.2 | Energy monitoring replica 2 |
| auth-app | 8080 | Spring Boot 3.3.2 | Authentication service |
| user-app | 8080 | Spring Boot 3.3.2 | User management |
| device-app | 8080 | Spring Boot 3.3.2 | Device management |
| rabbitmq | 5672 | RabbitMQ 3.13 | Message broker |
| traefik | 80, 8080 | Traefik 3.0 | Reverse proxy |
| user-db, auth-db, device-db, monitoring-db | 5432-5435 | PostgreSQL 16 | Databases |

## Message Flow Examples

### Chat Message Flow
```
1. User sends message via React UI
2. POST /api/chat/message
3. Chat Service checks RuleEngine
4. If matched: Auto-reply via RabbitMQ → WebSocket
5. If no match: Try AI (if enabled)
6. If AI fails: Forward to Admin via RabbitMQ → WebSocket
7. WebSocket broadcasts to connected clients
```

### Device Data Flow
```
1. Device Simulator publishes to device.data.exchange
2. Load Balancer consumes from device.data.central.queue
3. Consistent Hash(deviceId) → selects replica (1 or 2)
4. Forwards to device.data.ingest.1 or device.data.ingest.2
5. Monitoring Replica processes and aggregates
6. Publishes hourly summaries to energy.data.exchange
7. WebSocket forwards to frontend via ws://localhost/ws/energy
```

## Configuration

### Environment Variables

```bash
# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Chat Service (Optional AI)
AI_API_TYPE=disabled        # or "openai", "mistral"
AI_API_KEY=your-api-key     # Only if AI is enabled

# Monitoring Service
OVERCONSUMPTION_THRESHOLD=50  # kWh threshold for alerts

# Server Ports
SERVER_PORT=8086            # Chat Service
SERVER_PORT=8084            # WebSocket Service
SERVER_PORT=8090            # Load Balancer
```

## Testing the Chat Feature

### Send a Message with Rule Match
```bash
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "d2069db5-724a-461f-9156-64c204a236e6",
    "content": "Care este programul de suport?"
  }'
```

**Expected Response:**
```json
{
  "status": "auto-replied",
  "reply": "Programul suport este L-V 09:00-18:00."
}
```

### Send a Message without Rule Match
```bash
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "d2069db5-724a-461f-9156-64c204a236e6",
    "content": "Ce inseamna acest mesaj?"
  }'
```

**Expected Response:**
```json
{
  "status": "forwarded"
}
```

## Troubleshooting

### Chat Service Not Responding
1. Check if chat-app container is running: `docker ps | grep chat-app`
2. Check logs: `docker logs chat-app --tail 50`
3. Verify RabbitMQ connection: `docker logs chat-app | grep "Created new connection"`

### WebSocket Connection Fails
1. Ensure WebSocket service is running: `docker ps | grep websocket-app`
2. Check firewall allows port 8084
3. Verify frontend uses correct WebSocket URL: `ws://<host>/ws/chat`

### Load Balancer Not Distributing
1. Check RabbitMQ queues: http://localhost:15672
2. Verify routing keys: `device.data.ingest.1` and `device.data.ingest.2`
3. Monitor logs: `docker logs lb-app --tail 50`

## Known Limitations

1. **AI Support:** Disabled by default. Enable by setting `AI_API_TYPE` and `AI_API_KEY`
2. **Chat Persistence:** Messages are not persisted (in-memory only during session)
3. **Admin Interface:** No dedicated admin UI for responding to forwarded messages
4. **Load Balancer:** Consistent hashing doesn't rebalance when replicas change

## Future Enhancements

1. Persist chat messages to database
2. Implement admin dashboard for responding to users
3. Add dynamic replica management with load detection
4. Integrate with production LLM APIs (OpenAI, Anthropic)
5. Add encryption for WebSocket connections (WSS)
6. Implement message rate limiting and abuse detection

## License

University Assignment - Academic Use Only

## Authors

- Chat Service & WebSocket Integration
- Load Balancing & Replica Distribution
- AI-Driven Customer Support
