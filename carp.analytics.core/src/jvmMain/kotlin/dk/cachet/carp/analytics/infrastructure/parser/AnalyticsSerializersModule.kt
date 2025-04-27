package dk.cachet.carp.analytics.infrastructure.parser

import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess
import dk.cachet.carp.analytics.domain.process.ExternalProcess
import dk.cachet.carp.analytics.domain.process.PythonExternalProcess
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowComponent
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val AnalyticsSerializersModule = SerializersModule {
    polymorphic(ExternalProcess::class) {
        subclass(CommandLineExternalProcess::class)
        subclass(PythonExternalProcess::class)
    }
    polymorphic(WorkflowComponent::class) {
        subclass(Step::class)
        subclass(Workflow::class)
    }
}
