package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.VersionedStudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.studies.domain.users.Recruitment


/**
 * Create a 'complex' study for testing purposes.
 */
fun createComplexStudy(): Study
{
    val ownerId = UUID.randomUUID()
    val invitation = StudyInvitation( "Some study" )
    val study = Study( ownerId, "Test", "Description", invitation )

    // Specify protocol.
    val protocol = StudyProtocol( UUID.randomUUID(), "Test protocol" )
    protocol.addPrimaryDevice( Smartphone( "User's phone" ) ) // Needed to make the protocol deployable.
    val protocolVersion = ProtocolVersion( "Version 1" )
    study.versionedProtocolSnapshot = VersionedStudyProtocolSnapshot(
        protocol.getSnapshot(),
        protocolVersion
    )

    // Go live.
    study.goLive()

    return study
}

fun createComplexRecruitment(): Recruitment
{
    val studyId = UUID.randomUUID()
    val recruitment = Recruitment( studyId ).apply {
        val participant = addParticipant( EmailAddress( "test@test.com" ) )
        lockInStudy( createComplexProtocol().getSnapshot(), StudyInvitation( "Test" ) )
        addParticipantGroup( setOf( participant.id ) )
    }

    return recruitment
}
