import { expect } from 'chai'
import kotlinx from '@cachet/carp-kotlinx-datetime'
import Clock = kotlinx.datetime.Clock


describe( "kotlinx-datetime", () => {
    describe( "Clock", () => {
        it( "now succeeds", () => {
            const now = Clock.System.now()
            expect( now ).not.undefined
        } )
    } )

    describe( "Instant", () => {
        it( "toEpochMilliseconds succeeds", () => {
            const now = Clock.System.now()

            expect( now.toEpochMilliseconds() ).not.undefined
        } )
    } )
} )
