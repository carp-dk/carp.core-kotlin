package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryWithConnectedDeviceProtocol
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * Tests for helper methods in Validation.kt.
 */
class ValidationTest
{
    private val participantId = UUID.randomUUID()
    private val identity: AccountIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
    private val invitation: StudyInvitation = StudyInvitation( "Some study" )

    private fun createInvitation( assignedTo: AssignedTo ) =
        ParticipantInvitation( participantId, assignedTo, identity, invitation )


    @Test
    fun throwIfInvalidInvitations_for_valid_invitations()
    {
        val deviceRoleName = "Test device"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName ).getSnapshot()
        val invitation = createInvitation( AssignedTo.All )

        protocol.throwIfInvalidInvitations( listOf( invitation ) )
    }

    @Test
    fun throwIfInvalidInvitations_for_valid_invitations_with_unassigned_optional_participant_role()
    {
        val toAssign = "Assigned role"
        val protocol = createEmptyProtocol().apply {
            addParticipantRole( ParticipantRole( toAssign, false ) )
            addParticipantRole( ParticipantRole( "Unassigned optional role", true ) )
        }.getSnapshot()
        val invitation = createInvitation( AssignedTo.Roles( setOf( toAssign ) ) )

        protocol.throwIfInvalidInvitations( listOf( invitation ) )
    }

    @Test
    fun throwIfInvalidInvitations_throws_for_empty_invitations()
    {
        val protocol = createSinglePrimaryDeviceProtocol().getSnapshot()

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalidInvitations( emptyList() ) }
    }

    @Test
    fun throwIfInvalidInvitations_throws_for_invalid_participant_role()
    {
        val protocol = createSinglePrimaryDeviceProtocol().getSnapshot()
        val invitation = createInvitation( AssignedTo.Roles( setOf( "Invalid role" ) ) )

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalidInvitations( listOf( invitation ) ) }
    }

    @Test
    fun throwIfInvalidInvitations_throws_for_unassigned_primary_device()
    {
        val deviceToAssign = StubPrimaryDeviceConfiguration( "Assigned device" )
        val unassignedDevice = StubPrimaryDeviceConfiguration( "Unassigned device" )
        val assignedParticipantRole = ParticipantRole( "Assigned role", false )
        val unassignedParticipantRole = ParticipantRole( "Unassigned role", true )
        val assignToParticipant = AssignedTo.Roles( setOf( assignedParticipantRole.role ) )
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( deviceToAssign )
            addParticipantRole( assignedParticipantRole )
            changeDeviceAssignment( deviceToAssign, assignToParticipant )

            addPrimaryDevice( unassignedDevice )
            addParticipantRole( unassignedParticipantRole )
            changeDeviceAssignment( unassignedDevice, AssignedTo.Roles( setOf( unassignedParticipantRole.role ) ) )
        }.getSnapshot()
        val invitation = createInvitation( assignToParticipant )

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalidInvitations( listOf( invitation ) ) }
    }

    @Test
    fun throwIfInvalidInvitations_throws_for_unassigned_participant_role()
    {
        val toAssign = "Test device"
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( StubPrimaryDeviceConfiguration( toAssign ) )
            addPrimaryDevice( StubPrimaryDeviceConfiguration( "Unassigned second device" ) )
            addParticipantRole( ParticipantRole( "Role 1", false ) )
            addParticipantRole( ParticipantRole( "Unassigned role", false ) )
        }.getSnapshot()
        val invitation = createInvitation( AssignedTo.Roles( setOf( "Role 1" ) ) )

        assertFailsWith<IllegalArgumentException> { protocol.throwIfInvalidInvitations( listOf( invitation ) ) }
    }

    @Test
    fun throwIfInvalidPreregistrations_for_valid_preregistrations()
    {
        val primaryRoleName = "Primary"
        val connectedRoleName = "Connected"
        val (protocol, _, connected) =
            createSinglePrimaryWithConnectedDeviceProtocol( primaryRoleName, connectedRoleName )

        val preregistrations = mapOf( connectedRoleName to connected.createRegistration() )
        val protocolSnapshot = protocol.getSnapshot()
        protocolSnapshot.throwIfInvalidPreregistrations( preregistrations )
    }

    @Test
    fun throwIfInvalidPreregistrations_throws_for_preregistration_for_nonconnected_devices()
    {
        val primaryRoleName = "Primary"
        val connectedRoleName = "Connected"
        val (protocol, primary, _) =
            createSinglePrimaryWithConnectedDeviceProtocol( primaryRoleName, connectedRoleName )

        val preregistrations = mapOf( primaryRoleName to primary.createRegistration() )
        val protocolSnapshot = protocol.getSnapshot()
        assertFailsWith<IllegalArgumentException> {
            protocolSnapshot.throwIfInvalidPreregistrations( preregistrations )
        }
    }

    @Test
    fun throwIfInvalidPreregistrations_throws_for_preregistration_for_unknown_devices()
    {
        val deviceRoleName = "Primary"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName ).getSnapshot()

        val preregistrations = mapOf( "Unknown" to DefaultDeviceRegistration() )

        assertFailsWith<IllegalArgumentException> {
            protocol.throwIfInvalidPreregistrations( preregistrations )
        }
    }

    @Test
    fun throwIfInvalidPreregistrations_throws_for_invalid_preregistrations()
    {
        val primaryRoleName = "Primary"
        val connectedRoleName = "Connected"
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol( primaryRoleName, connectedRoleName )

        val invalidRegistration =
            object : DeviceRegistration()
            {
                override val deviceId: String = "Invalid"
                override val deviceDisplayName: String? = null
                override val registrationCreatedOn: Instant = Clock.System.now()
                override val additionalSpecifications: ApplicationData? = null
            }
        val preregistrations = mapOf( connectedRoleName to invalidRegistration )

        val protocolSnapshot = protocol.getSnapshot()
        assertFailsWith<IllegalArgumentException> {
            protocolSnapshot.throwIfInvalidPreregistrations( preregistrations )
        }
    }
}
