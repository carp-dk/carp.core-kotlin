import VerifyModule from './VerifyModule.js'

import { expect } from 'chai'
import kotlinx from '@cachet/carp-kotlinx-datetime'
import Clock = kotlinx.datetime.Clock
import kotlin from '@cachet/carp-kotlin'
import toLong = kotlin.toLong


describe( "kotlinx-datetime", () => {
    it( "verify module declarations", async () => {
        const instances: any[] = [
            Clock.System,
            Clock.System.now()
        ]

        const moduleVerifier = new VerifyModule(
            '@cachet/Kotlin-DateTime-library-kotlinx-datetime',
            './carp-kotlinx-datetime/Kotlin-DateTime-library-kotlinx-datetime.d.ts',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "Clock", () => {
        it( "now succeeds", () => {
            const now = Clock.System.now()
            expect( now ).not.undefined
        } )
    } )

    describe( "Instant", () => {
        it( "toEpochMilliseconds succeeds", () => {
            const now = Clock.System.now()

            const zero = toLong( 0 )
            expect( now.toEpochMilliseconds() > zero ).true
        } )
    } )
} )
