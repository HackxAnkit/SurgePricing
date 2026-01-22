import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const pricingLatency = new Trend('pricing_latency');
const driverUpdateLatency = new Trend('driver_update_latency');

// Test configuration
export const options = {
  scenarios: {
    // Pricing API load test - 50k concurrent users
    pricing_load: {
      executor: 'constant-arrival-rate',
      rate: 10000, // 10k requests per second
      timeUnit: '1s',
      duration: '60s',
      preAllocatedVUs: 500,
      maxVUs: 1000,
      exec: 'pricingTest',
    },
    // Driver location updates - 5k updates per second
    driver_updates: {
      executor: 'constant-arrival-rate',
      rate: 5000, // 5k requests per second
      timeUnit: '1s',
      duration: '60s',
      preAllocatedVUs: 200,
      maxVUs: 400,
      exec: 'driverUpdateTest',
    },
  },
  thresholds: {
    'http_req_duration{scenario:pricing_load}': ['p(95)<100'], // P95 < 100ms
    'errors': ['rate<0.01'], // Error rate < 1%
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost';

// Generate random coordinates (San Francisco area)
function randomCoord() {
  const lat = 37.7 + Math.random() * 0.1;
  const lng = -122.5 + Math.random() * 0.1;
  return { lat, lng };
}

// Generate random driver ID
function randomDriverId() {
  return `driver_${Math.floor(Math.random() * 10000)}`;
}

// Pricing API test
export function pricingTest() {
  const { lat, lng } = randomCoord();
  const url = `${BASE_URL}/price?lat=${lat}&lng=${lng}`;

  const start = Date.now();
  const response = http.get(url, {
    headers: { 'Content-Type': 'application/json' },
    timeout: '2s',
  });
  const duration = Date.now() - start;

  pricingLatency.add(duration);

  const success = check(response, {
    'status is 200': (r) => r.status === 200,
    'has baseFare': (r) => JSON.parse(r.body).baseFare !== undefined,
    'has surgeMultiplier': (r) => JSON.parse(r.body).surgeMultiplier !== undefined,
    'has finalPrice': (r) => JSON.parse(r.body).finalPrice !== undefined,
    'latency < 100ms': () => duration < 100,
  });

  errorRate.add(!success);
}

// Driver location update test
export function driverUpdateTest() {
  const { lat, lng } = randomCoord();
  const payload = JSON.stringify({
    driverId: randomDriverId(),
    lat: lat,
    lng: lng,
    timestamp: Date.now(),
  });

  const start = Date.now();
  const response = http.post(`${BASE_URL}/driver/location`, payload, {
    headers: { 'Content-Type': 'application/json' },
    timeout: '2s',
  });
  const duration = Date.now() - start;

  driverUpdateLatency.add(duration);

  const success = check(response, {
    'status is 202': (r) => r.status === 202,
    'has accepted status': (r) => JSON.parse(r.body).status === 'accepted',
  });

  errorRate.add(!success);
}

// Summary handler
export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'load-test-results.json': JSON.stringify(data),
  };
}

function textSummary(data, options) {
  const indent = options?.indent || '';
  let summary = '\n' + indent + '='.repeat(80) + '\n';
  summary += indent + 'LOAD TEST SUMMARY\n';
  summary += indent + '='.repeat(80) + '\n\n';

  // Pricing API metrics
  if (data.metrics.pricing_latency) {
    summary += indent + 'Pricing API:\n';
    summary += indent + `  Requests: ${data.metrics.pricing_latency.values.count}\n`;
    summary += indent + `  P50: ${data.metrics.pricing_latency.values['p(50)']}ms\n`;
    summary += indent + `  P95: ${data.metrics.pricing_latency.values['p(95)']}ms\n`;
    summary += indent + `  P99: ${data.metrics.pricing_latency.values['p(99)']}ms\n`;
    summary += indent + `  Max: ${data.metrics.pricing_latency.values.max}ms\n\n`;
  }

  // Driver update metrics
  if (data.metrics.driver_update_latency) {
    summary += indent + 'Driver Updates:\n';
    summary += indent + `  Requests: ${data.metrics.driver_update_latency.values.count}\n`;
    summary += indent + `  P50: ${data.metrics.driver_update_latency.values['p(50)']}ms\n`;
    summary += indent + `  P95: ${data.metrics.driver_update_latency.values['p(95)']}ms\n`;
    summary += indent + `  P99: ${data.metrics.driver_update_latency.values['p(99)']}ms\n\n`;
  }

  // Error rate
  if (data.metrics.errors) {
    const errorPct = (data.metrics.errors.values.rate * 100).toFixed(2);
    summary += indent + `Error Rate: ${errorPct}%\n`;
  }

  summary += indent + '='.repeat(80) + '\n';
  return summary;
}