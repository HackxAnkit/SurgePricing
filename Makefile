.PHONY: help build up down logs test load-test clean

help:
	@echo "Surge Pricing System - Make Commands"
	@echo "====================================="
	@echo "  make build      - Build Docker images"
	@echo "  make up         - Start all services"
	@echo "  make down       - Stop all services"
	@echo "  make restart    - Restart all services"
	@echo "  make logs       - View logs (all services)"
	@echo "  make logs-app   - View application logs only"
	@echo "  make test       - Run quick test suite"
	@echo "  make load-test  - Run k6 load tests"
	@echo "  make redis-cli  - Open Redis CLI"
	@echo "  make kafka-topics - List Kafka topics"
	@echo "  make clean      - Clean up everything"
	@echo "  make metrics    - Show Prometheus metrics"

build:
	@echo "Building Docker images..."
	docker-compose build

up:
	@echo "Starting services..."
	docker-compose up -d
	@echo "Waiting for services to be ready..."
	@sleep 10
	@echo "Services started! Access at http://localhost"

down:
	@echo "Stopping services..."
	docker-compose down

restart: down up

logs:
	docker-compose logs -f

logs-app:
	docker-compose logs -f surge-app-1 surge-app-2

test:
	@echo "Running test suite..."
	@chmod +x test-system.sh
	@./test-system.sh

load-test:
	@echo "Running k6 load tests..."
	@command -v k6 >/dev/null 2>&1 || { echo "k6 not installed. Install from https://k6.io"; exit 1; }
	k6 run load-test-k6.js

redis-cli:
	docker exec -it redis redis-cli

kafka-topics:
	docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

clean:
	@echo "Cleaning up..."
	docker-compose down -v
	docker system prune -f

metrics:
	@echo "Fetching metrics..."
	@curl -s http://localhost:8080/actuator/prometheus | grep -E "http_server_requests|jvm_memory|redis"

scale:
	@echo "Scaling to 4 application instances..."
	docker-compose up -d --scale surge-app-1=2 --scale surge-app-2=2

health:
	@echo "Checking service health..."
	@curl -s http://localhost/health && echo ""
	@curl -s http://localhost/price/health && echo ""
	@curl -s http://localhost/driver/health && echo ""