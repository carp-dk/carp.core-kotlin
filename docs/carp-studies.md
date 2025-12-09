# carp.studies [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.studies/carp.studies.core/badge.svg)](https://mvnrepository.com/artifact/dk.cachet.carp.studies) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.studies/carp.studies.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/studies/) 

Supports management of research studies, including the recruitment of participants and assigning metadata (e.g., contact information).
This subsystem maps pseudonymized data (managed by the 'deployments' subsystem) to actual participants.

## Study and participant group state

Most of [the `StudyService` endpoints](#application-services) return the current status of the study after the requested operation has executed.
Depending on the current state of the study, different operations are available. This is represented by [`StudyStatus`](../carp.studies.core/src/commonMain/kotlin/dk/cachet/carp/studies/application/StudyStatus.kt).

When a study is first created, you can configure it (`StudyStatus.Configuring`), e.g., set a description and study protocol.
Once the study goes live (`StudyService.goLive()`), it can be deployed to participant groups (`StudyStatus.Live`),
but, the study description and protocol can no longer be modified; they are "locked in".

Participant groups are managed through `RecruitmentService`. The status of participant groups is represented by [`ParticipantGroupStatus`](../carp.studies.core/src/commonMain/kotlin/dk/cachet/carp/studies/application/users/ParticipantGroupStatus.kt),
which reflects the underlying state machine:

![Participant group state machine](https://i.imgur.com/VIv3HKk.png)

Note: The legacy `inviteNewParticipantGroup` is still available to create and invite in one step, but planned to be removed in the future.

Once a participant group is `InDeployment`, the state of the underlying `studyDeploymentStatus` determines the concrete `ParticipantGroupStatus`.
Calling `RecruitmentService.stopParticipantGroup()` will stop the underlying deployment, but the deployment can also be stopped by participants in the study.
More detailed information about the study deployment process, e.g., the remaining devices to register, can be retrieved through `studyDeploymentStatus`.

## Application services

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.
New users that are allowed to add studies should be given a 'study owner' claim, e.g., their user ID.
In case you want to support organizations this could be the ID of the organization they belong to.

### [`StudyService`](../carp.studies.core/src/commonMain/kotlin/dk/cachet/carp/studies/application/StudyService.kt)
Allows creating and managing studies.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `createStudy` | Create a new study for a specified owner. | study owner: `owner.id` | manage study: `studyId` |
| `setInternalDescription` | Set study details which are visible only to the owner. | manage study: `studyId` | |
| `getStudyDetails` | Gets detailed information about a study, including which study protocol is set. | manage study: `studyId` | |
| `getStudyStatus` | Get the status for a study. | manage study: `studyId` | |
| `getStudiesOverview` | Get status for all studies created by the specified owner. | study owner: `owner.id` | |
| `setInvitation` | Specify an invitation, shared with participants once they are invited to a study. | manage study: `studyId` | |
| `setProtocol` | Specify the study protocol to use for a study. | manage study: `studyId` | |
| `removeProtocol` | Remove the currently set study protocol. | manage study: `studyId` | |
| `goLive` | Lock in the current study protocol so that a study may be deployed to participants. | manage study: `studyId` | |
| `remove` | Remove a study and all related data. | manage study: `studyId` | |

### [`RecruitmentService`](../carp.studies.core/src/commonMain/kotlin/dk/cachet/carp/studies/application/RecruitmentService.kt)

Allows setting recruitment goals, adding participants to studies, and creating deployments for them.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `addParticipant` | Add a participant identified by a specified email address or username to a study. | manage study: `studyId` | |
| `getParticipant` | Returns the participant with a specified ID for a study. | manage study: `studyId` | |
| `getParticipants` | Get all participants for a study. | manage study: `studyId` | |
| `inviteNewParticipantGroup` | (Deprecated) Create and instantly invite a group of previously added participants to a study. | manage study: `studyId` | |
| `createParticipantGroup` | Prepare a group of previously added participants for deployment without inviting them yet. | manage study: `studyId` | |
| `inviteParticipantGroup` | Invite a previously prepared participant group to start participating in the study. | manage study: `studyId` | |
| `getParticipantGroupStatusList` | Get the status of all participant groups in a study. | manage study: `studyId` | |
| `stopParticipantGroup` | Stop the study deployment in a study of a participant group. No further changes to this deployment will be allowed and no more data will be collected. | manage study: `studyId` | |