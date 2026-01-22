#!/usr/bin/env python3
"""
Traffic simulator for Surge Pricing System
Simulates realistic driver movements and rider demand
"""

import requests
import random
import time
import json
import threading
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_URL = "http://localhost"

# San Francisco coordinates
SF_CENTER = {"lat": 37.7749, "lng": -122.4194}
SF_RADIUS = 0.05  # ~5km radius

class TrafficSimulator:
    def __init__(self, num_drivers=1000, num_riders=5000):
        self.num_drivers = num_drivers
        self.num_riders = num_riders
        self.drivers = {}
        self.session = requests.Session()
        self.stats = {
            "driver_updates": 0,
            "price_checks": 0,
            "errors": 0
        }

    def random_location(self):
        """Generate random location near SF"""
        lat = SF_CENTER["lat"] + random.uniform(-SF_RADIUS, SF_RADIUS)
        lng = SF_CENTER["lng"] + random.uniform(-SF_RADIUS, SF_RADIUS)
        return {"lat": lat, "lng": lng}

    def simulate_driver_movement(self, driver_id):
        """Simulate a driver moving around"""
        location = self.random_location()

        payload = {
            "driverId": driver_id,
            "lat": location["lat"],
            "lng": location["lng"],
            "timestamp": int(time.time() * 1000)
        }

        try:
            response = self.session.post(
                f"{BASE_URL}/driver/location",
                json=payload,
                timeout=2
            )

            if response.status_code == 202:
                self.stats["driver_updates"] += 1
            else:
                self.stats["errors"] += 1

        except Exception as e:
            self.stats["errors"] += 1
            print(f"Error updating driver {driver_id}: {e}")

    def simulate_price_check(self):
        """Simulate a rider checking price"""
        location = self.random_location()

        try:
            response = self.session.get(
                f"{BASE_URL}/price",
                params={"lat": location["lat"], "lng": location["lng"]},
                timeout=2
            )

            if response.status_code == 200:
                self.stats["price_checks"] += 1
                data = response.json()
                return data.get("surgeMultiplier", 1.0)
            else:
                self.stats["errors"] += 1

        except Exception as e:
            self.stats["errors"] += 1
            print(f"Error checking price: {e}")

        return None

    def run_driver_updates(self, duration_seconds=60):
        """Continuously update driver locations"""
        print(f"Starting driver location updates ({self.num_drivers} drivers)...")

        end_time = time.time() + duration_seconds

        with ThreadPoolExecutor(max_workers=50) as executor:
            while time.time() < end_time:
                futures = []

                # Update all drivers
                for i in range(self.num_drivers):
                    driver_id = f"driver_{i:04d}"
                    future = executor.submit(self.simulate_driver_movement, driver_id)
                    futures.append(future)

                # Wait for batch to complete
                for future in as_completed(futures):
                    pass

                # Update every 5 seconds
                time.sleep(5)

                print(f"[{datetime.now().strftime('%H:%M:%S')}] "
                      f"Driver updates: {self.stats['driver_updates']}, "
                      f"Errors: {self.stats['errors']}")

    def run_price_checks(self, duration_seconds=60):
        """Continuously check prices"""
        print(f"Starting price checks ({self.num_riders} concurrent)...")

        end_time = time.time() + duration_seconds
        surge_values = []

        with ThreadPoolExecutor(max_workers=100) as executor:
            while time.time() < end_time:
                futures = []

                # Simulate riders checking prices
                for _ in range(100):  # 100 checks per iteration
                    future = executor.submit(self.simulate_price_check)
                    futures.append(future)

                # Collect surge values
                for future in as_completed(futures):
                    surge = future.result()
                    if surge:
                        surge_values.append(surge)

                time.sleep(0.5)  # 200 RPS

        # Stats
        if surge_values:
            avg_surge = sum(surge_values) / len(surge_values)
            max_surge = max(surge_values)
            print(f"\nSurge stats: Avg={avg_surge:.2f}x, Max={max_surge:.2f}x")

    def run_simulation(self, duration_seconds=60):
        """Run full simulation"""
        print("=" * 60)
        print("TRAFFIC SIMULATION STARTING")
        print("=" * 60)
        print(f"Duration: {duration_seconds}s")
        print(f"Drivers: {self.num_drivers}")
        print(f"Price checks: High frequency")
        print()

        # Start both threads
        driver_thread = threading.Thread(
            target=self.run_driver_updates,
            args=(duration_seconds,)
        )
        price_thread = threading.Thread(
            target=self.run_price_checks,
            args=(duration_seconds,)
        )

        driver_thread.start()
        price_thread.start()

        driver_thread.join()
        price_thread.join()

        # Final stats
        print("\n" + "=" * 60)
        print("SIMULATION COMPLETE")
        print("=" * 60)
        print(f"Total driver updates: {self.stats['driver_updates']}")
        print(f"Total price checks: {self.stats['price_checks']}")
        print(f"Total errors: {self.stats['errors']}")
        print(f"Success rate: {100 * (1 - self.stats['errors'] / max(self.stats['driver_updates'] + self.stats['price_checks'], 1)):.2f}%")
        print()

def main():
    import argparse

    parser = argparse.ArgumentParser(description="Traffic Simulator for Surge Pricing")
    parser.add_argument("--drivers", type=int, default=500, help="Number of drivers")
    parser.add_argument("--duration", type=int, default=60, help="Duration in seconds")
    parser.add_argument("--url", type=str, default="http://localhost", help="Base URL")

    args = parser.parse_args()

    global BASE_URL
    BASE_URL = args.url

    simulator = TrafficSimulator(num_drivers=args.drivers)
    simulator.run_simulation(duration_seconds=args.duration)

if __name__ == "__main__":
    main()