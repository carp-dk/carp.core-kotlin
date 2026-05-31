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
    export interface KtList<T>
    {
        readonly __doNotUseOrImplementIt: any
        asJsReadonlyArrayView(): Array<T>
    }
    export interface KtSet<T>
    {
        readonly __doNotUseOrImplementIt: any
        asJsReadonlySetView(): globalThis.Set<T>
    }
    export interface KtMap<K, V>
    {
        readonly __doNotUseOrImplementIt: any
        asJsReadonlyMapView(): globalThis.Map<K, V>
    }
    export interface List<T> extends KtList<T>, Collection<T> {}
    export interface Set<T> extends KtSet<T>, Collection<T> {}
    export interface Map<K, V> extends KtMap<K, V>
    {
        get( key: K ): V
        keys: Set<K>
        values: Collection<V>
    }
    export const listOf: <T>(array: T[]) => List<T> = KtList.fromJsArray as any
    export const setOf =
        function<T>(array: T[]): Set<T> { return KtSet.fromJsSet( new globalThis.Set( array ) ) as Set<T> }
    export const mapOf =
        function<K, V>( pairs: kotlinExport.Pair<K, V>[] ): Map<K, V>
        {
            return KtMap.fromJsMap( new globalThis.Map( pairs.map( (pair) => [ pair.first, pair.second ] ) ) ) as Map<K, V>
        }
}
export namespace kotlinExport.time
{
    export type Duration = bigint
    export namespace Duration
    {
        export const Companion: any = extend.$_$.l()
        export const parseIsoString: (isoDuration: string) => Duration = Companion.kg
        export const ZERO: Duration = Companion.dg_1
        export const INFINITE: Duration = Companion.hg_1
    }
}


// Implement facade collection interfaces with Kotlin's native exported collection views.
function patchCollectionPrototype(
    prototype: any,
    getView: ( collection: any ) => { has?: Function, includes?: Function, length?: number, size?: number }
)
{
    prototype.contains = function<T>( value: T ): boolean
    {
        const view = getView( this )
        return view.includes?.( value ) ?? view.has?.( value )
    }
    prototype.size = function(): number
    {
        const view = getView( this )
        return view.size ?? view.length ?? 0
    }
}

const ListPrototype = Object.getPrototypeOf( kotlinExport.collections.KtList.fromJsArray( [] ) )
patchCollectionPrototype( ListPrototype, ( list ) => list.asJsReadonlyArrayView() )
ListPrototype.toArray = function<T>(): T[] { return Array.from( this.asJsReadonlyArrayView() ) }

const SetPrototype = Object.getPrototypeOf( kotlinExport.collections.KtSet.fromJsSet( new globalThis.Set() ) )
patchCollectionPrototype( SetPrototype, ( set ) => set.asJsReadonlySetView() )
SetPrototype.toArray = function<T>(): T[] { return Array.from( this.asJsReadonlySetView() ) }

const MapPrototype = Object.getPrototypeOf( kotlinExport.collections.KtMap.fromJsMap( new globalThis.Map() ) )
MapPrototype.get = function<K, V>( key: K ): V { return this.asJsReadonlyMapView().get( key ) }
Object.defineProperty( MapPrototype, "keys", {
    get: function keys() { return kotlinExport.collections.setOf( Array.from( this.asJsReadonlyMapView().keys() ) ) }
} );
Object.defineProperty( MapPrototype, "values", {
    get: function values() { return kotlinExport.collections.listOf( Array.from( this.asJsReadonlyMapView().values() ) ) }
} );


// Export facade.
export default kotlinExport
