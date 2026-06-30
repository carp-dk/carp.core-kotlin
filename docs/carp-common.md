# carp.common [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.common/carp.common)](https://central.sonatype.com/artifact/dk.cachet.carp.common/carp.common) [![Snapshot](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdk%2Fcachet%2Fcarp%2Fcommon%2Fcarp.common%2Fmaven-metadata.xml)](https://central.sonatype.com/repository/maven-snapshots/dk/cachet/carp/common/carp.common/maven-metadata.xml)

Implements helper classes and base types relied upon by all subsystems.
Primarily, this contains the built-in types used to [define study protocols](carp-protocols.md#domain-objects)
which subsequently get passed to the deployments and clients subsystem.

## Data types

`DataType`s are identified by a given _name_ within a _namespace_ and identify the data contained within matching [`Data`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/Data.kt) points.
When a data type describes data relating to a time interval, the time interval isn't stored in the data point body, but in the header of the data point, common to all data point types (e.g., `interbeatinterval`).

[Sensor data types](#sensor-data-types) and [task data types](#task-data-types) can be accessed through [`CarpDataTypes`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/CarpDataTypes.kt).
They all reside under the namespace: **dk.cachet.carp**.

[Input data types](#input-data-types) can be accessed through [`CarpInputDataTypes`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/input/CarpInputDataTypes.kt).
They all reside under the namespace: **dk.cachet.carp.input**.

### Sensor data types

Passively collected data by hardware sensors is called `SensorData` (implements `Data`).
Sensor data types are device-agnostic.
The goal is that they can be reused for devices by different vendors.
They act as a common data format.

If device-specific data is needed, it can be passed using the `sensorSpecificData` field, which is of type `Data`.
Custom infrastructures built using CARP Core can specify custom `Data` types in their own codebase to hold sensor-specific data.
All [extendable domain objects](carp-protocols.md#extending-domain-objects), including `Data`, can be uploaded to CARP backends that [use the recommended CARP serializers](serialization.md#unknownpolymorphicserializer-deserializing-unknown-types);
they don't need the types at compile time or runtime, although then the data won't be validated on upload.

| Name                                                                                                                                        | Description                                                                                        |
|---------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| [geolocation](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/Geolocation.kt)                                   | Geographic location data, representing longitude and latitude.                                     |
| [stepcount](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/StepCount.kt)                                       | The number of steps a participant has taken in a specified time interval.                          |
| [ecg](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/ECG.kt)                                                   | Electrocardiogram data of a single lead.                                                           |
| [ppg](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/PPG.kt)                                                   | Photoplethysmogram data for one or more individually emitting light sources.                       |
| [heartrate](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/HeartRate.kt)                                       | Number of heart contractions (beats) per minute.                                                   |
| [interbeatinterval](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/InterbeatInterval.kt)                       | The time interval between two consecutive heartbeats.                                              |
| [sensorskincontact](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/SensorSkinContact.kt)                       | Whether a sensor requiring contact with skin is making proper contact at a specific point in time. |
| [nongravitationalacceleration](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/NonGravitationalAcceleration.kt) | Acceleration excluding gravity along perpendicular x, y, and z axes.                               |
| [eda](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/EDA.kt)                                                   | Single-channel electrodermal activity, represented as skin conductance.                            | 
| [angularvelocity](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/AngularVelocity.kt)                           | Rate of rotation around perpendicular x, y, and z axes.                                            |
| [signalstrength](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/SignalStrength.kt)                             | The received signal strength of a wireless device.                                                 |

### Task data types

Data related to when tasks (configured using [`TaskConfiguration`](#task-configurations)) were triggered (configured using [`TriggerConfiguration`](#trigger-configurations)) or completed.

| Name                                                                                                                                        | Description                                                                                        |
|---------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| [triggeredtask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/TriggeredTask.kt)                               | A task which was started or stopped by a trigger, referring to identifiers in the study protocol.  |
| [completedtask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/CompletedTask.kt)                               | An interactive task which was completed over the course of a specified time interval.              |

### Input data types

Data which is typically entered by users, as opposed to collected passively.
`CarpInputDataTypes` links these data types to a reduced set of [`InputElement`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/input/elements/InputElement.kt) abstractions.
UI frameworks can map each `InputElement` type to corresponding input fields.
Using helper functions in `CarpInputDataTypes`, the data input by users can be converted from/to strongly typed and validated input `Data`.

| Name                                                                                                          | Description                                        |
|---------------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| [sex](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/input/Sex.kt)               | Biological sex assigned at birth of a participant. |

## Device configurations

Implementations of [`DeviceConfiguration`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/DeviceConfiguration.kt) are used by the framework to describe the _type of device_ used to collect data, its _capabilities_,
and to _configure_ how it participates in the study protocol.

_Primary_ devices ([`PrimaryDeviceConfiguration`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/PrimaryDeviceConfiguration.kt)),
in addition to supporting data collection from internal sensors,
act as a hub to aggregate, synchronize, and upload incoming data received from one or more connected devices. 

| Class                                                                                                                          | Primary | Description                                                                                              |
|--------------------------------------------------------------------------------------------------------------------------------|:-------:|----------------------------------------------------------------------------------------------------------|
| [Smartphone](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/Smartphone.kt)                     |   Yes   | An internet-connected phone with built-in sensors.                                                       |
| [Website](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/Website.kt)                           |   Yes   | A website which participates in a study as a primary device.                                             |
| [AltBeacon](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/AltBeacon.kt)                       |         | A beacon meeting the open AltBeacon standard.                                                            |
| [BLEHeartRateDevice](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/BLEHeartRateDevice.kt)     |         | A Bluetooth device which implements a Heart Rate service.                                                |
| [CustomProtocolDevice](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/CustomProtocolDevice.kt) |   Yes   | A primary device which uses a single `CustomProtocolTask` to determine how to run a study on the device. |

## Sampling schemes and configurations

Supports specifying the sampling scheme for a [`DataType`](#data-types), including possible options, defaults, and constraints.

From sampling _schemes_, matching sampling _configurations_ can be created.
Per data type, only one `SamplingConfiguration` is ever active on a device.
The sampling configuration to be used is determined on clients in order of priority:

1. The sampling configuration, if specified in the study protocol, of the `Measure.DataStream` in the last triggered _active_ `TaskConfiguration`. 
   Once a task stops, it is no longer "active".
2. The default sampling configuration, if specified in the study protocol, for the `DeviceConfiguration`.
   This can be retrieved through `PrimaryDeviceDeployment` on the client.
3. The default sampling configuration hardcoded in the `Sensors` sampling schemes of the concrete `DeviceConfiguration`, if none of the previous configurations are present.

Some sampling schemes support specifying a different sampling configuration depending on how much battery is left,
indicated by the "Battery-aware" column.
These extend from [BatteryAwareSampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/BatteryAwareSampling.kt).

| Class | Battery-aware | Description |
| --- | :---: | --- |
| [AdaptiveGranularitySampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/AdaptiveGranularitySampling.kt) | Yes | Specify a desired level of granularity at which to measure depending on the battery level. |
| [GranularitySampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/GranularitySampling.kt) | | Specify a desired level of granularity, corresponding to expected degrees of power consumption. |
| [IntervalSampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/IntervalSampling.kt) | | Specify a time interval in between subsequent measurements. |
| [NoOptionsSampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/NoOptionsSampling.kt) | | Does not allow any sampling configuration. |

## Task configurations

| Class | Description |
| --- | --- |
| [BackgroundTask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/tasks/BackgroundTask.kt) | Specifies that all containing measures and/or output should immediately start running in the background once triggered. |
| [CustomProtocolTask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/tasks/CustomProtocolTask.kt) | Contains a definition on how to run tasks, measures, and triggers which differs from the CARP domain model. |
| [WebTask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/tasks/WebTask.kt) | Redirects to a web page which contains the task which needs to be performed. |

## Trigger configurations

| Class | Description |
| --- | --- |
| [ElapsedTimeTrigger](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/triggers/ElapsedTimeTrigger.kt) | Triggers after a specified amount of time has elapsed since the start of a study deployment. |
| [ScheduledTrigger](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/triggers/ScheduledTrigger.kt) | Trigger using a recurring schedule starting on the date that the study starts, specified using [the iCalendar recurrence rule standard](https://icalendar.org/iCalendar-RFC-5545/3-8-5-3-recurrence-rule.html). |
| [ManualTrigger](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/triggers/ManualTrigger.kt) | Initiated by a user, i.e., the user decides when to start a task. |
