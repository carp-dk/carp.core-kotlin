package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.trigger.Trigger
import dk.cachet.carp.analytics.domain.trigger.TriggerActivation
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

/**
 * Service for managing triggers for a [dk.cachet.carp.analytics.domain.workflow.Workflow].
 *
 * A trigger is a mechanism to start or stop execution of analytics workflows.
 */
interface TriggerService : ApplicationService<TriggerService, TriggerService.Event>
{
    companion object { val API_VERSION = ApiVersion(1, 0) }

    @Serializable
    sealed class Event : IntegrationEvent<TriggerService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }
    /**
     * Register a new trigger for a workflow.
     */
    suspend fun createTrigger( trigger: Trigger ): Trigger

    /**
     * Update a new trigger for a workflow.
     */
    suspend fun updateTrigger( trigger: Trigger ): Trigger

    /**
     * Delete a trigger for a workflow.
     *
     * @return true if the trigger was deleted, false if it was not found.
     */
    suspend fun deleteTrigger( triggerId: UUID ): Boolean

    /**
     * Ger a trigger for a workflow by ID.
     *
     * @return [Trigger] if the trigger if it was found.
     */
    suspend fun getTrigger( triggerId: UUID ): Trigger?

    /**
     * List all triggers for a Study.
     *
     * @return [List] of [Trigger]s.
     */
    suspend fun listTriggers( studyId: UUID ): List<Trigger>

    /**
     * List all triggers for a [dk.cachet.carp.analytics.domain.workflow.Workflow] within a Study.
     *
     * @return [List] of [Trigger]s.
     */
    suspend fun listByWorkflow( studyId: UUID, workflowId: UUID ): List<Trigger>

    /**
     * Start a trigger.
     *
     * @return true if starts.
     */
    suspend fun startTrigger( triggerId: UUID, at: Instant = Clock.System.now() ): Boolean

    /**
     * End a trigger.
     *
     * @return true if ended.
     */
    suspend fun endTrigger( triggerId: UUID ): Boolean

    /**
     * Record a trigger activation.
     *
     * @return true if the activation was recorded successfully.
     */
    suspend fun recordActivation( activation: TriggerActivation ): Boolean

    /**
     * Retrieve all activations for a specific study.
     *
     * @return [List] of [TriggerActivation]s associated with the study.
     */
    suspend fun getActivationsForTrigger( triggerId: UUID ): List<TriggerActivation>
}
