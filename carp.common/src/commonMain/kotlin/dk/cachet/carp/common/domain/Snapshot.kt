package dk.cachet.carp.common.domain

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.UUID
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * An immutable snapshot of an [AggregateRoot] at a given moment in time.
 */
@Immutable
@ImplementAsDataClass
@JsExport
interface Snapshot<TAggregateRoot>
{
    val id: UUID

    /**
     * The date when the object represented by this snapshot was created.
     */
    @Suppress( "NON_EXPORTABLE_TYPE" )
    val createdOn: Instant

    /**
     * The number of edits made to the object represented by this snapshot, indicating its version number.
     */
    val version: Int

    /**
     * Load the aggregate root object from this snapshot.
     */
    fun toObject(): TAggregateRoot
}
