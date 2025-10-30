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
            println("Testing service: ${service.serviceName}")

            val exampleRequests = try {
                checkNotNull( exampleApplicationServiceRequests[ service ] )
            } catch (e: Exception) {
                System.err.println("Failed to get example requests for service ${service.serviceName}: ${e.message}")
                throw e
            }

            val firstRun = exampleRequests.associateBy { it.method }

            val secondRun = try {
                generateExampleRequests( service ).associateBy { it.method }
            } catch (e: Exception) {
                System.err.println("Failed to generate example requests for service ${service.serviceName}: ${e.message}")
                System.err.println("Stack trace:")
                e.printStackTrace()
                throw e
            }

            firstRun.forEach { (method, firstExample) ->
                println("  Testing method: $method")
                val secondExample = secondRun[ method ]
                assertNotNull( secondExample, "Method $method not found in second run for service ${service.serviceName}" )

                if (firstExample.requestObject != secondExample.requestObject) {
                    System.err.println("Request object mismatch for method $method in service ${service.serviceName}")
                    System.err.println("First run request: ${firstExample.requestObject}")
                    System.err.println("Second run request: ${secondExample.requestObject}")
                }

                if (firstExample.response != secondExample.response) {
                    System.err.println("Response mismatch for method $method in service ${service.serviceName}")
                    System.err.println("First run response: ${firstExample.response}")
                    System.err.println("Second run response: ${secondExample.response}")
                }

                assertTrue(
                    firstExample.requestObject == secondExample.requestObject,
                    "The request example generated for \"$method\" in service ${service.serviceName} isn't deterministic."
                )
                assertTrue(
                    firstExample.response == secondExample.response,
                    "The response example generated for \"$method\" in service ${service.serviceName} isn't deterministic."
                )
            }

            println("  ✓ Service ${service.serviceName} passed")
        }
    }
}
