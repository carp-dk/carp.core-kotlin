package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.ApplicationServiceInfo
import dk.cachet.carp.common.application.services.ApplicationService
import org.reflections.Reflections
import kotlin.test.*


internal val exampleApplicationServiceRequests: Map<ApplicationServiceInfo, List<ExampleRequest>> =
    applicationServices.associateWith { generateExampleRequests( it ) }


class GenerateExampleRequestsTest
{

    @Test
    fun printDiscoveredServices()
    {
        val discovered = Reflections("dk.cachet.carp")
            .getSubTypesOf(ApplicationService::class.java)
            .filter { it.isInterface }

        println("!! Discovered interfaces:")
        discovered.forEach {
            println(" - ${it.name}")
            try
            {
                ApplicationServiceInfo.of(it)
                println("   :: Valid service")
            }
            catch ( e: Exception )
            {
                println("   :: Invalid service: ${e.message}")
            }
        }
    }


    @Test
    fun can_find_application_services()
    {
        assertFalse( applicationServices.isEmpty() )
    }

    @Test
    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    fun generateExampleRequests_always_generates_same_JSON()
    {
        applicationServices.forEach { service ->
            val exampleRequests = checkNotNull( exampleApplicationServiceRequests[ service ] )
            val firstRun = exampleRequests.associateBy { it.method }
            val secondRun = generateExampleRequests( service ).associateBy { it.method }

            firstRun.forEach { (method, firstExample) ->
                val secondExample = secondRun[ method ]
                assertNotNull( secondExample )

                assertTrue(
                    firstExample.requestObject == secondExample.requestObject,
                    "The request example generated for \"$method\" isn't deterministic."
                )
                assertTrue(
                    firstExample.response == secondExample.response,
                    "The response example generated for \"$method\" isn't deterministic."
                )
            }
        }
    }
}
