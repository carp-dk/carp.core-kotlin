package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.ApplicationServiceInfo
import kotlin.test.*


internal val exampleApplicationServiceRequests: Map<ApplicationServiceInfo, List<ExampleRequest>> =
    applicationServices.associateWith { generateExampleRequests( it ) }


class GenerateExampleRequestsTest
{
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
