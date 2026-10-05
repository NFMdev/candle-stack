# Project Deep Dive Documentation

## Generator Service
Generator service is a module that acts as a market data generation simulator. Simulates clients that emit trading events.

### Domain
#### Engine
The Engine represents the event factory.

Currently it allows to create different basic scenarios defining their status, trading symbols, etc.
In the future it will be possible to configure trading rates, overloads, and other advanced configurations.

Only relevant current class is `EventFactory.java`

#### Delivery Config & Delivery Mode
`DeliveryConfig.java` and `DeliveryMode.java`
Allow to configure ingestion url for each scenario and request mode

#### Market Trade Event
`MarketTradeEvent` class is the basic trading event model

#### Simulation Scenario, Scenario Status & Simulation Runtime State
`SimulationScenario` class is the scenario entity definition
`SimulationRuntimeSate` is the runtime status for each scenario
`ScenarioStatus` enum is the different status a scnario might be (currently: `RUNNING` or `STOPPED`)

I have changed the status control from SimulationScenario to SimulationRuntimeState to separate responsibilities, being SimulationScenario just the definition and SimulationRuntimeState the status source. Advanced status control and handling is pending. `ScenarioApplicationService` & `ScenarioApplicationServiceTest` are directly affected by this change and will require update.

#### Symbol Config
`SymbolConfig` class allows to create and store symbols

#### Trade Side
`TradeSide` enum defines the two different sides of trading "SELL" and "BUY"

### Runtime

#### Scenario Runner
Manages scenarios, event creation and delivery.

### Infrastructure
#### HttpIngestionClient
Implementation of `EventDeliveryPort` used to emit trade events. Since `HttpIngestionClient` is the only implementation, any reference to its parent interface will be automatically treated as HttpIngestionClient.


#### SimulationLifecycleManager
Manages runners and scenario states

## Ingestion Service
Ingestion Service is the actual kafka producer which ingests from simulated clients (generator-service instances) and publishes to kafka "market-trades" topic.
### Domain
#### MarketTradeEvent
`MarketTradeEvent` class is the basic trading event model
#### TradeSide
`TradeSide` enum defines the two different sides of trading "SELL" and "BUY"
#### MarketEventPublisherPort
`MarketEventPublisherPort` interface with publishing method for kafka messages

### Infrastructure
#### KafkaMarketEventPublisher
Class implementation for `MarketEventPublisherPort`

### Config
#### KafkaTopicsProperties
Kafka topic definition and representation

## Processing Service
Currently Processing Service stores one snapshot for every different trading instrument with important informtion: instrumentId, lastEventId, tradingDate, lastPrice, lastQuantity, lastEventTime, dayVolume, dayHigh, dayLow, tradeCount, updatedAt and currency

### Domain
#### Instrument Snapshot
`InstrumentSnapshot` class stores relevant information of specific instrument at specific time
#### Update Decision
`UpdateDecision` enum defines different possible results for trade processing and snapshot update
#### SnapshotUpdateResult
Joins information from `InstrumentSnapshot` and `UpdateDecision` separating instrument definition from its state

