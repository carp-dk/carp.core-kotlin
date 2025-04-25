package dk.cachet.carp.analytics.infrastructure.parser

import dk.cachet.carp.analytics.domain.process.CommandLineProcess
import dk.cachet.carp.analytics.domain.process.Process
import dk.cachet.carp.analytics.domain.process.PythonProcess
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowComponent
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val AnalyticsSerializersModule = SerializersModule {
    polymorphic(Process::class) {
        subclass(CommandLineProcess::class)
        subclass(PythonProcess::class)
    }
    polymorphic(WorkflowComponent::class) {
        subclass(Step::class)
        subclass(Workflow::class)
    }
}
