@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlin.js.JsExport
import kotlin.js.JsName


/**
 * A sequence of consecutive [measurements] for a [dataStream] starting from [firstSequenceId]
 * which all share the same [triggerIds] and [syncPoint].
 */
@JsExport
sealed interface DataStreamSequence<TData : Data> : Sequence<DataStreamPoint<TData>>
{
    val dataStream: DataStreamId
    val firstSequenceId: Long
    val measurements: List<Measurement<TData>>
    val triggerIds: List<Int>
    val syncPoint: SyncPoint

    /**
     * The current range of sequence IDs in the data stream covered by this [DataStreamSequence].
     */
    val range: LongRange get() =
        if ( measurements.isEmpty() ) LongRange.EMPTY
        else firstSequenceId until firstSequenceId + measurements.size


    /**
     * @throws IllegalStateException when the current state of this [DataStreamSequence] violates interface constraints.
     */
    fun throwIfIllegalState()
    {
        check( firstSequenceId >= 0 ) { "Sequence ID must be positive." }
        check( triggerIds.isNotEmpty() )
            { "Data always needs to be linked to at least one trigger that requested it." }
        check( measurements.all { it.dataType == dataStream.dataType } )
            { "Measurements all need to correspond to the data type of the data stream." }
    }

    /**
     * Get an iterator to iterate over [DataStreamPoint]s contained in this sequence.
     */
    override fun iterator(): Iterator<DataStreamPoint<TData>> =
        measurements.asSequence().mapIndexed { index, measurement ->
            DataStreamPoint(
                firstSequenceId + index,
                dataStream.studyDeploymentId,
                dataStream.deviceRoleName,
                measurement,
                triggerIds,
                syncPoint
            )
        }.iterator()

    /**
     * Determines whether [sequence] is a sequence for the same data stream with matching [triggerIds] and [syncPoint]
     * and immediately follows the last data point in this sequence.
     */
    fun isImmediatelyFollowedBy( sequence: DataStreamSequence<*> ): Boolean =
        dataStream == sequence.dataStream &&
        triggerIds == sequence.triggerIds &&
        syncPoint == sequence.syncPoint &&
        if ( range == LongRange.EMPTY ) firstSequenceId == sequence.firstSequenceId
        else range.last + 1 == sequence.firstSequenceId


    /**
     * Returns a new [MutableDataStreamSequence], containing all the measurements of this sequence.
     */
    fun toMutableDataStreamSequence(): MutableDataStreamSequence<TData>
    {
        val sequence = MutableDataStreamSequence<TData>(
            dataStream,
            firstSequenceId,
            triggerIds,
            syncPoint
        )
        sequence.appendMeasurements( measurements )

        return sequence
    }
}


/**
 * A mutable sequence of consecutive [measurements] for a [dataStream] starting from [firstSequenceId]
 * which all share the same [triggerIds] and [syncPoint].
 */
@JsExport
class MutableDataStreamSequence<TData : Data>(
    override val dataStream: DataStreamId,
    override val firstSequenceId: Long,
    triggerIds: List<Int>,
    override val syncPoint: SyncPoint = SyncPoint.UnixEpoch
) : DataStreamSequence<TData>
{
    override val triggerIds: List<Int> = triggerIds.toList()

    private val _measurements: MutableList<Measurement<TData>> = mutableListOf()
    override val measurements: List<Measurement<TData>>
        get() = _measurements

    init { throwIfIllegalInitialization() }


    /**
     * Append [measurements] to the end of this sequence.
     *
     * @throws IllegalArgumentException when any of the [measurements] is of a different data type than [dataStream].
     */
    @JsName( "appendMeasurementsList" )
    fun appendMeasurements( measurements: List<Measurement<TData>> )
    {
        require( measurements.all { it.dataType == dataStream.dataType } )
            { "Measurements all need to correspond to the data type of the data stream." }

        _measurements.addAll( measurements )
    }

    /**
     * Append [measurements] to the end of this sequence.
     *
     * @throws IllegalArgumentException when any of the [measurements] is of a different data type than [dataStream].
     */
    fun appendMeasurements( vararg measurements: Measurement<TData> ) = appendMeasurements( measurements.toList() )

    /**
     * Append all measurements of [sequence] to this sequence.
     *
     * @throws IllegalArgumentException when [sequence] doesn't match this sequence or doesn't immediately follow
     *   the last data point in this sequence.
     */
    fun appendSequence( sequence: DataStreamSequence<TData> )
    {
        require( isImmediatelyFollowedBy( sequence ) )
            { "Sequence doesn't match or doesn't immediately follow the last data point." }

        appendMeasurements( sequence.measurements )
    }

    override fun equals( other: Any? ): Boolean = equalsOther( other )
    override fun hashCode(): Int = measurements.hashCode()
}


/**
 * Serializer for any [DataStreamSequence], which doesn't guarantee the concrete type is retained.
 */
object DataStreamSequenceSerializer : KSerializer<DataStreamSequence<*>>
{
    @Serializable
    class DataStreamSequenceSnapshot internal constructor(
        override val dataStream: DataStreamId,
        override val firstSequenceId: Long,
        override val measurements: List<Measurement<Data>>,
        override val triggerIds: List<Int>,
        override val syncPoint: SyncPoint = SyncPoint.UnixEpoch
    ) : DataStreamSequence<Data>
    {
        init { throwIfIllegalInitialization() }

        override fun equals( other: Any? ): Boolean = equalsOther( other )
        override fun hashCode(): Int = measurements.hashCode()
    }

    private val serializer = DataStreamSequenceSnapshot.serializer()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize( encoder: Encoder, value: DataStreamSequence<*> ) =
        encoder.encodeSerializableValue(
            serializer,
            DataStreamSequenceSnapshot(
                value.dataStream,
                value.firstSequenceId,
                value.measurements,
                value.triggerIds,
                value.syncPoint
            )
        )

    override fun deserialize( decoder: Decoder ): DataStreamSequence<*> = decoder.decodeSerializableValue( serializer )
}

private fun DataStreamSequence<*>.throwIfIllegalInitialization() =
    try { throwIfIllegalState() }
    catch ( ex: IllegalStateException ) { throw IllegalArgumentException( ex ) }

private fun DataStreamSequence<*>.equalsOther( other: Any? ): Boolean
{
    if ( this === other ) return true
    if ( other !is DataStreamSequence<*> ) return false

    return toList() == other.toList()
}
