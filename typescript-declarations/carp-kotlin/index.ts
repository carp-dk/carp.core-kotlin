/// <reference path="kotlin-kotlin-stdlib.d.ts" />
import extend from "@cachet/kotlin-kotlin-stdlib"


// Facade with better method names and type conversions for internal types.
export namespace kotlinExport
{
    export type Nullable<T> = T | null | undefined
    /**
     * @deprecated Use bigint instead.
     */
    export type Long = bigint
    /**
     * @deprecated Use {@link BigInt} constructor or bigint literal instead.
     */
    export const toLong = (number: number): Long => BigInt( number )
    /**
     * @deprecated Use {@link KtMap} and initialize using JS arrays instead.
     */
    export class Pair<K, V>
    {
        constructor( first: K, second: V ) {
            let kotlinPair = new extend.$_$.Pair( first, second );
            return kotlinPair;
        }
        declare readonly first: K
        declare readonly second: V
    }
}
declare global
{
    // Backwards compatibility for codebases which still use Long.
    interface BigInt
    {
        /**
         * @deprecated Keep as {@link bigint} to prevent losing precision, or use {@link Number} constructor instead.
         */
        toNumber(): number
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

        /**
         * Copies all elements of this collection to a new array.
         */
        toArray(): Array<T>
    }
    /**
     * @deprecated Use {@link KtList} instead.
     */
    export type List<T> = extend.kotlin.collections.KtList<T>
    /**
     * @deprecated Use {@link KtSet} instead.
     */
    export type Set<T> = extend.kotlin.collections.KtSet<T>
    /**
     * @deprecated Use {@link KtMap} instead.
     */
    export type Map<K, V> = extend.kotlin.collections.KtMap<K, V>
    /**
     * @deprecated Use {@link KtList.fromJsArray} instead.
     */
    export function listOf<T>( array: T[] ) { return KtList.fromJsArray( array ); }
    /**
     * @deprecated Use {@link KtSet.fromJsSet} instead.
     */
    export function setOf<T>( array: T[] ) { return KtSet.fromJsSet( new Set( array ) ) }
    /**
     * @deprecated Use {@link KtMap.fromJsMap} instead.
     */
    export function mapOf<K, V>( pairs: kotlinExport.Pair<K, V>[] )
    {
        return KtMap.fromJsMap( new Map( pairs.map( pair => [ pair.first, pair.second ] ) ) )
    }
}
export namespace kotlinExport.time
{
    export interface Duration
    {
        get inWholeMilliseconds(): bigint
        get inWholeMicroseconds(): bigint
        toDurationString(): string
    }
    export namespace Duration
    {
        export const Companion: any = extend.$_$.Companion_getInstance_17()
        export const parseIsoString: (isoDuration: string) => Duration = Companion.pg
        export const ZERO: Duration = Companion.ig_1
        export const INFINITE: Duration = Companion.jg_1
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
        abstract class EmptyList<T> implements kotlin.collections.KtList<T> {}
        abstract class AbstractMutableList<T> implements kotlin.collections.KtList<T> {}
        interface Set<T> extends kotlin.collections.KtSet<T> {}
        abstract class EmptySet<T> implements kotlin.collections.KtSet<T> {}
        abstract class HashSet<T> implements kotlin.collections.KtSet<T> {}
        interface Map<K, V> extends kotlin.collections.KtMap<K, V> {}
        abstract class HashMap<K, V> implements kotlin.collections.KtMap<K, V> {}
    }
    namespace kotlin
    {
        namespace collections
        {
            interface KtList<T> extends kotlinExport.collections.Collection<T> {}
            interface KtSet<T> extends kotlinExport.collections.Collection<T> {}
            interface KtMap<K, V>
            {
                get( key: K ): V
                keys: KtSet<K>
                values: kotlinExport.collections.Collection<V>
            }
        }
    }
}


// Implement base interfaces in internal types.
(BigInt.prototype as any).toNumber = function(): number { return Number( this ); };
(BigInt.prototype as any).toDurationString = function(): string { return extend.$_$.Duration__toString_impl_8d916b( this ) };
Object.defineProperty( BigInt.prototype, "inWholeMilliseconds", {
    get: function inWholeMilliseconds()
    {
        return extend.$_$._Duration___get_inWholeMilliseconds__impl__msfiry( this );
    }
} );
Object.defineProperty( BigInt.prototype, "inWholeMicroseconds", {
    get: function inWholeMicroseconds()
    {
        return extend.$_$._Duration___get_inWholeMicroseconds__impl__8oe8vv( this );
    }
} );
extend.$_$.EmptyList.prototype.contains = function<T>( value: T ): boolean { return false; }
extend.$_$.EmptyList.prototype.size = function<T>(): number { return 0; }
extend.$_$.EmptyList.prototype.toArray = function<T>(): T[] { return []; }
extend.$_$.AbstractMutableList.prototype.contains = function<T>( value: T ): boolean { return this.asJsReadonlyArrayView().includes( value ); }
extend.$_$.AbstractMutableList.prototype.size = function<T>(): number { return this.asJsReadonlyArrayView().length; }
extend.$_$.EmptySet.prototype.contains = function<T>( value: T ): boolean { return false; }
extend.$_$.EmptySet.prototype.size = function<T>(): number { return 0; }
extend.$_$.EmptySet.prototype.toArray = function<T>(): T[] { return []; }
extend.$_$.HashSet.prototype.contains = function<T>( value: T ): boolean { return this.asJsReadonlySetView().has( value ); }
extend.$_$.HashSet.prototype.size = function<T>(): number { return this.asJsReadonlySetView().size; }
extend.$_$.HashMap.prototype.get = function<K, V>( key: K ): V { return this.asJsReadonlyMapView().get( key ); }
Object.defineProperty( extend.$_$.HashMap.prototype, "keys", {
    get: function keys() { return extend.kotlin.collections.KtSet.fromJsSet( new Set( this.asJsReadonlyMapView().keys() ) ); }
} );
Object.defineProperty( extend.$_$.HashMap.prototype, "values", {
    get: function values() { return extend.kotlin.collections.KtList.fromJsArray( [ ...this.asJsReadonlyMapView().values() ] ); }
} );


// Export facade.
export default kotlinExport
