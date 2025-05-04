/// <reference path="kotlinx-serialization-kotlinx-serialization-core.d.ts" />
/// <reference path="kotlinx-serialization-kotlinx-serialization-json.d.ts" />
import extendCore from "@cachet/kotlinx-serialization-kotlinx-serialization-core"
import extendJson from "@cachet/kotlinx-serialization-kotlinx-serialization-json"


// Facade with better method names and type conversions for internal types.
export namespace kotlinx.serialization
{
    export function getSerializer( type: any ) { return type.Companion.i17() }
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
        export const Default: Json = extendJson.$_$.Default_getInstance()
    }
}
export namespace kotlinx.serialization.builtins
{
    export const ListSerializer: (serializer: any) => any = extendCore.$_$.ListSerializer
    export const MapSerializer: (keySerializer: any, valueSerializer: any) => any = extendCore.$_$.MapSerializer
    export const SetSerializer: (serializer: any) => any = extendCore.$_$.SetSerializer
}


// Augment internal types to implement facade.
declare module "@cachet/kotlinx-serialization-kotlinx-serialization-json"
{
    namespace $_$
    {
        interface Json extends kotlinx.serialization.json.Json {}
        abstract class Json implements kotlinx.serialization.json.Json {}
    }
}


// Implement base interfaces in internal types.
extendJson.$_$.Json.prototype.encodeToString =
    function( serializer: any, value: any ): string
    {
        return this.g15( serializer, value );
    };
extendJson.$_$.Json.prototype.decodeFromString =
    function( serializer: any, string: string ): any
    {
        return this.h15( serializer, string );
    };


// Export facade.
export default kotlinx
