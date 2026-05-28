/// <reference path="kotlin-kotlin-stdlib.d.ts" />
import extend from "@cachet/kotlin-kotlin-stdlib"


// Facade with better method names and type conversions for internal types.
export namespace kotlinExport
{
    export type Nullable<T> = T | null | undefined
    /**
     * @deprecated Use bigint directly.
     */
    export type Long = bigint
    /**
     * @deprecated Use BigInt(number) directly.
     */
    export const toLong = (number: number): Long => BigInt( number )
    export const Pair = extend.kotlin.Pair
    export type Pair<K, V> = extend.kotlin.Pair<K, V>
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
    export const listOf: <T>(array: T[]) => List<T> = extend.$_$.j6
    export const setOf: <T>(array: T[]) => Set<T> = extend.$_$.u6
    export const mapOf =
        function<K, V>( pairs: kotlinExport.Pair<K, V>[] ): Map<K, V>
        {
            return extend.$_$.l6( pairs as any )
        }
}
export namespace kotlinExport.time
{
    export type Duration = bigint
    export namespace Duration
    {
        export const Companion: any = extend.$_$.t3()
        export const parseIsoString: (isoDuration: string) => Duration = Companion.ng
        export const ZERO: Duration = Companion.gg_1
        export const INFINITE: Duration = Companion.hg_1
    }
}


// Augment internal types to implement facade.
declare module "@cachet/kotlin-kotlin-stdlib"
{
    namespace $_$
    {
        interface Collection<T> extends kotlinExport.collections.Collection<T> {}
        interface List<T> extends kotlinExport.collections.List<T> {}
        interface Set<T> extends kotlinExport.collections.Set<T> {}
        interface Map<K, V> extends kotlinExport.collections.Map<K, V> {}
    }
}


// Implement base interfaces in internal types.
function patchCollectionPrototype( prototype: any )
{
    prototype.contains = function<T>( value: T ): boolean { return this.r1( value ); }
    prototype.size = function<T>(): number { return this.c1(); }
}

patchCollectionPrototype( (extend.$_$ as any).e4.prototype )
patchCollectionPrototype( (extend.$_$ as any).h4.prototype )

const EmptyListPrototype = Object.getPrototypeOf( extend.$_$.p5() )
EmptyListPrototype.contains = function<T>( value: T ): boolean { return this.r1( value ); }
EmptyListPrototype.size = function<T>(): number { return this.c1(); }
EmptyListPrototype.toArray = function<T>(): T[] { return []; }

const EmptySetPrototype = Object.getPrototypeOf( extend.$_$.r5() )
EmptySetPrototype.contains = function<T>( value: T ): boolean { return this.r1( value ); }
EmptySetPrototype.size = function<T>(): number { return this.c1(); }
EmptySetPrototype.toArray = function<T>(): T[] { return []; }

const HashMapPrototype = (extend.$_$ as any).g4.prototype
HashMapPrototype.get = function<K, V>( key: K ): V { return this.s2( key ); }
Object.defineProperty( HashMapPrototype, "keys", {
    get: function keys() { return this.g5(); }
} );
Object.defineProperty( HashMapPrototype, "values", {
    get: function values() { return this.h5(); }
} );


// Export facade.
export default kotlinExport
