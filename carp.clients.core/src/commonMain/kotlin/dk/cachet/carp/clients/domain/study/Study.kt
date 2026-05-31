package dk.cachet.carp.clients.domain.study

import dk.cachet.carp.clients.application.study.StudyStatus
import dk.cachet.carp.clients.domain.data.DataListener
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * A study deployment, identified by [studyDeploymentId],
 * which a client device participates in with the role [deviceRoleName].
 */
class Study(
    /**
     * The ID of the deployed study for which to collect data.
     */
    val studyDeploymentId: UUID,
    /**
     * The role name of the device this runtime is intended for within the deployment identified by [studyDeploymentId].
     */
    val deviceRoleName: String,
    id: UUID = UUID.randomUUID(),
    createdOn: Instant = Clock.System.now()
) : AggregateRoot<Study, StudySnapshot, Study.Event>( id, createdOn )
{
    sealed class Event : DomainEvent
    {
        data class DeploymentStatusReceived( val deploymentStatus: StudyDeploymentStatus ) : Event()
        data class DeviceDeploymentReceived( val deploymentInformation: PrimaryDeviceDeployment ) : Event()
    }


    companion object Factory
    {
        internal fun fromSnapshot( snapshot: StudySnapshot ): Study
        {
            val study =
                Study( snapshot.studyDeploymentId, snapshot.deviceRoleName, snapshot.id, snapshot.createdOn ).apply {
                    deploymentStatus = snapshot.deploymentStatus
                    deploymentInformation = snapshot.deploymentInformation
                }
            study.wasLoadedFromSnapshot( snapshot )

            return study
        }
    }


    private var deploymentStatus: StudyDeploymentStatus? = null
    private var deploymentInformation: PrimaryDeviceDeployment? = null


    /**
     * Get the status of this [Study].
     */
    fun getStatus(): StudyStatus
    {
        val status = deploymentStatus ?: return StudyStatus.DeploymentNotStarted( id )

        return when ( status )
        {
            is StudyDeploymentStatus.Invited -> error( "Client device should already be registered." )
            is StudyDeploymentStatus.DeployingDevices ->
                StudyStatus.Deploying.fromStudyDeploymentStatus( id, deviceRoleName, status, deploymentInformation )
            is StudyDeploymentStatus.Running ->
                StudyStatus.Running( id, status, checkNotNull( deploymentInformation ) )
            is StudyDeploymentStatus.Stopped ->
                StudyStatus.Stopped( id, status, deploymentInformation )
        }
    }

    /**
     * An updated [deploymentStatus] has been received.
     */
    fun deploymentStatusReceived( deploymentStatus: StudyDeploymentStatus )
    {
        this.deploymentStatus = deploymentStatus
        event( Event.DeploymentStatusReceived( deploymentStatus ) )
    }

    /**
     * A new primary device [deployment] determining what data to collect for this study has been received.
     *
     * @throws IllegalArgumentException when the role name [deployment] is intended for is different from the expected [deviceRoleName].
     */
    fun deviceDeploymentReceived( deployment: PrimaryDeviceDeployment )
    {
        checkNotNull( deploymentStatus )
            { "Can't receive device deployment before having received deployment status." }
        require( deployment.deviceConfiguration.roleName == deviceRoleName )
            { "The deployment is intended for a device with a different role name." }

        deploymentInformation = deployment
        event( Event.DeviceDeploymentReceived( deployment ) )
    }

    /**
     * Verify whether all prerequisites for the deployment to run on this device are met, or throw an exception otherwise.
     *
     * TODO: This shouldn't be a separate call that only works once all devices are registered.
     *   Partial validation should happen on `deviceDeploymentReceived`, subsequent `registerDevice` calls once added here,
     *   and remote device registrations through `deploymentStatusReceived`.
     *
     * @throws IllegalStateException when:
     *  - deployment hasn't been received yet
     *  - not all required devices have been registered
     * @throws UnsupportedOperationException in case not all necessary plugins to execute the deployment are available.
     */
    fun validateDeviceDeployment( dataListener: DataListener )
    {
        val deployment = checkNotNull( deploymentInformation )
        val remainingDevicesToRegister = deploymentStatus?.getRemainingDevicesToRegister().orEmpty()

        // All devices need to be registered before deployment can be validated.
        check( remainingDevicesToRegister.isEmpty() )

        // Verify whether data collection is supported on all connected devices and for all requested measures.
        for ( device in deployment.getRuntimeDeviceInfo() )
        {
            // It shouldn't be possible to be ready for deployment when connected device registration is not set.
            val registration = checkNotNull( device.registration )

            // Verify whether connected device is supported.
            val deviceType = device.configuration::class
            if ( device.isConnectedDevice )
            {
                dataListener.tryGetConnectedDataCollector( deviceType, registration )
                    ?: throw UnsupportedOperationException(
                        "Connecting to device of type \"$deviceType\" is not supported on this client."
                    )
            }

            val dataTypes = device.tasks
                .flatMap { it.measures.filterIsInstance<Measure.DataStream>() }
                .map { it.type }
                .distinct()
            for ( dataType in dataTypes )
            {
                val supportsData =
                    if ( device.isConnectedDevice )
                    {
                        dataListener.supportsDataOnConnectedDevice( dataType, deviceType, registration )
                    }
                    else dataListener.supportsData( dataType )

                if ( !supportsData )
                {
                    throw UnsupportedOperationException(
                        "Subscribing to data of data type \"$dataType\" " +
                        "on device with role \"${device.configuration.roleName}\" is not supported on this client."
                    )
                }
            }
        }
    }

    /**
     * Get an immutable snapshot of the current state of this [Study] using the specified snapshot [version].
     */
    override fun getSnapshot( version: Int ): StudySnapshot = StudySnapshot.fromStudy( this, version )
}
