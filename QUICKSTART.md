# Surge Pricing Engine - Quickstart

## Prereqs
- Java 17
- Kafka running on `localhost:9092`
- Redis running on `localhost:6379`

## Run the service
```
./mvnw spring-boot:run
```

## Run the UI (React)
```
cd frontend
npm install
npm run dev
```

## Simulation & load testing
```
python3 -m venv .venv
source .venv/bin/activate
pip install -r scripts/requirements.txt
```

### Ingestion only (5k updates/sec for 30s)
```
python scripts/simulate.py ingest --rps 5000 --duration 30
```

### Price read load (10k req/sec for 20s)
```
python scripts/simulate.py price --rps 10000 --duration 20
```

### Full scenario (warmup → 50% drop → pause)
```
python scripts/simulate.py scenario
```

### Custom geofences
```
python scripts/simulate.py ingest --points "37.7749,-122.4194;37.7849,-122.4094"
```

## API examples
```
curl -X POST http://localhost:8081/rider/book \
  -H "Content-Type: application/json" \
  -d '{"riderId":"rider_1","pickupLat":29.3446,"pickupLng":79.5644,"dropLat":29.3806,"dropLng":79.4636}'

curl "http://localhost:8081/driver/availability?lat=29.3446&lng=79.5644"
```