rootProject.name = "carp.core-kotlin"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include( "carp.analytics.core" )
include( "carp.common" )
include( "carp.common.test" )
include( "carp.test" )
include( "carp.detekt" )
include( "carp.protocols.core" )
include( "carp.data.core" )
include( "carp.studies.core" )
include( "carp.deployments.core" )
include( "carp.clients.core" )
include( "rpc" )
include( "publish-npm-packages" )
