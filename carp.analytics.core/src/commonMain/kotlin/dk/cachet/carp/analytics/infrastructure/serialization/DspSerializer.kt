package dk.cachet.carp.analytics.infrastructure.serialization

import dk.cachet.carp.analytics.application.plan.CommandSpec
import dk.cachet.carp.analytics.application.plan.InTasksRun
import dk.cachet.carp.analytics.application.plan.TasksRun
import dk.cachet.carp.analytics.domain.tasks.CommandTaskDefinition
import dk.cachet.carp.analytics.domain.tasks.PythonTaskDefinition
import dk.cachet.carp.analytics.domain.tasks.TaskDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

object DspSerializer
{
    val module: SerializersModule = SerializersModule {
        polymorphic(TasksRun::class) {
            subclass(InTasksRun::class)
            subclass(CommandSpec::class)
        }

        polymorphic(TaskDefinition::class) {
            subclass(CommandTaskDefinition::class)
            subclass(PythonTaskDefinition::class)
        }
    }

    val json: Json = Json {
        serializersModule = module
        classDiscriminator = "type"
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
}
