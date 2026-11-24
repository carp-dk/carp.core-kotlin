@file:Suppress("detekt:IgnoredReturnValue")

package dk.cachet.carp.analytics.infrastructure.serialization

import dk.cachet.carp.analytics.domain.environment.CondaEnvironmentDefinition
import dk.cachet.carp.analytics.domain.environment.DockerEnvironmentDefinition
import dk.cachet.carp.analytics.domain.environment.EnvironmentDefinition
import dk.cachet.carp.analytics.domain.environment.PixiEnvironmentDefinition
import dk.cachet.carp.analytics.domain.environment.REnvironmentDefinition
import dk.cachet.carp.analytics.domain.environment.SystemEnvironmentDefinition
import dk.cachet.carp.analytics.domain.tasks.ArgToken
import dk.cachet.carp.analytics.domain.tasks.CommandTaskDefinition
import dk.cachet.carp.analytics.domain.tasks.InputRef
import dk.cachet.carp.analytics.domain.tasks.Literal
import dk.cachet.carp.analytics.domain.tasks.Module
import dk.cachet.carp.analytics.domain.tasks.OutputRef
import dk.cachet.carp.analytics.domain.tasks.ParamRef
import dk.cachet.carp.analytics.domain.tasks.PythonEntryPoint
import dk.cachet.carp.analytics.domain.tasks.PythonTaskDefinition
import dk.cachet.carp.analytics.domain.tasks.RTaskDefinition
import dk.cachet.carp.analytics.domain.tasks.Script
import dk.cachet.carp.analytics.domain.tasks.TaskDefinition
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowComponent
import dk.cachet.carp.common.infrastructure.serialization.CLASS_DISCRIMINATOR
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Polymorphic serializers for the workflow definition: tasks, entry points and argument tokens.

 */
object CoreAnalyticsSerializer
{
    val module: SerializersModule = SerializersModule {
        polymorphic(EnvironmentDefinition::class) {
            subclass(CondaEnvironmentDefinition::class)
            subclass(DockerEnvironmentDefinition::class)
            subclass(PixiEnvironmentDefinition::class)
            subclass(REnvironmentDefinition::class)
            subclass(SystemEnvironmentDefinition::class)
        }
        polymorphic(WorkflowComponent::class) {
            subclass(Step::class)
            subclass(Workflow::class)
        }
        polymorphic(TaskDefinition::class) {
            subclass(CommandTaskDefinition::class)
            subclass(PythonTaskDefinition::class)
            subclass(RTaskDefinition::class)
        }

        polymorphic(PythonEntryPoint::class) {
            subclass(Script::class)
            subclass(Module::class)
        }

        polymorphic(ArgToken::class) {
            subclass(Literal::class)
            subclass(InputRef::class)
            subclass(OutputRef::class)
            subclass(ParamRef::class)
        }
    }

    val json: Json = Json {
        serializersModule = module
        classDiscriminator = CLASS_DISCRIMINATOR
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
}
