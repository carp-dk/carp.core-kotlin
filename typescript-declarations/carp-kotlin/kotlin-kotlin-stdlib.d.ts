declare module "@cachet/kotlin-kotlin-stdlib"
{
    namespace $_$
    {
        type Long = bigint

        interface Collection<T>
        {
            // contains
            r1( value: T ): boolean

            // size
            c1(): number

            toArray(): Array<T>
        }

        interface List<T> extends Collection<T> {}
        function j6<T>( elements: T[] ): List<T>
        function p5<T>(): List<T>

        interface Set<T> extends Collection<T> {}
        function u6<T>( elements: T[] ): Set<T>
        function r5<T>(): Set<T>

        interface Map<K, V>
        {
            // get
            s2( key: K ): V

            // keys
            g5(): Set<K>

            // values
            h5(): Collection<V>
        }
        function l6<K, V>( pairs: kotlin.Pair<K, V>[] ): Map<K, V>

        type Duration = Long
        interface DurationCompanion
        {
            // parseIsoString
            ng( isoDuration: string ): Duration

            // ZERO
            gg_1: Duration

            // INFINITE
            hg_1: Duration
        }
        function t3(): DurationCompanion
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
            const KtList: any
            const KtSet: any
            const KtMap: any
        }
    }
}
