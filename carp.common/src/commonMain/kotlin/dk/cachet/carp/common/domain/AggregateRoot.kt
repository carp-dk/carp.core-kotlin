package dk.cachet.carp.common.domain

import dk.cachet.carp.common.application.UUID
import kotlin.time.Instant


/**
 * A root object with a unique [id] which ensures the integrity of underlying state as a whole,
 * tracks events raised from within, and for which an immutable 'snapshot' at any given moment in time can be obtained.
 */
abstract class AggregateRoot<TRoot, TSnapshot : Snapshot<TRoot>, TEvent : DomainEvent>(
    val id: UUID,
    /**
     * The date when this object was created.
     */
    val createdOn: Instant
)
{
    /**
     * If the aggregate root was loaded from a snapshot, its version; null otherwise.
     * On repository writes, this value should be used to verify whether the expected version is edited.
     */
    var fromSnapshotVersion: Int? = null
        private set

    /**
     * Specify that this aggregate root was loaded from [snapshot], which sets [fromSnapshotVersion] accordingly.
     */
    fun wasLoadedFromSnapshot( snapshot: TSnapshot )
    {
        check( fromSnapshotVersion == null )
            { "An aggregate root should only be loaded from a snapshot once." }
        check( events.size == 0 )
            { "The snapshot an aggregate root was loaded from should be set before executing any operations on it." }

        fromSnapshotVersion = snapshot.version
    }

    private val events: MutableList<TEvent> = mutableListOf()

    /**
     * Add an event triggered by this aggregate root to a queue.
     */
    protected fun event( event: TEvent ) = events.add( event )

    /**
     * In case the instance equals [value], [createEvent] and add it to the event queue of this aggregate root.
     */
    protected fun <TPredicate> TPredicate.eventIf( value: TPredicate, createEvent: () -> TEvent ): TPredicate =
        this.also { if ( this == value ) event( createEvent() ) }

    /**
     * Returns all tracked events on this aggregate root in the order they were triggered and clears the queue.
     */
    fun consumeEvents(): List<TEvent>
    {
        val toProcess = events.toList()
        events.clear()
        return toProcess
    }

    /**
     * Get an immutable snapshot of the current the state of this aggregate.
     */
    fun getSnapshot(): TSnapshot
    {
        val curSnapshotVersion = fromSnapshotVersion
        val newSnapshotVersion = if ( curSnapshotVersion == null ) 0 else curSnapshotVersion + events.size
        return getSnapshot( newSnapshotVersion )
    }

    /**
     * Get an immutable snapshot of the current state of this aggregate using the specified snapshot [version].
     */
    protected abstract fun getSnapshot( version: Int ): TSnapshot
}
