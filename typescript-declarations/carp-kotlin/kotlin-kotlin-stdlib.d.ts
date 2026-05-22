declare module "@cachet/kotlin-kotlin-stdlib"
{
    namespace $_$
    {
        type Long = bigint
        function toLong( number: number ): Long

        class Pair<K, V>
        {
            constructor( first: K, second: V )

            // first
            od_1: K

            // second
            pd_1: V
        }

        interface Collection<T>
        {
            // contains
            g2( value: T ): boolean

            // size
            o(): number

            toArray(): Array<T>
        }

        interface List<T> extends Collection<T> {}
        interface EmptyList<T> extends List<T> {}
        interface AbstractMutableList<T> extends List<T> {}
        function listOf_0<T>( elements: T[] ): List<T>

        interface Set<T> extends Collection<T> {}
        interface EmptySet<T> extends Set<T> {}
        interface HashSet<T> extends Set<T> {}
        function setOf_0<T>( elements: T[] ): Set<T>

        interface Map<K, V>
        {
            // get
            d2( key: K ): V

            // keys
            z1(): Set<K>

            // values
            a2(): Collection<V>
        }
        interface HashMap<K, V> extends Map<K, V> {}
        function mapOf_0<K, V>( pairs: Pair<K, V>[] ): Map<K, V>

        type Duration = Long
        interface DurationCompanion
        {
            // parseIsoString
            kh( isoDuration: string ): Duration

            // ZERO
            hh_1: Duration

            // INFINITE
            ih_1: Duration
        }
        function Companion_getInstance_20(): DurationCompanion
        function _Duration___get_inWholeMilliseconds__impl__msfiry(duration: Duration): Long
        function _Duration___get_inWholeMicroseconds__impl__8oe8vv(duration: Duration): Long
    }

    namespace kotlin
    {
        namespace collections
        {
            const KtList: any
            const KtSet: any
            const KtMap: any
        }
    }
}
