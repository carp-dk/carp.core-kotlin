/// <reference path="kotlin-kotlin-stdlib.d.ts" />
import extend from "@cachet/kotlin-kotlin-stdlib"


// Facade with better method names and type conversions for internal types.
export namespace kotlinExport
{
    export type Nullable<T> = T | null | undefined
    export interface Long
    {
        toNumber(): number
    }
    export const toLong = (number: number): Long => BigInt( number ) as any as Long
    export class Pair<K, V>
    {
        constructor( first: K, second: V ) {
            let kotlinPair = new extend.$_$.Pair( first, second );
            kotlinPair.first = kotlinPair.od_1;
            kotlinPair.second = kotlinPair.pd_1;
            return kotlinPair;
        }
        get first(): K { return this.first; }
        get second(): V { return this.second; }
    }
}
export namespace kotlinExport.collections
{
    export const KtList = extend.kotlin.collections.KtList
    export const KtSet = extend.kotlin.collections.KtSet
    export const KtMap = extend.kotlin.collections.KtMap

    export interface Collection<T>
    {
        contains( value: T ): boolean
        size(): number
        toArray(): Array<T>
    }
    export interface List<T> extends Collection<T> {}
    export interface Set<T> extends Collection<T> {}
    export interface Map<K, V>
    {
        get( key: K ): V
        keys: Set<K>
        values: Collection<V>
    }
    export const listOf: <T>(array: T[]) => List<T> = extend.$_$.listOf_0
    export const setOf: <T>(array: T[]) => Set<T> = extend.$_$.setOf_0
    export const mapOf =
        function<K, V>( pairs: kotlinExport.Pair<K, V>[] ): Map<K, V>
        {
            return extend.$_$.mapOf_0( pairs as any )
        }
}
export namespace kotlinExport.time
{
    export interface Duration
    {
        get inWholeMilliseconds(): number
        get inWholeMicroseconds(): number
    }
    export namespace Duration
    {
        export const Companion: any = extend.$_$.Companion_getInstance_20()
        export const parseIsoString = (isoDuration: string): Duration => Companion.kh( isoDuration ) as Duration
        export const ZERO: Duration = Companion.hh_1 as Duration
        export const INFINITE: Duration = Companion.ih_1 as Duration
    }
}


// Augment internal types to implement facade.
declare module "@cachet/kotlin-kotlin-stdlib"
{
    namespace $_$
    {
        interface Pair<K, V> extends kotlinExport.Pair<K, V>
        {
            first: K
            second: V
        }
        interface Collection<T> extends kotlinExport.collections.Collection<T> {}
        abstract class EmptyList<T> implements kotlinExport.collections.List<T> {}
        abstract class AbstractMutableList<T> implements kotlinExport.collections.List<T> {}
        interface Set<T> extends kotlinExport.collections.Set<T> {}
        abstract class EmptySet<T> implements kotlinExport.collections.Set<T> {}
        abstract class HashSet<T> implements kotlinExport.collections.Set<T> {}
        interface Map<K, V> extends kotlinExport.collections.Map<K, V> {}
        abstract class HashMap<K, V> implements kotlinExport.collections.Map<K, V>
        {
            get( key: K ): V
            keys: kotlinExport.collections.Set<K>
            values: kotlinExport.collections.Collection<V>
        }
    }
}


// Implement base interfaces in internal types.
(BigInt.prototype as any).toNumber = function(): number { return Number( this.valueOf() ); };
const infiniteDuration = kotlinExport.time.Duration.INFINITE
Object.defineProperty( BigInt.prototype, "inWholeMilliseconds", {
    get: function inWholeMilliseconds()
    {
        if ( this.valueOf() === infiniteDuration ) return -1

        return Number( extend.$_$._Duration___get_inWholeMilliseconds__impl__msfiry( this.valueOf() ) );
    }
} );
Object.defineProperty( BigInt.prototype, "inWholeMicroseconds", {
    get: function inWholeMicroseconds()
    {
        if ( this.valueOf() === infiniteDuration ) return -1

        return Number( extend.$_$._Duration___get_inWholeMicroseconds__impl__8oe8vv( this.valueOf() ) );
    }
} );
extend.$_$.EmptyList.prototype.contains = function<T>( value: T ): boolean { return false; }
extend.$_$.EmptyList.prototype.size = function<T>(): number { return 0; }
extend.$_$.EmptyList.prototype.toArray = function<T>(): T[] { return []; }
extend.$_$.AbstractMutableList.prototype.contains = function<T>( value: T ): boolean { return this.g2( value ); }
extend.$_$.AbstractMutableList.prototype.size = function<T>(): number { return this.o(); }
extend.$_$.EmptySet.prototype.contains = function<T>( value: T ): boolean { return false; }
extend.$_$.EmptySet.prototype.size = function<T>(): number { return 0; }
extend.$_$.EmptySet.prototype.toArray = function<T>(): T[] { return []; }
extend.$_$.HashSet.prototype.contains = function<T>( value: T ): boolean { return this.g2( value ); }
extend.$_$.HashSet.prototype.size = function<T>(): number { return this.o(); }
extend.$_$.HashMap.prototype.get = function<K, V>( key: K ): V { return this.d2( key ); }
Object.defineProperty( extend.$_$.HashMap.prototype, "keys", {
    get: function keys() { return this.z1(); }
} );
Object.defineProperty( extend.$_$.HashMap.prototype, "values", {
    get: function values() { return this.a2(); }
} );


// Export facade.
export default kotlinExport
