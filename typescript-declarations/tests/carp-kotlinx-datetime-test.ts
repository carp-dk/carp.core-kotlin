import { expect } from 'chai'
import kotlin from '@cachet/carp-kotlin'
import kotlinx from '@cachet/carp-kotlinx-datetime'
import Clock = kotlinx.datetime.Clock


describe( "kotlinx-datetime compatibility facade", () => {
    it( "forwards Clock.System to kotlin.time", () => {
        expect( Clock.System ).equals( kotlin.time.Clock.System )
    } )

    it( "forwards Instant from Clock.System.now", () => {
        const instant: kotlinx.datetime.Instant = Clock.System.now()
        const stdlibInstant: kotlin.time.Instant = instant

        expect( stdlibInstant.toEpochMilliseconds() > 0 ).true
    } )
} )
