package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.DefaultUUIDFactory
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.UUIDFactory
import dk.cachet.carp.common.application.devices.CustomProtocolDevice
import dk.cachet.carp.common.application.tasks.CustomProtocolTask
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import kotlin.time.Clock


/**
 * Implementation of [ProtocolFactoryService] which provides factory methods
 * to create [StudyProtocolSnapshot]'s according to predefined templates.
*/
class ProtocolFactoryServiceHost(
    val uuidFactory: UUIDFactory = DefaultUUIDFactory,
    val clock: Clock = Clock.System
) : ProtocolFactoryService
{
    /**
     * Create a study protocol to be deployed to a single device which has its own way of describing study protocols that
     * deviates from the CARP core study protocol model.
     *
     * The [customProtocol] is stored in a single [CustomProtocolTask] which in the CARP study protocol model is described
     * as being triggered at the start of the study for a [CustomProtocolDevice] with role name "Custom device".
     */
    override suspend fun createCustomProtocol(
        ownerId: UUID,
        name: String,
        customProtocol: String,
        description: String?
    ): StudyProtocolSnapshot
    {
        val protocol = StudyProtocol( ownerId, name, description, uuidFactory.randomUUID(), clock.now() )
        val customDevice = CustomProtocolDevice( "Custom device" )
        protocol.addPrimaryDevice( customDevice )
        val task = CustomProtocolTask( "Custom device task", customProtocol )
        protocol.addTaskControl( customDevice.atStartOfStudy().start( task, customDevice ) )

        return protocol.getSnapshot()
    }
}
