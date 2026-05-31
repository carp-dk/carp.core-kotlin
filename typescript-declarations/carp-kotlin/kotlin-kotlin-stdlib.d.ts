declare module "@cachet/kotlin-kotlin-stdlib"
{
    namespace $_$
    {
        type Long = bigint

        interface Collection<T>
        {
            contains( value: T ): boolean
            size(): number
            toArray(): Array<T>
        }

        interface List<T> extends Collection<T> {}
        interface Set<T> extends Collection<T> {}
        interface Map<K, V>
        {
            get( key: K ): V
            keys: Set<K>
            values: Collection<V>
        }

        type Duration = Long
        interface DurationCompanion
        {
            // parseIsoString
            lg( isoDuration: string ): Duration

            // ZERO
            eg_1: Duration

            // INFINITE
            hg_1: Duration
        }
        function l(): DurationCompanion
    }

    namespace kotlin
    {
        class Pair<K, V>
        {
            constructor( first: K, second: V )
            first: K
            second: V
        }

        namespace collections
        {
            type KtList<T> = { readonly __doNotUseOrImplementIt: any, asJsReadonlyArrayView(): Array<T> }
            type KtSet<T> = { readonly __doNotUseOrImplementIt: any, asJsReadonlySetView(): globalThis.Set<T> }
            type KtMap<K, V> = { readonly __doNotUseOrImplementIt: any, asJsReadonlyMapView(): globalThis.Map<K, V> }
            const KtList: { fromJsArray<T>( array: T[] ): KtList<T> }
            const KtSet: { fromJsSet<T>( set: globalThis.Set<T> ): KtSet<T> }
            const KtMap: { fromJsMap<K, V>( map: globalThis.Map<K, V> ): KtMap<K, V> }
        }
    }
}
