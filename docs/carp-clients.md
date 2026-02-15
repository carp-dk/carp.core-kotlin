# carp.clients [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.clients/carp.clients.core)](https://central.sonatype.com/artifact/dk.cachet.carp.clients/carp.clients.core) [![Snapshot](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdk%2Fcachet%2Fcarp%2Fclients%2Fcarp.clients.core%2Fmaven-metadata.xml)](https://central.sonatype.com/repository/maven-snapshots/dk/cachet/carp/clients/carp.clients.core/maven-metadata.xml) 

This is the runtime which performs the actual data collection on a device (e.g., desktop computer or smartphone).
This subsystem contains reusable components which understand the runtime configuration derived from a study protocol by the ‘deployment’ subsystem.
Integrations with sensors are loaded through a 'device data collector' plug-in system to decouple sensing—not part of core—from sensing logic.

[`ClientManager`](../carp.clients.core/src/commonMain/kotlin/dk/cachet/carp/clients/application/ClientManager.kt) is the main entry point into this subsystem.
Concrete devices extend on it, e.g., [`SmartphoneClient`](../carp.clients.core/src/commonMain/kotlin/dk/cachet/carp/clients/domain/SmartphoneClient.kt) manages data collection on a smartphone.

## Study state

[`StudyStatus`](../carp.clients.core/src/commonMain/kotlin/dk/cachet/carp/clients/application/study/StudyStatus.kt) represents the status of a single study which runs on `ClientManager`.

![Study state machine](https://i.imgur.com/fi348XB.png)