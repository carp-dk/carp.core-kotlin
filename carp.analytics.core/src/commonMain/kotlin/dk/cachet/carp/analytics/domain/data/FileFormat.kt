package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable

// FILE FORMAT ENUM


/**
 * File format enumeration.
 *
 * Specifies the serialization format of data for parsing/writing.
 *
 * Each enum value has:
 * - `mimeType`: Standard MIME type
 * - `extension`: File extension (without dot)
 * - `isBinary`: Whether binary or text format
 */
@Serializable
enum class FileFormat(
    val mimeType: String,
    val extension: String,
    val isBinary: Boolean
)
{
    // Text Formats
    TXT( "text/plain", "txt", false ),

    // Tabular Formats
    CSV( "text/csv", "csv", false ),
    TSV( "text/tab-separated-values", "tsv", false ),
    EXCEL( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx", true ),

    // Semi-Structured Formats
    JSON( "application/json", "json", false ),
    XML( "application/xml", "xml", false ),
    YAML( "application/x-yaml", "yaml", false ),

    // Binary Columnar Formats
    PARQUET( "application/octet-stream", "parquet", true ),
    AVRO( "application/octet-stream", "avro", true ),

    // Generic Binary
    BINARY( "application/octet-stream", "bin", true ),

    // Generic/Unknown Formats
    UNKNOWN( "application/octet-stream", "", false );

    /**
     * Check if this format is text-based.
     */
    val isText: Boolean
        get() = !isBinary
}


/**
 * Enumeration of write modes for data destinations.
 */
@Serializable
enum class WriteMode
{
    /** Append new data to existing data */
    APPEND,

    /** Overwrite existing data */
    OVERWRITE,

    /** Fail if data already exists */
    ERROR_IF_EXISTS,

    /** Create new file with unique name if exists */
    CREATE_NEW
}

