package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyStatus
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * Represents a study which can be pilot tested and eventually 'go live',
 * for which a recruitment goal can be set, and participants can be recruited.
 */
class Study(
    /**
     * The ID of the entity (e.g., person or group) that created this [Study].
     */
    val ownerId: UUID,
    /**
     * A descriptive name for the study, assigned by, and only visible to, the entity with [ownerId].
     */
    name: String,
    /**
     * A description for the study, assigned by, and only visible to, the entity with [ownerId].
     */
    description: String? = null,
    /**
     * A description of the study, shared with participants once they are invited to the study.
     */
    invitation: StudyInvitation = StudyInvitation( name ),
    id: UUID = UUID.randomUUID(),
    createdOn: Instant = Clock.System.now()
) : AggregateRoot<Study, StudySnapshot, Study.Event>( id, createdOn )
{
    sealed class Event : DomainEvent
    {
        data class InternalDescriptionChanged( val name: String, val description: String? ) : Event()
        data class InvitationChanged( val invitation: StudyInvitation ) : Event()
        data class ProtocolSnapshotChanged( val protocolSnapshot: StudyProtocolSnapshot? ) : Event()
        data class StateChanged( val isLive: Boolean ) : Event()
    }


    companion object Factory
    {
        fun fromSnapshot( snapshot: StudySnapshot ): Study
        {
            val study = Study(
                snapshot.ownerId,
                snapshot.name,
                snapshot.description,
                snapshot.invitation,
                snapshot.id,
                snapshot.createdOn
            )
            study.protocolSnapshot = snapshot.protocolSnapshot
            study.isLive = snapshot.isLive

            // Events introduced by loading the snapshot are not relevant to a consumer wanting to persist changes.
            study.consumeEvents()
            study.wasLoadedFromSnapshot( snapshot )

            return study
        }
    }


    /**
     * A descriptive name for the study, assigned by, and only visible to, the entity with [ownerId].
     */
    var name: String = name
        set( value )
        {
            field = value
            event( Event.InternalDescriptionChanged( name, description ) )
        }

    /**
     * A description for the study, assigned by, and only visible to, the entity with [ownerId].
     */
    var description: String? = description
        set( value )
        {
            field = value
            event( Event.InternalDescriptionChanged( name, description ) )
        }

    val canSetInvitation: Boolean get() = !isLive

    /**
     * A description of the study, shared with participants once they are invited to the study.
     */
    var invitation: StudyInvitation = invitation
        /**
         * Set a new description of the study, to be shared with participants once they are invited to the study.
         *
         * @throws IllegalStateException when the invitation can no longer be changed since the study went 'live'.
         */
        set( value )
        {
            check( !isLive ) { "Can't change invitation since this study already went live." }

            field = value
            event( Event.InvitationChanged( invitation ) )
        }

    /**
     * Get the status (serializable) of this [Study].
     */
    fun getStatus(): StudyStatus =
        if ( isLive )
        {
            StudyStatus.Live(
                id,
                name,
                createdOn,
                protocolSnapshot?.id,
                canSetInvitation,
                canSetStudyProtocol,
                canDeployToParticipants
            )
        }
        else
        {
            StudyStatus.Configuring(
                id,
                name,
                createdOn,
                protocolSnapshot?.id,
                canSetInvitation,
                canSetStudyProtocol,
                canDeployToParticipants,
                canGoLive
            )
        }

    /**
     * Get [StudyDetails] for this [Study].
     */
    fun getStudyDetails(): StudyDetails =
        StudyDetails( id, ownerId, name, createdOn, description, invitation, protocolSnapshot )

    val canSetStudyProtocol: Boolean get() = !isLive

    /**
     * A snapshot of the protocol to use in this study, or null when not yet defined.
     */
    var protocolSnapshot: StudyProtocolSnapshot? = null
        /**
         * Set the protocol to use in this study as defined by an immutable snapshot.
         * Passing 'null' removes the assigned protocol.
         *
         * @throws IllegalStateException when the study protocol can no longer be set since the study went 'live'.
         * @throws IllegalArgumentException when:
         *   - an invalid protocol snapshot is specified
         *   - a protocol is specified which cannot be deployed (contains deployment errors)
         */
        set( value )
        {
            check( !isLive ) { "Can't set protocol since this study already went live." }
            if ( value != null )
            {
                val protocol = StudyProtocol.fromSnapshot( value )
                require( protocol.isDeployable() )
                    { "The specified protocol contains deployment errors and therefore won't be able to be deployed." }
            }

            field = value
            event( Event.ProtocolSnapshotChanged( value ) )
        }

    /**
     * Determines whether a study protocol has been locked in and the study may be deployed to real participants.
     */
    var isLive: Boolean = false
        private set

    private val canGoLive: Boolean get() = protocolSnapshot != null

    /**
     * Lock in the current study protocol so that the study may be deployed to participants.
     *
     * @throws IllegalStateException when no study protocol is set yet.
     */
    fun goLive()
    {
        checkNotNull( protocolSnapshot ) { "A study protocol needs to be defined for a study to go live." }

        if ( !isLive )
        {
            isLive = true
            event( Event.StateChanged( isLive ) )
        }
    }

    /**
     * Determines whether the study in its current state is ready to be deployed to participants.
     */
    val canDeployToParticipants: Boolean get() = isLive

    /**
     * Get an immutable snapshot of the current state of this [Study] using the specified snapshot [version].
     */
    override fun getSnapshot( version: Int ): StudySnapshot = StudySnapshot.fromStudy( this, version )
}
