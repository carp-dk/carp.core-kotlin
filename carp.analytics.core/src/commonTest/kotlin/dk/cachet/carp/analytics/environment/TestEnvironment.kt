package dk.cachet.carp.analytics.environment

import dk.cachet.carp.analytics.domain.environment.Environment

class TestEnvironment( override val name: String = "TestEnvironment", override val dependencies: List<String> ) :
    Environment
