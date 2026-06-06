package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.domain.users.getAssignedDeviceRoleNames
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlin.js.JsExport


/**
 * Throw [IllegalArgumentException] when [invitations] don't match the requirements of the protocol.
 *
 * @throws IllegalArgumentException when:
 *  - [invitations] is empty
 *  - any of the participant roles in [invitations] is not part of the study protocol
 *  - not all necessary primary devices part of the study protocol have been assigned a participant
 *  - not all necessary participant roles part of the study have been assigned a participant
 */
@JsExport
fun StudyProtocolSnapshot.throwIfInvalidInvitations( invitations: List<ParticipantInvitation> )
{
    require( invitations.isNotEmpty() ) { "No participants invited." }

    val assignedParticipantRoles = invitations
        .map { it.assignedRoles }
        .filterIsInstance<AssignedTo.Roles>()
        .flatMap { it.roleNames }
        .toSet()
    val availableRoles = participantRoles.map { it.role }.toSet()
    assignedParticipantRoles.forEach { assigned ->
        require( assigned in availableRoles )
            { "The assigned participant role \"$assigned\" is not part of the study protocol." }
    }

    val allRolesAssigned = invitations.any { it.assignedRoles is AssignedTo.All }
    if ( !allRolesAssigned ) // When all roles are assigned, it also covers all devices; no need to check.
    {
        val requiredRoles = participantRoles.filter { !it.isOptional }.map { it.role }.toSet()
        require( assignedParticipantRoles.containsAll( requiredRoles ) )
            { "Not all necessary participant roles have been assigned a participant." }

        val requiredPrimaryDeviceRoleNames = primaryDevices.filter { !it.isOptional }.map { it.roleName }
        val assignedPrimaryDeviceRoleNames = invitations
            .flatMap { this.getAssignedDeviceRoleNames( it.assignedRoles ) }
            .toSet()
        require( assignedPrimaryDeviceRoleNames.containsAll( requiredPrimaryDeviceRoleNames ) )
            { "Not all necessary devices required for this study have been assigned to a participant." }
    }
}

/**
 * Throw [IllegalArgumentException] when [connectedDevicePreregistrations] don't match the requirements of the protocol.
 *
 * @throws IllegalArgumentException when:
 *  - one of the role names in [connectedDevicePreregistrations] isn't defined in the study protocol
 *  - an invalid registration for one of the devices is passed
 */
@JsExport
fun StudyProtocolSnapshot.throwIfInvalidPreregistrations(
    connectedDevicePreregistrations: Map<String, DeviceRegistration>
)
{
    connectedDevicePreregistrations.forEach { (roleName, registration) ->
        val connectedDevice = connectedDevices.firstOrNull { it.roleName == roleName }
        requireNotNull( connectedDevice )
        {
            "The device with role name \"$roleName\" for which a preregistration was defined " +
            "isn't a connected device in the study protocol."
        }

        val isInvalidRegistration = connectedDevice.isDefinitelyInvalidRegistration( registration )
        require( !isInvalidRegistration )
        {
            "The preregistration for the connected device with role name \"$roleName\" is invalid."
        }
    }
}
