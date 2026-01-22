#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost"

echo "================================================"
echo "Surge Pricing System - Quick Test Suite"
echo "================================================"

# Function to test endpoint
test_endpoint() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_status=$5

    echo -n "Testing $name... "

    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
    fi

    status=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)

    if [ "$status" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (Status: $status)"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (Expected: $expected_status, Got: $status)"
        echo "Response: $body"
        return 1
    fi
}

# Wait for services to be ready
echo -e "\n${YELLOW}Waiting for services to start...${NC}"
sleep 5

# Test 1: Health Check
test_endpoint "Health Check" "GET" "$BASE_URL/health" "" "200"

# Test 2: Pricing API Health
test_endpoint "Pricing Health" "GET" "$BASE_URL/price/health" "" "200"

# Test 3: Driver Health
test_endpoint "Driver Health" "GET" "$BASE_URL/driver/health" "" "200"

# Test 4: Get Price
test_endpoint "Get Price" "GET" "$BASE_URL/price?lat=37.7749&lng=-122.4194" "" "200"

# Test 5: Submit Driver Location
driver_payload='{
  "driverId": "test_driver_001",
  "lat": 37.7749,
  "lng": -122.4194,
  "timestamp": '$(date +%s000)'
}'
test_endpoint "Submit Driver Location" "POST" "$BASE_URL/driver/location" "$driver_payload" "202"

# Test 6: Load test simulation
echo -e "\n${YELLOW}Running quick load simulation (10 seconds)...${NC}"
for i in {1..100}; do
    lat=$(echo "37.7749 + ($RANDOM % 100) * 0.001" | bc)
    lng=$(echo "-122.4194 + ($RANDOM % 100) * 0.001" | bc)
    curl -s "$BASE_URL/price?lat=$lat&lng=$lng" > /dev/null &
done

wait
echo -e "${GREEN}✓ Load simulation completed${NC}"

# Test 7: Multiple driver submissions
echo -e "\n${YELLOW}Submitting 50 driver locations...${NC}"
for i in {1..50}; do
    driver_id="driver_$(printf "%04d" $i)"
    lat=$(echo "37.7749 + ($RANDOM % 100) * 0.001" | bc)
    lng=$(echo "-122.4194 + ($RANDOM % 100) * 0.001" | bc)

    payload="{\"driverId\":\"$driver_id\",\"lat\":$lat,\"lng\":$lng,\"timestamp\":$(date +%s000)}"
    curl -s -X POST -H "Content-Type: application/json" -d "$payload" "$BASE_URL/driver/location" > /dev/null &
done

wait
echo -e "${GREEN}✓ Submitted 50 driver locations${NC}"

# Test 8: Wait and check if surge updated
echo -e "\n${YELLOW}Waiting 20 seconds for surge calculation...${NC}"
sleep 20

echo -n "Checking surge values... "
response=$(curl -s "$BASE_URL/price?lat=37.7749&lng=-122.4194")
surge=$(echo $response | grep -o '"surgeMultiplier":[0-9.]*' | cut -d':' -f2)

if [ ! -z "$surge" ]; then
    echo -e "${GREEN}✓ Surge: ${surge}x${NC}"
else
    echo -e "${RED}✗ No surge data${NC}"
fi

# Summary
echo -e "\n================================================"
echo -e "${GREEN}Test suite completed!${NC}"
echo "================================================"
echo ""
echo "Next steps:"
echo "  1. Run full load test: k6 run load-test-k6.js"
echo "  2. Monitor logs: docker-compose logs -f"
echo "  3. Check Redis: docker exec -it redis redis-cli"
echo "  4. View metrics: curl http://localhost:8081/actuator/prometheus"
echo ""