Overview
Energy Management System with real-time communication, customer support chatbot, and scalable load balancing.
Microservices: Authentication, User Management, Device Management, Monitoring (2 replicas), Chat, WebSocket, Load Balancer.
Frontend: React with real-time energy monitoring and chat support.

Prerequisites
- Docker & Docker Compose
- Available ports: 8081 (web), 8089 (Traefik dashboard), 5672 (RabbitMQ), 5432-5435 (PostgreSQL)

Build and Run

Start all services:
docker compose up --build

Stop services:
docker compose down

Stop and remove data:
docker compose down -v

Access Points
Frontend: http://localhost:8081
Traefik Dashboard: http://localhost:8089
RabbitMQ Management: http://localhost:15672 (guest/guest)

APIs (via Traefik, port 8081):
- Authentication: http://localhost:8081/api/auth
- User Management: http://localhost:8081/api/user
- Device Management: http://localhost:8081/api/device
- Monitoring: http://localhost:8081/api/monitoring
- Chat: http://localhost:8081/api/chat

WebSocket (via Traefik):
- Chat: ws://localhost:8081/ws/chat
- Energy data: ws://localhost:8081/ws/energy

PostgreSQL databases (for debugging):
- User DB: localhost:5432 (postgres/root)
- Device DB: localhost:5433 (postgres/root)
- Auth DB: localhost:5434 (postgres/root)
- Monitoring DB: localhost:5435 (postgres/root)

Assignment 3 Components
- Customer Support Chat Service (port 8086): rule-based chatbot (12 rules) + optional AI (OpenAI/Mistral)
- WebSocket Service (port 8084): real-time chat messages and energy alerts
- Load Balancer (port 8090): distributes device data across 2 monitoring replicas using consistent hashing
- Monitoring replicas (2x): consume from per-replica ingest queues, aggregate hourly energy, emit alerts
- Device Simulator (Python): publishes synthetic device measurements to RabbitMQ

Messaging (RabbitMQ)
- Device data: device.data.exchange → load balancer → device.data.ingest.{1,2} → monitoring replicas
- Sync events: synchronization.exchange (USER/DEVICE CRUD operations)
- Chat: chat.exchange → websocket → frontend
- Energy & alerts: energy.hourly.exchange → websocket → frontend

See README.md for complete documentation.
- Monitoring pipeline: device measurements → monitoring-service → hourly aggregation → published to 
energy.hourly.exchange → consumed by websocket-service for live UI updates.

WebSocket Utility
- Frontend maintains a WebSocket connection to websocket-service for real-time energy 
updates without page refresh, complementing REST endpoints for historical data.


