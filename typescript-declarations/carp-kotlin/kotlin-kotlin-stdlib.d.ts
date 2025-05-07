declare module "@cachet/kotlin-kotlin-stdlib"
{
    namespace $_$
    {
        interface Long
        {
            // toNumber
            h1(): number
        }
        function toLong( number: number ): Long

        class Pair<K, V>
        {
            constructor( first: K, second: V )

            // first
            me_1: K

            // second
            ne_1: V
        }

        interface Collection<T>
        {
            // contains
            j1( value: T ): boolean

            // size
            m(): number

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
            g2( key: K ): V

            // keys
            h2(): Set<K>

            // values
            i2(): Collection<V>
        }
        interface HashMap<K, V> extends Map<K, V> {}
        function mapOf_0<K, V>( pairs: Pair<K, V>[] ): Map<K, V>

        interface Duration extends Long {}
        interface DurationCompanion
        {
            // parseIsoString
            ii(): Duration

            // ZERO
            fi_1: Duration

            // INFINITE
            gi_1: Duration
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
