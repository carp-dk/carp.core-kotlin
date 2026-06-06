import VerifyModule from './VerifyModule.js'

import { expect } from 'chai'
import kotlin from '@cachet/carp-kotlin'
import toLong = kotlin.toLong
import Pair = kotlin.Pair
import Duration = kotlin.time.Duration
import listOf = kotlin.collections.listOf
import setOf = kotlin.collections.setOf
import mapOf = kotlin.collections.mapOf


describe( "kotlin", () => {
    it( "verify module declarations", async () => {
        const list = listOf( [ 42 ] )
        const set = setOf( [ 42 ] )
        const map = mapOf( [ new Pair( 42, "answer" ) ] )
        const instances: any[] = [
            toLong( 42 ),
            new Pair( 42, "answer" ),
            [ "Collection", list ],
            [ "List", list ],
            [ "EmptyList", listOf<number>( [] ) ],
            [ "AbstractMutableList", list ],
            [ "Set", set ],
            [ "EmptySet", setOf<number>( [] ) ],
            [ "HashSet", set ],
            [ "Map", map ],
            [ "HashMap", map ],
            [ "DurationCompanion", Duration.Companion ],
            [ "Duration", Duration.ZERO ]
        ]

        const moduleVerifier = new VerifyModule(
            '@cachet/kotlin-kotlin-stdlib',
            './carp-kotlin/kotlin-kotlin-stdlib.d.ts',
            instances
        )
        await moduleVerifier.verify()
    } )

    describe( "Long", () => {
        it( "toLong and back toNumber equals", () => {
            const answer = toLong( 42 )
            const answerAsNumber: Number = answer.toNumber()

            expect( answerAsNumber ).equals( 42 )
        } )
    } )

    describe( "Pair", () => {
        it( "can access first and second", () => {
            const answer = new Pair( 42, "answer" )
            expect( answer.first ).equals( 42 )
            expect( answer.second ).equals( "answer" )
        } )
    } )

    describe( "List", () => {
        it( "listOf and back toArray succeeds", () => {
            const numbers = [ 1, 2, 3 ]
            const numbersList = listOf( numbers )
            const numbersArray = numbersList.toArray()

            expect( numbersArray ).deep.equals( numbers )
        } )

        it( "contains succeeds", () => {
            const includesAnswer = listOf( [ 0, 42, 50 ] )

            expect( includesAnswer.contains( 42 ) ).is.true
            expect( includesAnswer.contains( 100 ) ).is.false
        } )

        it( "size succeeds", () => {
            const three = listOf( [ 1, 2, 3 ] )

            expect( three.size() ).equals( 3 )
        } )

        it( "empty list succeeds", () => {
            const emptyList = listOf<number>( [] )

            expect( emptyList.toArray() ).deep.equals( [] )
            expect( emptyList.contains( 42 ) ).is.false
            expect( emptyList.size() ).equals( 0 )
        } )
    } )

    describe( "Set", () => {
        it( "setOf and conversion back to array succeeds", () => {
            const answers = [ 42 ]
            const answersSet = setOf( answers )
            const answersArray = answersSet.toArray()

            expect( answersArray ).deep.equals( answers )
        } )

        it( "contains succeeds", () => {
            const includesAnswer = setOf( [ 0, 42, 50 ] )

            expect( includesAnswer.contains( 42 ) ).is.true
            expect( includesAnswer.contains( 100 ) ).is.false
        } )

        it( "size succeeds", () => {
            const three = setOf( [ 1, 2, 3 ] )

            expect( three.size() ).equals( 3 )
        } )

        it( "empty set succeeds", () => {
            const emptySet = setOf<number>( [] )

            expect( emptySet.toArray() ).deep.equals( [] )
            expect( emptySet.contains( 42 ) ).is.false
            expect( emptySet.size() ).equals( 0 )
        } )
    } )

    describe( "Map", () => {
        it( "get succeeds", () => {
            const answers = mapOf( [ new Pair( "answer", 42 ) ] )
            expect( answers.get( "answer" ) ).equals( 42 )
        } )

        it( "mapOf keys and entries accessible", () => {
            const answers = [ new Pair( "answer", 42 ) ]
            const answersMap = mapOf( answers )

            expect( answersMap.keys.toArray() ).deep.equals( [ "answer" ] )
            expect( answersMap.values.toArray() ).deep.equals( [ 42 ] )
        } )
    } )

    describe( "Duration", () => {
        it( "parseIsoString succeeds", () => {
            const oneSeconds = Duration.parseIsoString( "PT1S" )
            expect( oneSeconds.inWholeMilliseconds ).equals( 1000 )
        } )

        it( "ZERO and INFINITE succeeds", () => {
            const zero = Duration.ZERO
            expect( zero.inWholeMilliseconds ).equals( 0 )
            expect( zero.inWholeMicroseconds ).equals( 0 )

            const infinite = Duration.INFINITE
            expect( infinite.toDurationString() ).equals( "Infinity" )
        } )
    } )
} )
