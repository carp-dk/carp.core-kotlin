package dk.cachet.carp.analytics.infrastructure.parser

import dk.cachet.carp.analytics.application.process.DataRetrievalProcess
import dk.cachet.carp.analytics.application.process.MeanDailyStepCountProcess
import dk.cachet.carp.analytics.domain.environment.Environment
import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess
import dk.cachet.carp.analytics.domain.process.ExternalProcess
import dk.cachet.carp.analytics.domain.process.PythonExternalProcess
import dk.cachet.carp.analytics.domain.process.WorkflowProcess
import dk.cachet.carp.analytics.domain.trigger.ManualTrigger
import dk.cachet.carp.analytics.domain.trigger.ScheduledTrigger
import dk.cachet.carp.analytics.domain.trigger.Trigger
import dk.cachet.carp.analytics.domain.workflow.ComponentMetadata
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.analytics.domain.workflow.SubWorkflowMetadata
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowComponent
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.analytics.infrastructure.environment.CondaEnvironment
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val AnalyticsSerializersModule = SerializersModule {
    polymorphic(WorkflowProcess::class) {
        subclass(CommandLineExternalProcess::class)
        subclass(PythonExternalProcess::class)
        subclass(DataRetrievalProcess::class)
        subclass(MeanDailyStepCountProcess::class)

    }
    polymorphic(WorkflowComponent::class) {
        subclass(Step::class)
        subclass(Workflow::class)
    }
    polymorphic(Environment::class) {
        subclass(CondaEnvironment::class)
    }

    polymorphic(ComponentMetadata::class) {
        subclass(WorkflowMetadata::class)
        subclass(StepMetadata::class)
        subclass(SubWorkflowMetadata::class)
    }

    polymorphic(Trigger::class) {
        subclass(ScheduledTrigger::class)
        subclass(ManualTrigger::class)
    }
}
