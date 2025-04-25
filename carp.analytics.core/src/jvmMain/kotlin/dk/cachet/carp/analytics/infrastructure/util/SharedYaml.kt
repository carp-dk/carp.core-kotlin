package dk.cachet.carp.analytics.infrastructure.util

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration

val SharedYaml: Yaml = Yaml(
    configuration = YamlConfiguration(
        encodeDefaults = true,
        strictMode = false
    )
)
