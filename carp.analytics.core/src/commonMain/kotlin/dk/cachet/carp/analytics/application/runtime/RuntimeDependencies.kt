package dk.cachet.carp.analytics.application.runtime

import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.analytics.application.data.DataRegistry

/**
 * Singleton object defining all supported runtime dependency keys for injection
 * into workflow processes and services.
 *
 * These keys should be used to populate and query runtime injection maps.
 */
object RuntimeDependencies {
    /** Key for injecting the StudyDataService. */
    val StudyDataService = RuntimeDependencyKey<DataStreamService>("DataStreamService")

    /** Key for injecting the DataRegistry. */
    val DataRegistry = RuntimeDependencyKey<DataRegistry>("dataRegistry")
}
