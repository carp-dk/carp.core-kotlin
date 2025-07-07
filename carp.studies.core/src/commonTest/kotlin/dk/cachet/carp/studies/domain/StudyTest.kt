package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.VersionedStudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.StudyStatus
import kotlin.test.*


/**
 * Tests for [Study].
 */
class StudyTest
{
    @Test
    fun creating_study_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val study = createComplexStudy()

        val snapshot = study.getSnapshot()
        val fromSnapshot = Study.fromSnapshot( snapshot )

        assertEquals( study.id, fromSnapshot.id )
        assertEquals( study.ownerId, fromSnapshot.ownerId )
        assertEquals( study.name, fromSnapshot.name )
        assertEquals( study.description, fromSnapshot.description )
        assertEquals( study.invitation, fromSnapshot.invitation )
        assertEquals( study.createdOn, fromSnapshot.createdOn )
        assertEquals( study.versionedProtocolSnapshot, fromSnapshot.versionedProtocolSnapshot )
        assertEquals( study.isLive, fromSnapshot.isLive )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }

    @Test
    fun set_invitation_succeeds()
    {
        val study = createStudy()

        assertTrue( study.canSetInvitation )

        val invitation = StudyInvitation( "Test study", "This is a test." )
        study.invitation = invitation
        assertEquals( invitation, study.invitation )
    }

    @Test
    fun set_invitation_fails_for_live_study()
    {
        val study = createStudy()
        setDeployableProtocol( study )
        study.goLive()

        assertFailsWith<IllegalStateException> { study.invitation = StudyInvitation( "Some study" ) }
    }

    @Test
    fun set_protocol_succeeds()
    {
        val study = createStudy()

        assertTrue( study.canSetStudyProtocol )

        setDeployableProtocol( study )
        assertEquals( Study.Event.ProtocolSnapshotChanged(study.versionedProtocolSnapshot?.protocolSnapshot), study.consumeEvents().single() )
        assertEquals(study.versionedProtocolSnapshot?.protocolSnapshot?.id, study.getStatus().studyProtocolId )
    }

    @Test
    fun set_protocol_to_null_succeeds()
    {
        val study = createStudy()
        study.versionedProtocolSnapshot = null
        assertEquals( Study.Event.ProtocolSnapshotChanged( null ), study.consumeEvents().single() )
        assertNull( study.getStatus().studyProtocolId )
    }

    @Test
    fun set_protocol_fails_for_protocol_with_deployment_errors()
    {
        val study = createStudy()

        val protocol = StudyProtocol( UUID.randomUUID(), "Test protocol" )
        val versionedProtocolSnapshot = VersionedStudyProtocolSnapshot(
            protocol.getSnapshot(),
            ProtocolVersion( "Version 1" )
        )
        assertFailsWith<IllegalArgumentException> { study.versionedProtocolSnapshot = versionedProtocolSnapshot }
        assertEquals( 0, study.consumeEvents().count() )
    }

    @Test
    fun verify_state_of_new_study()
    {
        val study = createStudy()
        assertFalse( study.canDeployToParticipants )
        assertFalse( study.isLive )

        val status = study.getStatus()
        assertNull( status.studyProtocolId )
        assertEquals( study.canSetInvitation, status.canSetInvitation )
        assertEquals( study.canSetStudyProtocol, status.canSetStudyProtocol )
        assertEquals( study.canDeployToParticipants, status.canDeployToParticipants )
        assertTrue( status is StudyStatus.Configuring )
        assertFalse( status.canGoLive )
    }

    @Test
    fun canDeployToParticipants_false_until_live()
    {
        val study = createStudy()
        assertFalse( study.canDeployToParticipants )

        // Define protocol.
        setDeployableProtocol( study )
        assertFalse( study.canDeployToParticipants )

        // Go live.
        study.goLive()
        assertTrue( study.canDeployToParticipants )
        assertEquals( Study.Event.StateChanged( true ), study.consumeEvents().last() )
    }

    @Test
    fun canSetInvitation_and_canSetStudyProtocol_true_until_live()
    {
        val study = createStudy()
        assertTrue( study.canSetInvitation )
        assertTrue( study.canSetStudyProtocol )

        // Go live.
        setDeployableProtocol( study )
        study.goLive()
        assertFalse( study.canSetInvitation )
        assertFalse( study.canSetStudyProtocol )
    }

    @Test
    fun goLive_can_be_called_more_than_once()
    {
        val study = createStudy()
        setDeployableProtocol( study )
        study.goLive()

        // Study already live, but should not fail.
        study.goLive()
        val stateEvents = study.consumeEvents().filterIsInstance<Study.Event.StateChanged>()
        assertEquals( 1, stateEvents.count() )
    }

    @Test
    fun goLive_fails_when_no_protocol_set()
    {
        val study = createStudy()
        val status = study.getStatus()

        assertTrue( status is StudyStatus.Configuring )
        assertFalse( status.canGoLive )

        assertFailsWith<IllegalStateException> { study.goLive() }
        assertEquals( 0, study.consumeEvents().count() )
    }


    private fun createStudy(): Study = Study( UUID.randomUUID(), "Test study" )

    private fun setDeployableProtocol( study: Study )
    {
        val protocol = StudyProtocol( UUID.randomUUID(), "Test protocol" )
        protocol.addPrimaryDevice( Smartphone( "User's phone" ) ) // One primary device is needed to deploy.
        study.versionedProtocolSnapshot = VersionedStudyProtocolSnapshot(
            protocol.getSnapshot(),
            ProtocolVersion( "Version 1" )
        )
    }
}
