package dk.cachet.carp.analytics.infrastructure.parser

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration

val WorkflowYaml = Yaml(
    serializersModule = AnalyticsSerializersModule,
    configuration = YamlConfiguration(
        encodeDefaults = true,
        strictMode = false
    )
)
