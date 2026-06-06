package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.serialization.CustomData
import dk.cachet.carp.data.infrastructure.getDataType
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlin.js.JsExport


/**
 * The result of a measurement of [data] of a given [dataType] at a specific point or interval in time.
 * When [sensorEndTime] is set, the [data] pertains to an interval in time; otherwise, a point in time.
 *
 * The unit of [sensorStartTime] and [sensorEndTime] is fully determined by the sensor that collected the data.
 * For example, it could be a simple clock increment since the device powered up.
 */
@Serializable( MeasurementSerializer::class )
@JsExport
data class Measurement<out TData : Data>(
    val sensorStartTime: Long,
    val sensorEndTime: Long?,
    val dataType: DataType,
    val data: TData
)
{
    init
    {
        if ( sensorEndTime != null )
        {
            require( sensorEndTime >= sensorStartTime ) { "If set, sensorEndTime needs to lie after sensorStartTime." }
        }
    }

    /**
     * Determines whether the measured data pertains to a [DataTimeType.POINT] in time or a [DataTimeType.TIME_SPAN].
     */
    fun getDataTimeType(): DataTimeType =
        if ( sensorEndTime == null ) DataTimeType.POINT
        else DataTimeType.TIME_SPAN

    /**
     * Convert this [Measurement] to one synchronized using [syncPoint].
     */
    fun synchronize( syncPoint: SyncPoint ): Measurement<TData> =
        copy(
            sensorStartTime = syncPoint.applyToTimestamp( sensorStartTime ),
            sensorEndTime =
                if ( sensorEndTime == null ) sensorEndTime
                else syncPoint.applyToTimestamp( sensorEndTime )
        )
}


/**
 * A custom serializer for [Measurement] which omits the data type information as it is already
 * part of polymorphic serialization of the measured data.
 * In addition, it supports serializing star-projected `Measurement<*>` types.
 */
object MeasurementSerializer : KSerializer<Measurement<Data>>
{
    @Serializable
    private class Surrogate( val sensorStartTime: Long, val sensorEndTime: Long? = null, val data: Data )

    override val descriptor: SerialDescriptor = Surrogate.serializer().descriptor


    override fun serialize( encoder: Encoder, value: Measurement<Data> )
    {
        val surrogate = Surrogate( value.sensorStartTime, value.sensorEndTime, value.data )
        encoder.encodeSerializableValue( Surrogate.serializer(), surrogate )
    }

    override fun deserialize( decoder: Decoder ): Measurement<Data>
    {
        val surrogate = decoder.decodeSerializableValue( Surrogate.serializer() )
        val data = surrogate.data

        // Data type can only be derived from serializers of types known at runtime,
        // but for unknown types, it can be extracted from the wrapper class (`CustomData`).
        val dataType =
            if ( data is CustomData ) DataType.fromString( data.className )
            else getDataType( data::class )

        return Measurement( surrogate.sensorStartTime, surrogate.sensorEndTime, dataType, data )
    }
}
