declare module "@cachet/kotlinx-serialization-kotlinx-serialization-json"
{
    namespace $_$
    {
        interface Json
        {
            // encodeToString
            u1l( serializer: any, instance: any ): string

            // decodeFromString
            v1l( serializer: any, string: string ): string
        }
        function Default_getInstance(): Json
    }
}
