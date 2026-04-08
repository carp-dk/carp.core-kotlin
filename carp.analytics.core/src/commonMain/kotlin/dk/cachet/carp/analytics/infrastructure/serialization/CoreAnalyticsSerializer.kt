@file:Suppress("detekt:IgnoredReturnValue")

package dk.cachet.carp.analytics.infrastructure.serialization

import dk.cachet.carp.analytics.application.plan.CommandSpec
import dk.cachet.carp.analytics.application.plan.InTasksRun
import dk.cachet.carp.analytics.application.plan.TasksRun
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

object CoreAnalyticsSerializer
{
    val module: SerializersModule = SerializersModule {
        polymorphic(TasksRun::class) {
            subclass(InTasksRun::class)
            subclass(CommandSpec::class)
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
        classDiscriminator = "type"
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
}
