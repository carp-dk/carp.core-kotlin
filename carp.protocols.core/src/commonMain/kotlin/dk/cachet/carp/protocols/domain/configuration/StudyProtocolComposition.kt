package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlin.time.Instant


/**
 * A composition root used to initialize [StudyProtocol] with concrete implementations of its interfaces.
 */
@Suppress( "UnnecessaryAbstractClass" ) // https://github.com/detekt/detekt/issues/4626
abstract class StudyProtocolComposition internal constructor(
    protected val deviceConfiguration: ProtocolDeviceConfiguration,
    protected val taskConfiguration: ProtocolTaskConfiguration,
    protected val participantConfiguration: ProtocolParticipantConfiguration,
    id: UUID,
    createdOn: Instant
) : ProtocolDeviceConfiguration by deviceConfiguration,
    ProtocolTaskConfiguration by taskConfiguration,
    ProtocolParticipantConfiguration by participantConfiguration,
    AggregateRoot<StudyProtocol, StudyProtocolSnapshot, StudyProtocol.Event>( id, createdOn )
