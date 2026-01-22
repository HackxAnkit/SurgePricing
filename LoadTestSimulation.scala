package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

class LoadTestSimulation extends Simulation {

  // Configuration
  val baseUrl = sys.env.getOrElse("BASE_URL", "http://localhost")
  val pricingRps = 10000 // 10k requests per second
  val driverRps = 5000   // 5k requests per second
  val testDuration = 60  // 60 seconds

  // HTTP Protocol
  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .shareConnections

  // Random data generators
  def randomLat: Double = 37.7 + Random.nextDouble() * 0.1
  def randomLng: Double = -122.5 + Random.nextDouble() * 0.1
  def randomDriverId: String = s"driver_${Random.nextInt(10000)}"

  // Pricing API scenario
  val pricingScenario = scenario("Pricing API Load Test")
    .exec(
      http("Get Price")
        .get("/price")
        .queryParam("lat", _ => randomLat)
        .queryParam("lng", _ => randomLng)
        .check(status.is(200))
        .check(jsonPath("$.baseFare").exists)
        .check(jsonPath("$.surgeMultiplier").exists)
        .check(jsonPath("$.finalPrice").exists)
        .check(responseTimeInMillis.lte(100)) // P95 < 100ms requirement
    )

  // Driver location update scenario
  val driverUpdateScenario = scenario("Driver Location Update Load Test")
    .exec(
      http("Update Driver Location")
        .post("/driver/location")
        .body(StringBody(session =>
          s"""{
            "driverId": "${randomDriverId}",
            "lat": ${randomLat},
            "lng": ${randomLng},
            "timestamp": ${System.currentTimeMillis()}
          }"""
        ))
        .check(status.is(202))
        .check(jsonPath("$.status").is("accepted"))
    )

  // Setup test execution
  setUp(
    // Pricing API: 10k RPS with constant rate
    pricingScenario.inject(
      constantUsersPerSec(pricingRps) during (testDuration seconds)
    ).protocols(httpProtocol),

    // Driver Updates: 5k RPS with constant rate
    driverUpdateScenario.inject(
      constantUsersPerSec(driverRps) during (testDuration seconds)
    ).protocols(httpProtocol)
  ).assertions(
    // Global assertions
    global.responseTime.percentile(95).lt(100),
    global.failedRequests.percent.lt(1.0)
  )
}