# carp.protocols [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.protocols/carp.protocols.core)](https://central.sonatype.com/artifact/dk.cachet.carp.protocols/carp.protocols.core) [![Snapshot](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdk%2Fcachet%2Fcarp%2Fprotocols%2Fcarp.protocols.core%2Fmaven-metadata.xml)](https://central.sonatype.com/repository/maven-snapshots/dk/cachet/carp/protocols/carp.protocols.core/maven-metadata.xml) 

Implements open standards which can describe a study protocol—how a study should be run.
Essentially, this subsystem has no technical dependencies on any particular sensor technology or application as it merely describes why, when, and what data should be collected.

## Domain objects

To configure a `StudyProtocol`, the following domain objects and [common CARP types](carp-common.md) are involved:

![Protocols Domain Objects](https://i.imgur.com/KWIDXf7.png)

- [`StudyProtocol`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/StudyProtocol.kt):
A description of how a study is to be executed, defining the 'primary' devices responsible for aggregating data, the optional devices connected to them, and the `Trigger`s which lead to data collection on said devices.
- [`DeviceConfiguration`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/DeviceConfiguration.kt):
Describes any type of electronic device, such as a sensor, video camera, desktop computer, or smartphone that collects data which can be incorporated into the platform after it has been processed by a 'primary device' (potentially itself).
- [`PrimaryDeviceConfiguration`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/PrimaryDeviceConfiguration.kt):
A device which aggregates, synchronizes, and optionally uploads incoming data received from one or more connected devices (potentially just itself).
Typically, a desktop computer, smartphone, or web server.
- [`TaskConfiguration`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/tasks/TaskConfiguration.kt):
Describes requested `Measure`s and/or output to be presented on a device.
- [`Measure`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/tasks/Measure.kt):
Defines data that needs to be measured/collected passively for a supported `DataType`.
- [`DataType`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/DataType.kt):
Defines a type of data which can be processed by the platform (e.g., measured/collected/uploaded).
- [`TriggerConfiguration`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/triggers/TriggerConfiguration.kt):
Any condition on a device which starts or stops tasks at certain points in time when the condition applies.
The condition can either be time-bound, based on incoming data, initiated by a user of the platform, or a combination of these.
- [`TaskControl`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/TaskControl.kt):
Specifies a task which at some point during a `StudyProtocol` gets sent to a specific device.
This allows modeling triggers which trigger multiple tasks targeting multiple devices.
- [`SamplingConfiguration`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/SamplingConfiguration.kt):
Contains configuration on how to sample data.
- [`DataTypeSamplingScheme`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/DataTypeSamplingScheme.kt):
Specifies the possible sampling configuration options for a `DataType`, including defaults and constraints.

Most of these are abstract base types. For information on concrete types extending on these, check the [common subsystem documentation](carp-common.md).

## Extending domain objects

In case the [currently supported built-in types](carp-common.md) do not provide the functionality you require, the following abstract classes can be extended to model your own custom study logic:

- Extend `DeviceConfiguration` or `PrimaryDeviceConfiguration` to add support for a new type of device, and extend `DeviceRegistration` to specify how a single instance of this device should be uniquely identified, the capabilities it has, and device-specific configuration options needed for the device to operate.
Example: [`AltBeacon`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/AltBeacon.kt).  
- Extend `TaskConfiguration` to provide custom logic on how to schedule the containing `Measure`s, or if you need to trigger custom tasks unrelated to the study protocol in your client application.
- Specify new `DataType`s by extending from `Data`, and optionally extend from `DataTypeSamplingScheme` and `SamplingConfiguration` to specify a custom configuration on _how_ your new data type can be measured on a specific device.
Example: [`Geolocation`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/Geolocation.kt) and [`IntervalSampling`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/IntervalSampling.kt).
- Extend `TriggerConfiguration` to describe custom conditions which you want to use to trigger tasks.

All extending classes (except `DataTypeSamplingScheme`) should be **immutable data classes**.
The domain objects using them expect them to remain unchanged (they are [DDD value objects](https://deviq.com/value-object/)).

Add a `@Serializable` annotation to the class so that it can be serialized by `kotlinx.serialization`.
In most cases this is sufficient, but for more information, check [the serialization documentation for CARP developers](serialization.md).

## Application services

### [`ProtocolService`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/application/ProtocolService.kt)

Allows managing multiple versions of study protocols.

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.
New users that are allowed to add protocols should be given a 'protocol owner' claim, e.g., their user ID.
In case you want to support organizations this could be the ID of the organization they belong to.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `add` | Add a study protocol. | protocol owner: `protocol.ownerId` |  |
| `addVersion` | Add a new version for a specified study protocol. | protocol owner: `protocol.ownerId` | |
| `updateParticipantDataConfiguration` | Replace expected participant data for a specified study protocol. | protocol owner: `protocol.ownerId` | |
| `getBy` | Find the study protocol with the specified id. | protocol owner: `protocol.ownerId` | |
| `getAllForOwner` | Find all study protocols owned by a specific owner. | protocol owner: `ownerId` | |
| `getVersionHistoryFor` | Returns all stored versions for the study protocol with the given id. |  protocol owner: `protocol.ownerId` | |

### [`ProtocolFactoryService`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/application/ProtocolFactoryService.kt)

Provides factory methods to create common study protocols.
To store these protocols, pass the returned protocols to `ProtocolService`.

| Endpoint | Description |
| --- | --- |
| `createCustomProtocol` | Create a study protocol to be deployed to a single device which has its own way of describing study protocols that deviates from the CARP core study protocol model. |