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

#### SimulationLifecycleManager
Manages runners and scenario states

## Ingestion Service