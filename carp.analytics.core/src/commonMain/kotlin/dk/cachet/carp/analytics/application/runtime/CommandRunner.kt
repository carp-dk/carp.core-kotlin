package dk.cachet.carp.analytics.application.runtime

import dk.cachet.carp.analytics.application.execution.RunPolicy
import dk.cachet.carp.analytics.application.plan.CommandSpec

interface CommandRunner
{
    fun run( command: CommandSpec, policy: RunPolicy ): CommandResult
}
