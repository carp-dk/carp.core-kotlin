@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.isPrimary
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * A serializable snapshot of a [StudyProtocol] at the moment in time when it was created.
 */
@Serializable
@JsExport
data class StudyProtocolSnapshot(
    override val id: UUID,
    override val createdOn: Instant,
    override val version: Int,
    val ownerId: UUID,
    val name: String,
    val description: String? = null,
    val primaryDevices: Set<AnyPrimaryDeviceConfiguration> = emptySet(),
    val connectedDevices: Set<AnyDeviceConfiguration> = emptySet(),
    val connections: Set<DeviceConnection> = emptySet(),
    val tasks: Set<TaskConfiguration<*>> = emptySet(),
    val triggers: Map<Int, TriggerConfiguration<*>> = emptyMap(),
    val taskControls: Set<TaskControl> = emptySet(),
    val participantRoles: Set<ParticipantRole> = emptySet(),
    /**
     * Per device role, the participant roles to which the device is assigned.
     * Unassigned device are assigned to "anyone".
     */
    val assignedDevices: Map<String, Set<String>> = emptyMap(),
    val expectedParticipantData: Set<ExpectedParticipantData> = emptySet(),
    val applicationData: ApplicationData? = null
) : Snapshot<StudyProtocol>
{
    @Serializable
    data class DeviceConnection( val roleName: String, val connectedToRoleName: String )

    companion object
    {
        /**
         * Create a snapshot of the specified [StudyProtocol] using the specified snapshot [version].
         */
        fun fromProtocol( protocol: StudyProtocol, version: Int ): StudyProtocolSnapshot
        {
            val triggers = protocol.triggers.associate { it.id to it.trigger }

            return StudyProtocolSnapshot(
                id = protocol.id,
                createdOn = protocol.createdOn,
                version = version,
                ownerId = protocol.ownerId,
                name = protocol.name,
                description = protocol.description,
                primaryDevices = protocol.primaryDevices.toSet(),
                connectedDevices = protocol.devices.minus( protocol.primaryDevices ).toSet(),
                connections = protocol.primaryDevices.flatMap { getConnections( protocol, it ) }.toSet(),
                tasks = protocol.tasks.toSet(),
                triggers = triggers,
                taskControls = triggers
                    .flatMap { trigger -> protocol.getTaskControls( trigger.value ).map { trigger to it } }
                    .map { (trigger, tc) ->
                        TaskControl( trigger.key, tc.task.name, tc.destinationDevice.roleName, tc.control )
                    }
                    .toSet(),
                participantRoles = protocol.participantRoles.toSet(),
                assignedDevices = protocol.deviceAssignments
                    .filter { it.value is AssignedTo.Roles }
                    .map { it.key.roleName to (it.value as AssignedTo.Roles).roleNames }
                    .toMap(),
                expectedParticipantData = protocol.expectedParticipantData.toSet(),
                applicationData = protocol.applicationData
            )
        }

        private fun getConnections(
            protocol: StudyProtocol,
            primaryDevice: AnyPrimaryDeviceConfiguration
        ): Iterable<DeviceConnection>
        {
            val connections: MutableList<DeviceConnection> = mutableListOf()

            protocol.getConnectedDevices( primaryDevice ).forEach { device ->
                connections.add( DeviceConnection( device.roleName, primaryDevice.roleName ) )
                if ( device.isPrimary() ) connections.addAll( getConnections( protocol, device ) )
            }

            return connections
        }
    }


    override fun toObject(): StudyProtocol = StudyProtocol.fromSnapshot( this )
}
