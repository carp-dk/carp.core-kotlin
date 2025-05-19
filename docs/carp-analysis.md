# carp.analytics

TODO: Introductory description.

## Domain Model

TODO

## Application services

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.

### [`TriggersService`](../carp.analytics.core/src/commonMain/kotlin/dk/cachet/carp/analytics/application/TriggersService.kt)

Manages triggers that automatically or manually initiate workflow executions.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `createTrigger` | Register a new trigger for a study workflow. | manage study: `studyId` | |
| `updateTrigger` | Update the configuration of an existing trigger. | manage study: `studyId` | |
| `deleteTrigger` | Remove a registered trigger by its ID. | manage study: `studyId` | |
| `getTrigger` | Retrieve details for a specific trigger. | in study: `studyId` | |
| `listTriggers` | List all triggers for a given study. | in study: `studyId` | |
| `startTrigger` | Manually start a trigger instance. | in study: `studyId` | |
| `endTrigger` | End an active trigger instance. | in study: `studyId` | |

### [`WorkflowService`](../carp.analytics.core/src/commonMain/kotlin/dk/cachet/carp/analytics/application/WorkflowService.kt)

Create, manage, and retrieve data analysis workflows.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `createWorkflow` | Create a new workflow definition for a study. | manage study: `studyId` | |
| `updateWorkflow` | Update a workflow using its metadata. | manage study: `studyId` | |
| `getWorkflow` | Retrieve a specific workflow definition. | in study: `studyId` | |
| `deleteWorkflow` | Delete a workflow from a study. | manage study: `studyId` | |
| `listWorkflows` | List all workflows defined in a study. | in study: `studyId` | |

### [`ExecutionService`](../carp.analytics.core/src/commonMain/kotlin/dk/cachet/carp/analytics/application/ExecutionService.kt)

Execute and monitor workflow runs triggered by defined schedules, data events, or manual invocations.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `executeWorkflow` | Trigger execution of a registered workflow. | in study: `studyId` | |
| `executeWorkflowFromDefinition` | Execute a workflow defined on the fly. | in study: `studyId` | |
| `getExecutionState` | Get the current status of a workflow execution. | in study: `studyId` | |
| `getExecutionResult` | Retrieve the results of a completed execution. | in study: `studyId` | |
| `findExecutions` | Find workflow executions within a time range. | in study: `studyId` | |
| `getLatestExecutionStatus` | Check the most recent execution state for a workflow. | in study: `studyId` | |

## Serialization

TODO

## JSON-RPC

TODO

## Schema

TODO
