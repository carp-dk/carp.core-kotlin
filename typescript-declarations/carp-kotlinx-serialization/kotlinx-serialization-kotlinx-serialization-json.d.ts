declare module "@cachet/kotlinx-serialization-kotlinx-serialization-json"
{
    namespace $_$
    {
        interface Json
        {
            // encodeToString
            x1l( serializer: any, instance: any ): string

            // decodeFromString
            y1l( serializer: any, string: string ): string
        }
        function Default_getInstance(): Json
    }
}
