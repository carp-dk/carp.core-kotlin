# carp.data [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.data/carp.data.core)](https://central.sonatype.com/artifact/dk.cachet.carp.data/carp.data.core) [![Snapshot](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdk%2Fcachet%2Fcarp%2Fdata%2Fcarp.data.core%2Fmaven-metadata.xml)](https://central.sonatype.com/repository/maven-snapshots/dk/cachet/carp/data/carp.data.core/maven-metadata.xml) 

Contains all pseudonymized data.
When combined with the original study protocol, the full provenance of the data (when/why it was collected) is known.

## Data streams

[`Measurement`](../carp.data.core/src/commonMain/kotlin/dk/cachet/carp/data/application/Measurement.kt)'s contain measured [`Data`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/Data.kt) at a specific point or interval in time. 
`Measurement`'s of the same [`DataType`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/DataType.kt) coming from the same device in a study deployment are aggregated in _data streams_, identified by [`DataStreamId`](../carp.data.core/src/commonMain/kotlin/dk/cachet/carp/data/application/DataStreamId.kt).

Data streams are made up out of [`DataStreamPoint`](../carp.data.core/src/commonMain/kotlin/dk/cachet/carp/data/application/DataStreamPoint.kt)'s.
Multiple _subsequent_ `DataStreamPoint`'s can be stored as [`DataStreamSequence`](../carp.data.core/src/commonMain/kotlin/dk/cachet/carp/data/application/DataStreamSequence.kt),
which all share the same [`SyncPoint`](../carp.data.core/src/commonMain/kotlin/dk/cachet/carp/data/application/SyncPoint.kt) and reference to `TriggerConfiguration`s which requested the data to be collected.
To aggregate multiple `DataStreamSequence`'s of one or more data streams, e.g., to prepare for data upload, [`DataStreamBatch`](../carp.data.core/src/commonMain/kotlin/dk/cachet/carp/data/application/DataStreamBatch.kt) can be used.

![Data stream object model](https://i.imgur.com/6dnuynT.png)

## Application services

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.

### [`DataStreamService`](../carp.data.core/src/commonMain/kotlin/dk/cachet/carp/data/application/DataStreamService.kt)

Store and retrieve [`DataStreamPoint`](../carp.data.core/src/commonMain/kotlin/dk/cachet/carp/data/application/DataStreamPoint.kt)s for study deployments.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `openDataStreams` | Start accepting data for a specific study deployment. | manage deployment: `studyDeploymentId`| |
| `appendToDataStreams` | Append a batch of data point sequences to corresponding data streams. | in deployment: `studyDeploymentId` |  |
| `getDataStream` | Retrieve all data points in data stream that fall within the requested range. | in deployment: `dataStream.studyDeploymentId` | |
| `getDataStreamsStatus` | Retrieve status for each configured data stream in a deployment. | in deployment: `studyDeploymentId` | |
| `closeDataStreams` | Stop accepting data for specified study deployments. | manage deployment: (all) `studyDeploymentId` | |
| `removeDataStreams` | Close data streams and remove all data for specified study deployments. | manage deployment: (all) `studyDeploymentId` | | 
