declare module "@cachet/kotlinx-serialization-kotlinx-serialization-json"
{
    namespace $_$
    {
        interface Json
        {
            // encodeToString
            s1m( serializer: any, instance: any ): string

            // decodeFromString
            t1m( serializer: any, string: string ): string
        }
        function Default_getInstance(): Json
    }
}
