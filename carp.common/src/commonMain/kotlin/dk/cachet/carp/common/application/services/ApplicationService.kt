package dk.cachet.carp.common.application.services

import kotlin.js.JsExport


/**
 * Exposes interactions with internal domain objects which may raise [TIntegrationEvent]s.
 */
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
interface ApplicationService<
    Self : ApplicationService<Self, TIntegrationEvent>,
    in TIntegrationEvent : IntegrationEvent<Self>
>
