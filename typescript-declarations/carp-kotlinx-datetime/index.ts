/// <reference path="Kotlin-DateTime-library-kotlinx-datetime.d.ts" />
import extend from "@cachet/Kotlin-DateTime-library-kotlinx-datetime"


// Facade with better method names and type conversions for internal types.
export namespace kotlinx.datetime
{
    export interface Clock
    {
        now(): Instant
    }
    export namespace Clock
    {
        export const System: Clock = extend.$_$.b as unknown as Clock
    }
    export interface Instant
    {
        toEpochMilliseconds(): number
    }
}


// Implement base interfaces in internal types.
const SystemPrototype = Object.getPrototypeOf( extend.$_$.b )
SystemPrototype.now = function(): kotlinx.datetime.Instant { return this.m15(); };

const InstantPrototype = Object.getPrototypeOf( extend.$_$.b.m15() )
InstantPrototype.toEpochMilliseconds = function(): number { return Number( this.m1l() ); };


// Export facade.
export default kotlinx
