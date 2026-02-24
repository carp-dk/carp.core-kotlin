package dk.cachet.carp.analytics.application.execution

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RunPolicyTest
{
    private class TestPolicy( override val timeoutMs: Long? ) : RunPolicy

    @Test
    fun `timeoutMs is returned correctly`()
    {
        val policyWithTimeout = TestPolicy(timeoutMs = 1234L)
        val policyWithoutTimeout = TestPolicy(timeoutMs = null)

        assertEquals(1234L, policyWithTimeout.timeoutMs)
        assertNull(policyWithoutTimeout.timeoutMs)
    }
}

