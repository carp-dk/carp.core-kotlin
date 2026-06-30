declare module "@cachet/kotlin-kotlin-stdlib"
{
    namespace $_$
    {
        class Pair<K, V>
        {
            constructor( first: K, second: V )

            // first
            first: K

            // second
            second: V
        }

        interface Collection<T>
        {
            toArray(): Array<T>
        }

        interface List<T> extends Collection<T>, kotlin.collections.KtList<T> {}
        interface EmptyList<T> extends List<T> {}
        interface AbstractMutableList<T> extends List<T> {}

        interface Set<T> extends Collection<T>, kotlin.collections.KtSet<T> {}
        interface EmptySet<T> extends Set<T> {}
        interface HashSet<T> extends Set<T> {}

        interface Map<K, V> extends kotlin.collections.KtMap<K, V> {}
        interface HashMap<K, V> extends Map<K, V> {}

        type Duration = bigint
        interface DurationCompanion
        {
            // parseIsoString
            tg( isoDuration: string ): Duration

            // ZERO
            mg_1: Duration

            // INFINITE
            ng_1: Duration
        }
        function Companion_getInstance_17(): DurationCompanion
        function _Duration___get_inWholeMilliseconds__impl__msfiry( duration: Duration ): bigint
        function _Duration___get_inWholeMicroseconds__impl__8oe8vv( duration: Duration ): bigint
        function Duration__toString_impl_8d916b( duration: Duration ): string

        interface System
        {
            // now
            lg(): Instant
        }
        const System_instance: System

        interface Instant
        {
            // toEpochMilliseconds
            ih(): bigint
        }
    }

    namespace kotlin
    {
        namespace collections
        {
            namespace KtList
            {
                function fromJsArray<T>( elements: T[] ): KtList<T>
            }
            interface KtList<T>
            {
                asJsReadonlyArrayView(): readonly T[]
            }
            namespace KtSet
            {
                function fromJsSet<T>( elements: Set<T> ): KtSet<T>
            }
            interface KtSet<T>
            {
                asJsReadonlySetView (): Readonly<Set<T>>
            }
            namespace KtMap
            {
                function fromJsMap<K, V>( entries: ReadonlyMap<K, V> ): KtMap<K, V>
            }
            interface KtMap<K, V>
            {
                asJsReadonlyMapView(): ReadonlyMap<K, V>
            }
        }
    }
}
