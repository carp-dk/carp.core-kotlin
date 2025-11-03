package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.trigger.*
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

/**
 * Repository interface for basic CRUD operations on triggers.
 */
interface TriggerCrudRepository
{
    /**
     * Create a new trigger.
     *
     * @param trigger The trigger to create.
     * @return The created trigger with its ID and timestamps.
     */
    suspend fun create( trigger: Trigger ): Trigger

    /**
     * Update an existing trigger.
     *
     * @param trigger The trigger with updated information.
     * @return The updated trigger.
     */
    suspend fun update( trigger: Trigger ): Trigger

    /**
     * Delete a trigger by its ID.
     *
     * @param triggerId The ID of the trigger to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    suspend fun delete( triggerId: UUID ): Boolean

    /**
     * Retrieve a trigger by its ID.
     *
     * @param triggerId The ID of the trigger to retrieve.
     * @return The trigger if found, null otherwise.
     */
    suspend fun get( triggerId: UUID ): Trigger?

    /**
     * List all triggers for a specific study.
     *
     * @param studyId The ID of the study to list triggers for.
     * @return A list of triggers associated with the study.
     */
    suspend fun list( studyId: UUID ): List<Trigger>

    /**
     * List all triggers for a specific workflow within a study.
     *
     * @param studyId The ID of the study to list triggers for.
     * @param workflowId The ID of the workflow to filter triggers by.
     * @return A list of triggers associated with the study and workflow.
     */
    suspend fun listByWorkflow( studyId: UUID, workflowId: UUID ): List<Trigger>
}

/**
 * Repository interface for managing trigger lifecycle operations.
 */
interface TriggerLifecycleRepository
{
    /**
     * Start a trigger by its ID at the specified time.
     *
     * @param triggerId The ID of the trigger to start.
     * @param startedAt The time when the trigger was started.
     * @return True if the trigger was successfully started, false otherwise.
     */
    suspend fun startTrigger( triggerId: UUID, startedAt: Instant ): Boolean

    /**
     * End a trigger by its ID at the specified time.
     *
     * @param triggerId The ID of the trigger to end.
     * @return True if the trigger was successfully ended, false otherwise.
     */
    suspend fun endTrigger( triggerId: UUID ): Boolean

    /**
     *  Retrieve all active scheduled triggers
     */
    suspend fun getAllScheduled(): List<ScheduledTrigger>
}

/**
 * Repository interface for tracking trigger activations.
 */
interface TriggerActivationRepository
{
    /**
     * Record a trigger activation.
     *
     * @param activation The activation details to record.
     * @return True if the activation was successfully recorded, false otherwise.
     */
    suspend fun recordActivation( activation: TriggerActivation ): Boolean

    /**
     * Add a new activation for a trigger.
     *
     * @param activation The activation to add.
     */
    suspend fun addActivation( activation: TriggerActivation )

    /**
     * Retrieve all activations for a specific study.
     *
     * @param studyId The ID of the study to retrieve activations for.
     * @return A list of trigger activations associated with the study.
     */
    suspend fun getActivationsForStudy( studyId: UUID ): List<TriggerActivation>

    /**
     * Retrieve all activations for a specific trigger.
     */
    suspend fun getActivationsForTrigger( triggerId: UUID ): List<TriggerActivation>

    /**
     * Get the latest activation for a specific trigger.
     */
    suspend fun getLatestActivationForTrigger( triggerId: UUID ): TriggerActivation?
}

/**
 * Repository interface for storing and retrieving triggers.
 *
 * Supports creation, updating, deletion, and lifecycle tracking.
 * Combines all trigger repository capabilities through interface composition.
 */
interface TriggerRepository : TriggerCrudRepository, TriggerLifecycleRepository, TriggerActivationRepository
