# carp.analytics: runs and artifacts

Detail page for [carp.analytics](carp-analytics.md).
Describes what happens between starting a run and fetching its results.

## Run sequence

1. A researcher writes a workflow in YAML.
2. The client calls `validateWorkflow` while editing, and `submitWorkflow` when done. Only a workflow without errors is stored.
3. The client calls `executeWorkflow`. The call returns an `ExecutorState` straight away.
4. Clients poll `getExecutionState`, or follow the `ExecutionService` events.
5. When the run has finished, clients call `getExecutionResult` and fetch the artifacts they need by reference.

Validation runs every check that does not need compute: it resolves step library references, lints the workflow, plans the run, and checks that the study protocol collects the data the workflow asks for.
Each issue has a code, a severity and a message, so a client can show the researcher what to fix.

## Run states

| State | Meaning |
| --- | --- |
| `PENDING` | The run is accepted and waiting to start. |
| `PROVISIONING` | Environments are being installed. A first run can spend minutes here. Later runs reuse cached environments. |
| `RUNNING` | Steps are running. |
| `SUCCEEDED` | All steps finished. |
| `FAILED` | A step failed. Steps that had not started are marked `SKIPPED` in the report. |
| `CANCELLED` | The run was stopped with `cancelExecution`. |

`SKIPPED` is a step state, not a run state.

A workflow has at most one active run.
Calling `executeWorkflow` while a run is active returns that run, so a retried request is harmless.
Runs of different workflows can happen at the same time, each in its own workspace.

## Events

`ExecutionService` emits `RunStarted`, `StepStarted`, `StepCompleted`, `RunCompleted`, `RunFailed` and `RunCancelled`.
Clients that want live progress follow these. Polling `getExecutionState` is enough for everything else.

## Cancelling a run

- During provisioning, the environment install is stopped and the environment is not cached.
- During execution, the running step is stopped and no further steps start.
- Steps that already finished keep their outputs. The run is marked `CANCELLED`.

## Artifacts

The `ExecutionReport` lists, for each step, its status, its start and end time, and the artifacts it produced.
For each artifact the report holds an `ArtifactReference`: a URI, a MIME type, a size and a SHA-256 hash.

Files are not sent through the application services.
A client fetches an artifact from its URI, and can check the download against the hash.
This keeps status calls small, however large the results are.

Small typed results, such as a `SummaryStatistic` or a `Classification`, can be read directly from the report.

### Cohort and participant artifacts

Each workflow output is declared as cohort-scoped or participant-scoped.
For a participant-scoped output, the runtime asks the data source which participant each result belongs to:

- CARP data resolves to the participant's study deployment.
- Other data names its subject in a way the data source declares, for example a column or a file name pattern.

Validation checks that a participant-scoped output keeps its subject through every step before it.
A step that combines data across participants before such an output is reported as an error, before anything runs.

Researchers see all artifacts of a study.
`getParticipantResults` returns only the participant-scoped artifacts of one study deployment, so a participant's app can show them their own results.
Each artifact has a `producedAt` time. To check for new results, the app passes the latest `producedAt` it has seen as `since`: an empty list means there is nothing new, otherwise the list holds exactly the new results.
Artifacts whose subject does not come from CARP data have no study deployment and are not visible to participants.
Corrections a participant makes go back as data through [`DataStreamService`](carp-data.md#datastreamservice), not through the analytics services.

## Run bundle

Every run keeps a run folder with the workflow, the plan, the report, each step's inputs, outputs and logs, and what each environment contained.
It also has an RO-Crate (`ro-crate-metadata.json`), so the run can be checked file by file and run again.
How a client downloads the bundle is still open.
