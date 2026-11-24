package dk.cachet.carp.analytics.domain.environment

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.PolymorphicSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Tests for the concrete [EnvironmentDefinition] types.
 */
class EnvironmentDefinitionTest
{
    private val json = createDefaultJSON( CoreAnalyticsSerializer.module )
    private val serializer = PolymorphicSerializer( EnvironmentDefinition::class )

    @Test
    fun all_environment_types_are_registered_for_serialization()
    {
        val environments: List<EnvironmentDefinition> = listOf(
            CondaEnvironmentDefinition( UUID.randomUUID(), "conda", listOf( "numpy" ), mapOf( "A" to "1" ) ),
            DockerEnvironmentDefinition( UUID.randomUUID(), "docker", image = "python:3.11" ),
            PixiEnvironmentDefinition(
                UUID.randomUUID(),
                "pixi",
                featureDeps = mapOf( "test" to listOf( "pytest" ) ),
                lockFile = "pixi.lock"
            ),
            REnvironmentDefinition( UUID.randomUUID(), "r", "4.3.1", listOf( "dplyr" ), "renv.lock" ),
            SystemEnvironmentDefinition( UUID.randomUUID(), "system" )
        )

        environments.forEach { environment ->
            val serialized = json.encodeToString( serializer, environment )
            assertEquals( environment, json.decodeFromString( serializer, serialized ) )
        }
    }

    @Test
    fun valid_r_versions_pass()
    {
        listOf( "4", "4.3", "4.3.1" ).forEach {
            assertTrue( REnvironmentDefinition( UUID.randomUUID(), "r", it ).validate().isEmpty() )
        }
    }

    @Test
    fun invalid_r_versions_fail()
    {
        assertEquals( 2, REnvironmentDefinition( UUID.randomUUID(), "r", "" ).validate().size )
        assertEquals( 1, REnvironmentDefinition( UUID.randomUUID(), "r", "4.x" ).validate().size )
        assertEquals( 1, REnvironmentDefinition( UUID.randomUUID(), "r", "4.3.1.2" ).validate().size )
    }
}
