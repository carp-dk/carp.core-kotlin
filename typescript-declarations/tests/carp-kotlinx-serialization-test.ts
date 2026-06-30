import VerifyModule from './VerifyModule.js'

import { expect } from 'chai'
import kotlinx from '@cachet/carp-kotlinx-serialization'
import Json = kotlinx.serialization.json.Json


describe( "kotlinx-serialization", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            [ "Json", Json.Default ]
        ]

        const moduleVerifier = new VerifyModule(
            '@cachet/kotlinx-serialization-kotlinx-serialization-json',
            './carp-kotlinx-serialization/kotlinx-serialization-kotlinx-serialization-json.d.ts',
            instances
        )
        await moduleVerifier.verify()
    } )
} )
