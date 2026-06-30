package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * Describes the status of a study: the number of participants, progress towards study goal, etc.
 */
@Serializable
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
sealed class StudyStatus
{
    abstract val studyId: UUID
    /**
     * A descriptive name for the study, as assigned by the study owner.
     */
    abstract val name: String
    /**
     * The date when this study was created.
     */
    abstract val createdOn: Instant
    /**
     * In case a study protocol for this study is set, its id; null otherwise.
     */
    abstract val studyProtocolId: UUID?
    /**
     * Determines whether the invitation which is shared with participants can be changed for the study.
     */
    abstract val canSetInvitation: Boolean
    /**
     * Determines whether a study protocol can be set/changed for the study.
     */
    abstract val canSetStudyProtocol: Boolean
    /**
     * Determines whether the study in its current state is ready to be deployed to participants.
     */
    abstract val canDeployToParticipants: Boolean


    /**
     * Study status for when a study is being configured.
     */
    @Serializable
    data class Configuring(
        override val studyId: UUID,
        override val name: String,
        override val createdOn: Instant,
        override val studyProtocolId: UUID?,
        override val canSetInvitation: Boolean,
        override val canSetStudyProtocol: Boolean,
        override val canDeployToParticipants: Boolean,
        /**
         * Determines whether a study is fully configured and can 'go live'.
         */
        val canGoLive: Boolean
    ) : StudyStatus()


    /**
     * Study status for when a study is 'live'.
     */
    @Serializable
    data class Live(
        override val studyId: UUID,
        override val name: String,
        override val createdOn: Instant,
        override val studyProtocolId: UUID?,
        override val canSetInvitation: Boolean,
        override val canSetStudyProtocol: Boolean,
        override val canDeployToParticipants: Boolean
    ) : StudyStatus()
}
