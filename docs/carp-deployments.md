# carp.deployments [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.deployments/carp.deployments.core)](https://central.sonatype.com/artifact/dk.cachet.carp.deployments/carp.deployments.core) [![Snapshot](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdk%2Fcachet%2Fcarp%2Fdeployments%2Fcarp.deployments.core%2Fmaven-metadata.xml)](https://central.sonatype.com/repository/maven-snapshots/dk/cachet/carp/deployments/carp.deployments.core/maven-metadata.xml) 

Maps the information specified in a study protocol to runtime configurations used by the 'clients' subystem to run the protocol on concrete devices (e.g., a smartphone) and allow researchers to monitor their state.
To start collecting data, participants need to be invited, devices need to be registered, and consent needs to be given to collect the requested data.

## Deployment sequence

The following diagram depicts a typical sequence of calls to [application services in the deployments subsystem](#application-services) to create a study deployment, invite participants, and deploy it successfully on a client device.
The events between `DeploymentService` and `ParticipationService` are opaque to the user and are merely an implementation detail.
The application and domain services in `carp.studies` and `carp.client` abstract away this entire sequence.
Matching code for these calls can be found in the main README as part of the [carp.deployments](../README.md#example-deployments) and [carp.client](../README.md#example-client) examples.

![Study deployment sequence diagram](https://i.imgur.com/5vhCqwu.png)

## Study and device deployment state

Most of the [the `DeploymentService` endpoints](#application-services) return the current status of a study deployment after the requested operation has been executed.
Depending on the current state of the deployment, different operations are available.
This is represented by [`StudyDeploymentStatus`](../carp.deployments.core/src/commonMain/kotlin/dk/cachet/carp/deployments/application/StudyDeploymentStatus.kt), which reflects the underlying state machine:

![Study deployment state machine](https://i.imgur.com/GTk5OHe.png)

The overall deployment state depends on the aggregate of individual device deployment states.
Each device within the study deployment has a corresponding [`DeviceDeploymentStatus`](../carp.deployments.core/src/commonMain/kotlin/dk/cachet/carp/deployments/application/DeviceDeploymentStatus.kt):

![Device deployment state machine](https://i.imgur.com/acxD0Vw.png)

## Application services

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.

### [`DeploymentService`](../carp.deployments.core/src/commonMain/kotlin/dk/cachet/carp/deployments/application/DeploymentService.kt)

Allows deploying study protocols to participants and retrieving primary device deployments for participating primary devices as defined in the protocol.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `createStudyDeployment` | Instantiate a study deployment for a given protocol with invited participants. | | manage deployment: `studyDeploymentId`, in deployment: `studyDeploymentId` |
| `removeStudyDeployments` | Remove study deployments and all related data to it. | manage deployment: (all) `studyDeploymentId`| |
| `getStudyDeploymentStatus` | Get the status for a study deployment. | in deployment: `studyDeploymentId` | |
| `getStudyDeploymentStatusList` | Get the status for a set of deployments. | in deployment: (all) `studyDeploymentIds` | |
| `registerDevice` | Register a device for a study deployment. | in deployment: `studyDeploymentId` | |
| `unregisterDevice` | Unregister a device for a study deployment. | in deployment: `studyDeploymentId` | |
| `getDeviceDeploymentFor` | Get the deployment configuration for a primary device in a study deployment. | in deployment: `studyDeploymentId` | |
| `deviceDeployed` | Indicate to stakeholders in a study deployment that a primary device was deployed successfully, i.e., that the study deployment was loaded on the device and that the necessary runtime is available to run it. | in deployment: `studyDeploymentId` | |
| `stop` | Stop a study deployment. No further changes to this deployment will be allowed and no more data will be collected. | in deployment: `studyDeploymentId` | |

### [`ParticipationService`](../carp.deployments.core/src/commonMain/kotlin/dk/cachet/carp/deployments/application/ParticipationService.kt)

Allows retrieving participations for study deployments,
and managing data related to participants which is input by users.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `getActiveParticipationInvitations` | Get all participations of active study deployments a specified account has been invited to. | authenticated: `accountId` | |
| `getParticipantData` | Get currently set data for all expected participant data. | in deployment: `studyDeploymentId` | |
| `getParticipantDataList` |  Get currently set data for all expected participant data for a set of study deployments. | in deployment: `studyDeploymentId` | |
| `setParticipantData` | Set participant data for a specified study deployment. | in deployment: `studyDeploymentId` | |
