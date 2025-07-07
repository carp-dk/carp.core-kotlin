package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.application.VersionedStudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [StudyService].
 */
interface StudyServiceTest
{
    /**
     * Create a study service to be used in the tests.
     */
    fun createService(): StudyService


    @Test
    fun createStudy_succeeds() = runTest {
        val service = createService()

        val ownerId = UUID.randomUUID()
        val name = "Test"
        val status = service.createStudy( ownerId, name )

        // Verify whether study was added.
        val foundStudy = service.getStudyStatus( status.studyId )
        assertEquals( status.studyId, foundStudy.studyId )
        assertEquals( name, foundStudy.name )

        // Default study description when not specified.
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( name, studyDetails.invitation.name )
        assertNull( studyDetails.invitation.description )

        assertFalse( foundStudy.canDeployToParticipants )
    }

    @Test
    fun createStudy_with_invitation_succeeds() = runTest {
        val service = createService()

        val ownerId = UUID.randomUUID()
        val name = "Test"
        val description = "Description"
        val invitation = StudyInvitation( "Lorem ipsum", "Some description" )
        val status = service.createStudy( ownerId, name, description, invitation )

        val foundStudy = service.getStudyStatus( status.studyId )
        assertEquals( status.studyId, foundStudy.studyId )
        assertEquals( name, foundStudy.name )
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( description, studyDetails.description )
        assertEquals( invitation, studyDetails.invitation )
        assertFalse( foundStudy.canDeployToParticipants )
    }

    @Test
    fun setInternalDescription_succeeds() = runTest {
        val service = createService()
        val status = service.createStudy( UUID.randomUUID(), "Test" )

        val newName = "New name"
        val newDescription = "New description"
        val updatedStatus = service.setInternalDescription( status.studyId, newName, newDescription )
        assertEquals( newName, updatedStatus.name )
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( newName, studyDetails.name )
        assertEquals( newDescription, studyDetails.description )
    }

    @Test
    fun setInternalDescription_fails_for_unknown_studyId() = runTest {
        val service = createService()

        assertFailsWith<IllegalArgumentException>
        {
            service.setInternalDescription( UUID.randomUUID(), "New name", "New description" )
        }
    }

    @Test
    fun getStudyDetails_fails_for_unknown_studyId() = runTest {
        val service = createService()

        assertFailsWith<IllegalArgumentException> { service.getStudyDetails( unknownId ) }
    }

    @Test
    fun getStudyStatus_succeeds() = runTest {
        val service = createService()
        val status = service.createStudy( UUID.randomUUID(), "Test" )

        val foundStatus = service.getStudyStatus( status.studyId )
        assertEquals( status, foundStatus )
    }

    @Test
    fun getStudyStatus_fails_for_unknown_studyId() = runTest {
        val service = createService()

        assertFailsWith<IllegalArgumentException> { service.getStudyStatus( unknownId ) }
    }

    @Test
    fun getStudiesOverview_returns_owner_studies() = runTest {
        val service = createService()

        val ownerId = UUID.randomUUID()
        val studyOne = service.createStudy( ownerId, "One" )
        val studyTwo = service.createStudy( ownerId, "Two" )
        service.createStudy( UUID.randomUUID(), "Three" )

        val studiesOverview = service.getStudiesOverview( ownerId )
        val expectedStudies = setOf( studyOne, studyTwo )
        assertEquals( 2, studiesOverview.intersect( expectedStudies ).count() )
    }

    @Test
    fun setInvitation_succeeds() = runTest {
        val service = createService()
        val status = service.createStudy( UUID.randomUUID(), "Test" )

        assertTrue( status.canSetInvitation )
        val invitation = StudyInvitation( "Study name", "Description" )
        service.setInvitation( status.studyId, invitation )
        val studyDetails = service.getStudyDetails( status.studyId )
        assertEquals( invitation, studyDetails.invitation )
    }

    @Test
    fun setInvitation_fails_for_unknown_studyId() = runTest {
        val service = createService()

        assertFailsWith<IllegalArgumentException>
        {
            service.setInvitation( unknownId, StudyInvitation( "Some study" ) )
        }
    }

    @Test
    fun setProtocol_succeeds() = runTest {
        val service = createService()
        var status = service.createStudy( UUID.randomUUID(), "Test" )

        assertTrue( status.canSetStudyProtocol )
        val versionedProtocol = createVersionedDeployableProtocol()
        status = service.setProtocol( status.studyId, versionedProtocol )
        assertFalse( status.canDeployToParticipants )
        assertTrue( status is StudyStatus.Configuring )

        val details = service.getStudyDetails( status.studyId )
        assertEquals( versionedProtocol, details.versionedProtocolSnapshot )
    }

    @Test
    fun setProtocol_fails_for_unknown_studyId() = runTest {
        val service = createService()

        assertFailsWith<IllegalArgumentException> { service.setProtocol( unknownId, createVersionedDeployableProtocol() ) }
    }

