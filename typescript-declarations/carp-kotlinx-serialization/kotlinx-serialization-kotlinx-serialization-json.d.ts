declare module "@cachet/kotlinx-serialization-kotlinx-serialization-json"
{
    namespace $_$
    {
        interface Json
        {
            // encodeToString
            t15( serializer: any, instance: any ): string

            // decodeFromString
            u15( serializer: any, string: string ): string
        }
        function Default_getInstance(): Json
    }
}
