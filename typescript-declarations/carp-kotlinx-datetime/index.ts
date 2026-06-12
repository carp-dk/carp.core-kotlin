import kotlin from "@cachet/carp-kotlin"


/**
 * @deprecated Use `kotlin.time` from `@cachet/carp-kotlin` instead.
 */
export namespace kotlinx.datetime
{
    /**
     * @deprecated Use `kotlin.time.Clock` from `@cachet/carp-kotlin` instead.
     */
    export interface Clock extends kotlin.time.Clock {}
    export namespace Clock
    {
        export const System: Clock = kotlin.time.Clock.System
    }

    /**
     * @deprecated Use `kotlin.time.Instant` from `@cachet/carp-kotlin` instead.
     */
    export interface Instant extends kotlin.time.Instant {}
}


// Export deprecated compatibility facade.
export default kotlinx
