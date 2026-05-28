/// <reference path="kotlinx-serialization-kotlinx-serialization-core.d.ts" />
/// <reference path="kotlinx-serialization-kotlinx-serialization-json.d.ts" />
import extendCore from "@cachet/kotlinx-serialization-kotlinx-serialization-core"
import extendJson from "@cachet/kotlinx-serialization-kotlinx-serialization-json"


// Facade with better method names and type conversions for internal types.
export namespace kotlinx.serialization
{
    export function getSerializer( type: any )
    {
        const serializer = type.Companion && type.Companion.kv
            ? type.Companion.kv( [] )
            : type.$metadata$.associatedObjects[ 0 ]()
        return serializer.kv ? serializer.kv( [] ) : serializer
    }
}
export namespace kotlinx.serialization.json
{
    export interface Json
    {
        encodeToString( serializer: any, value: any ): string
        decodeFromString( serializer: any, string: string ): any
    }
    export namespace Json
    {
        export const Default: Json = extendJson.$_$.a() as unknown as Json
    }
}
export namespace kotlinx.serialization.builtins
{
    export const ListSerializer: (serializer: any) => any = extendCore.$_$.ListSerializer
    export const MapSerializer: (keySerializer: any, valueSerializer: any) => any = extendCore.$_$.MapSerializer
    export const SetSerializer: (serializer: any) => any = extendCore.$_$.SetSerializer
}

// Implement base interfaces in internal types.
const JsonPrototype = Object.getPrototypeOf( kotlinx.serialization.json.Json.Default ) as any
JsonPrototype.encodeToString = function( serializer: any, value: any ): string
{
    return this.e1l( serializer, value );
};
JsonPrototype.decodeFromString = function( serializer: any, string: string ): any
{
    return this.f1l( serializer, string );
};


// Export facade.
export default kotlinx
