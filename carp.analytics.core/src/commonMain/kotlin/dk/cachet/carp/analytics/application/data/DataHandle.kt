package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.data.application.CollectedDataSet

sealed interface DataHandle

data class InMemoryData(val dataset: CollectedDataSet) : DataHandle