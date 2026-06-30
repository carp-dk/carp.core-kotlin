package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.serialization.CLASS_DISCRIMINATOR
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


/**
 * A builder to help migrate a [JsonObject] part of an application service API
 * from [minimumMinorVersion] to [targetMinorVersion].
 */
class ApiJsonObjectMigrationBuilder(
    jsonObject: Map<String, JsonElement>,
    val minimumMinorVersion: Int,
    val targetMinorVersion: Int
)
{
    val json: MutableMap<String, JsonElement> = jsonObject.toMutableMap()

    init
    {
        // When API version is included, it always needs to be migrated.
        val version: JsonElement? = json[ API_VERSION_FIELD ]
        if ( version != null )
        {
            json[ API_VERSION_FIELD ] = version.replaceString( ".$minimumMinorVersion", ".$targetMinorVersion" )
        }
    }


    /**
     * Run a [migration] on this object in case it is a polymorphic type identified by [classDiscriminator].
     */
    fun ifType( classDiscriminator: String, migration: ApiJsonObjectMigrationBuilder.() -> Unit )
    {
        if ( json.getType() == classDiscriminator ) apply( migration )
    }

    /**
     * Change the type of this object to [newClassDiscriminator].
     */
    fun changeType( newClassDiscriminator: String )
    {
        json[ CLASS_DISCRIMINATOR ] = JsonPrimitive( newClassDiscriminator )
    }

    /**
     * Retrieve the object identified by [fieldName] in this object, and run the specified [migration].
     */
    fun updateObject( fieldName: String, migration: ApiJsonObjectMigrationBuilder.() -> Unit )
    {
        val o = requireNotNull( json[ fieldName ] as? JsonObject )
        val newJson: JsonObject = ApiJsonObjectMigrationBuilder( o, minimumMinorVersion, targetMinorVersion )
            .apply( migration ).build()
        json[ fieldName ] = newJson
    }

    /**
     * Retrieve the object identified by [fieldName] in this object, and if present, run the specified [migration].
     */
    fun updateOptionalObject( fieldName: String, migration: ApiJsonObjectMigrationBuilder.() -> Unit )
    {
        val o = json[ fieldName ] as? JsonObject ?: return
        val newJson: JsonObject = ApiJsonObjectMigrationBuilder( o, minimumMinorVersion, targetMinorVersion )
            .apply( migration ).build()
        json[ fieldName ] = newJson
    }

    /**
     * Retrieve the array identified by [fieldName] in this object, and run the specified [migration].
     */
    fun updateArray( fieldName: String, migration: ApiJsonArrayMigrationBuilder.() -> Unit )
    {
        val a = requireNotNull( json[ fieldName ] as? JsonArray )
        val newJson: JsonArray = ApiJsonArrayMigrationBuilder( a, minimumMinorVersion, targetMinorVersion )
            .apply( migration ).build()
        json[ fieldName ] = newJson
    }

    /**
     * Copy the value of the field with [fromFieldName] to the field with [toFieldName].
     */
    fun copyField( fromFieldName: String, toFieldName: String )
    {
        val from = requireNotNull( json[ fromFieldName ] )
        json[ toFieldName ] = from
    }

    private fun JsonElement.replaceString( oldValue: String, newValue: String ): JsonPrimitive
    {
        require( this is JsonPrimitive && this.isString )
        return JsonPrimitive( content.replace( oldValue, newValue ) )
    }

    fun build(): JsonObject = JsonObject( json.toMap() )
}