    @Test
    fun setProtocol_fails_for_invalid_protocol_snapshot() = runTest {
        val service = createService()
        val status = service.createStudy( UUID.randomUUID(), "Test" )

        val validSnapshot = createDeployableProtocol()
        val invalidSnapshot = validSnapshot.copy(
            taskControls = setOf(
                TaskControl( 0, "Unknown task", "Unknown device", TaskControl.Control.Start )
            )
        )
        val versionedInvalidSnapshot = VersionedStudyProtocolSnapshot(
            invalidSnapshot,
            ProtocolVersion( "Version 1" ),
        )
        assertFailsWith<IllegalArgumentException> { service.setProtocol( status.studyId, versionedInvalidSnapshot ) }
    }

    @Test
    fun setProtocol_fails_for_protocol_which_cant_be_deployed() = runTest {
        val service = createService()
        val status = service.createStudy( UUID.randomUUID(), "Test" )

        val protocol = StudyProtocol( UUID.randomUUID(), "Not deployable" )
        val versionedProtocol = VersionedStudyProtocolSnapshot(
            protocol.getSnapshot(),
            ProtocolVersion( "Version 1" ),
        )
        assertFailsWith<IllegalArgumentException> { service.setProtocol( status.studyId, versionedProtocol ) }
    }

    @Test
    fun removeProtocol_succeeds() = runTest {
        val service = createService()
        val status = service.createStudy( UUID.randomUUID(), "Test" )
        val studyId = status.studyId
        service.setProtocol( studyId, createVersionedDeployableProtocol() )

        val removedStatus = service.removeProtocol( studyId )
        assertTrue( removedStatus is StudyStatus.Configuring )
        val details = service.getStudyDetails( studyId )
        assertNull( details.versionedProtocolSnapshot )
    }

    @Test
    fun removeProtocol_fails_for_unknown_studyId() = runTest {
        val service = createService()

        assertFailsWith<IllegalArgumentException> { service.removeProtocol( unknownId ) }
    }

    @Test
    fun setInvitation_and_changes_to_protocol_fail_after_study_gone_live() = runTest {
        val service = createService()
        var status = service.createStudy( UUID.randomUUID(), "Test" )

        val versionedProtocol = VersionedStudyProtocolSnapshot(
            createDeployableProtocol(),
            ProtocolVersion( "Version 1" ),
        )
        service.setProtocol( status.studyId, versionedProtocol )
        status = service.goLive( status.studyId )
        assertFalse( status.canSetInvitation )
        assertFalse( status.canSetStudyProtocol )

        assertFailsWith<IllegalStateException>
        {
            service.setInvitation( status.studyId, StudyInvitation( "Some study" ) )
        }
        assertFailsWith<IllegalStateException> { service.setProtocol( status.studyId, versionedProtocol ) }
        assertFailsWith<IllegalStateException> { service.removeProtocol( status.studyId ) }
    }

    @Test
    fun goLive_succeeds() = runTest {
        val service = createService()

        var status = service.createStudy( UUID.randomUUID(), "Test" )
        assertTrue( status is StudyStatus.Configuring )

        // Set protocol and go live.
        service.setProtocol( status.studyId, createVersionedDeployableProtocol() )
        status = service.goLive( status.studyId )
        assertTrue( status.canDeployToParticipants )
        assertTrue( status is StudyStatus.Live )
    }

    @Test
    fun goLive_fails_for_unknown_studyId() = runTest {
        val service = createService()

        assertFailsWith<IllegalArgumentException> { service.goLive( unknownId ) }
    }

    @Test
    fun goLive_fails_when_no_protocol_set_yet() = runTest {
        val service = createService()
        val status = service.createStudy( UUID.randomUUID(), "Test" )

        assertFailsWith<IllegalStateException> { service.goLive( status.studyId ) }
    }

    @Test
    fun remove_succeeds() = runTest {
        val service = createService()
        val ownerId = UUID.randomUUID()
        val status = service.createStudy( ownerId, "Test" )

        val isRemoved = service.remove( status.studyId )

        assertTrue( isRemoved )
        val studies = service.getStudiesOverview( ownerId )
        assertTrue( studies.isEmpty() )
    }

    @Test
    fun remove_returns_false_when_already_removed() = runTest {
        val service = createService()
        val ownerId = UUID.randomUUID()
        val status = service.createStudy( ownerId, "Test" )
        service.remove( status.studyId )

        val isRemoved = service.remove( status.studyId )

        assertFalse( isRemoved )
    }


    private fun createDeployableProtocol(): StudyProtocolSnapshot
    {
        val protocol = StudyProtocol( UUID.randomUUID(), "Test protocol" )

        // Add primary device, needed for it to be deployable.
        protocol.addPrimaryDevice( Smartphone( "User's phone" ) )

        return protocol.getSnapshot()
    }

    private fun createVersionedDeployableProtocol(): VersionedStudyProtocolSnapshot
    {
        val protocol = createDeployableProtocol()
        return VersionedStudyProtocolSnapshot(
            protocol,
            ProtocolVersion( "Version 1" ),
        )
    }
}
