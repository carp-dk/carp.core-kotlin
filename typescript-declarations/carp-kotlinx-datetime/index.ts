import kotlin from "@cachet/carp-kotlin"


// Facade with better method names and type conversions for internal types.
export namespace kotlinx.datetime
{
    export interface Clock
        extends kotlin.time.Clock
    {
    }
    export namespace Clock
    {
        export const System: Clock = kotlin.time.Clock.System as Clock
    }
    export interface Instant
        extends kotlin.time.Instant
    {
    }
}


// Export facade.
export default kotlinx
