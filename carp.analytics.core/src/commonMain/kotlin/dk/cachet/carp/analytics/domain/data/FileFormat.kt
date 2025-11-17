package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable

/**
 * Enumeration of supported file formats for data serialization and deserialization.
 */
@Serializable
enum class FileFormat {
    /** Comma-Separated Values */
    CSV,

    /** JavaScript Object Notation */
    JSON,

    /** Apache Parquet columnar storage */
    PARQUET,

    /** Apache Avro serialization */
    AVRO,

    /** Extensible Markup Language */
    XML,

    /** Microsoft Excel spreadsheet */
    EXCEL,

    /** Generic binary format */
    BINARY,

    /** Tab-Separated Values */
    TSV,

    /** YAML Ain't Markup Language */
    YAML
}

/**
 * Enumeration of supported database types.
 */
@Serializable
enum class DatabaseType {
    /** PostgreSQL relational database */
    POSTGRESQL,

    /** MySQL relational database */
    MYSQL,

    /** SQLite embedded database */
    SQLITE,

    /** MongoDB document database */
    MONGODB,

    /** MariaDB relational database */
    MARIADB,

    /** Microsoft SQL Server */
    SQLSERVER
}

/**
 * Enumeration of write modes for data destinations.
 */
@Serializable
enum class WriteMode {
    /** Append new data to existing data */
    APPEND,

    /** Overwrite existing data */
    OVERWRITE,

    /** Fail if data already exists */
    ERROR_IF_EXISTS,

    /** Create new file with unique name if exists */
    CREATE_NEW
}

/**
 * HTTP methods for API interactions.
 */
@Serializable
enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS
}

