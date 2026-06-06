/// <reference path="Kotlin-DateTime-library-kotlinx-datetime.d.ts" />
import extend from "@cachet/Kotlin-DateTime-library-kotlinx-datetime"
import kotlin from "@cachet/carp-kotlin"


// Facade with better method names and type conversions for internal types.
export namespace kotlinx.datetime
{
    export interface Clock
    {
        now(): Instant
    }
    export namespace Clock
    {
        export const System: Clock = extend.$_$.System_instance
    }
    export interface Instant
    {
        toEpochMilliseconds(): kotlin.Long
    }
}


// Augment internal types to implement facade.
declare module "@cachet/Kotlin-DateTime-library-kotlinx-datetime"
{
    namespace $_$
    {
        interface System extends kotlinx.datetime.Clock {}
        abstract class System implements kotlinx.datetime.Clock {}
        interface Instant_0 extends kotlinx.datetime.Instant {}
        abstract class Instant_0 implements kotlinx.datetime.Instant {}
    }
}


// Implement base interfaces in internal types.
extend.$_$.System.prototype.now = function(): kotlinx.datetime.Instant { return this.d16(); };
extend.$_$.Instant_0.prototype.toEpochMilliseconds = function(): kotlin.Long { return this.h1m(); };


// Export facade.
export default kotlinx
