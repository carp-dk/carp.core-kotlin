package dk.cachet.carp.analytics.infrastructure.serialization

import dk.cachet.carp.analytics.application.plan.CommandSpec
import dk.cachet.carp.analytics.application.plan.InTasksRun
import dk.cachet.carp.analytics.application.plan.TasksRun
import dk.cachet.carp.analytics.domain.data.ApiDestination
import dk.cachet.carp.analytics.domain.data.ApiSource
import dk.cachet.carp.analytics.domain.data.DatabaseDestination
import dk.cachet.carp.analytics.domain.data.DatabaseSource
import dk.cachet.carp.analytics.domain.data.DataDestination
import dk.cachet.carp.analytics.domain.data.DataSource
import dk.cachet.carp.analytics.domain.data.FileDestination
import dk.cachet.carp.analytics.domain.data.FileSystemSource
import dk.cachet.carp.analytics.domain.data.InMemorySource
import dk.cachet.carp.analytics.domain.data.RegistryDestination
import dk.cachet.carp.analytics.domain.data.StreamDestination
import dk.cachet.carp.analytics.domain.data.StreamSource
import dk.cachet.carp.analytics.domain.data.StepOutputSource
import dk.cachet.carp.analytics.domain.data.UrlSource
import dk.cachet.carp.analytics.domain.tasks.CommandTaskDefinition
import dk.cachet.carp.analytics.domain.tasks.PythonTaskDefinition
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
        }

        // Data sources (7 types)
        polymorphic(DataSource::class) {
            subclass(FileSystemSource::class)
            subclass(UrlSource::class)
            subclass(DatabaseSource::class)
            subclass(InMemorySource::class)
            subclass(ApiSource::class)
            subclass(StreamSource::class)
            subclass(StepOutputSource::class)
        }

        // Data destinations (5 types)
        polymorphic(DataDestination::class) {
            subclass(FileDestination::class)
            subclass(RegistryDestination::class)
            subclass(DatabaseDestination::class)
            subclass(ApiDestination::class)
            subclass(StreamDestination::class)
        }
    }

    val json: Json = Json {
        serializersModule = module
        classDiscriminator = "type"
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
}
