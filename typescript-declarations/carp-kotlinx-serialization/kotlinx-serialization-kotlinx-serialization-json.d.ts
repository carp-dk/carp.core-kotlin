declare module "@cachet/kotlinx-serialization-kotlinx-serialization-json"
{
    namespace $_$
    {
        interface Json
        {
            // encodeToString
            b1l( serializer: any, instance: any ): string

            // decodeFromString
            c1l( serializer: any, string: string ): string
        }
        function Default_getInstance(): Json
    }
}
