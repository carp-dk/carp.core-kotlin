package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable

/**
 * Enumeration of supported data source types.
 * Used to distinguish between different storage and retrieval mechanisms.
 */
@Serializable
enum class DataSourceType {
    /** Local or network file system storage */
    FILE_SYSTEM,

    /** HTTP/HTTPS URL-based sources */
    HTTP,

    /** Database storage (relational or NoSQL) */
    DATABASE,

    /** In-memory data registry */
    IN_MEMORY,

    /** REST API or other web service endpoints */
    API,

    /** Streaming data sources */
    STREAM
}

/**
 * Enumeration of supported data destination types.
 * Used to specify where processed data should be written.
 */
@Serializable
enum class DestinationType {
    /** Local or network file system storage */
    FILE_SYSTEM,

    /** In-memory data registry */
    REGISTRY,

    /** Database storage */
    DATABASE,

    /** REST API or other web service endpoints */
    API,

    /** Streaming data destinations */
    STREAM
}

