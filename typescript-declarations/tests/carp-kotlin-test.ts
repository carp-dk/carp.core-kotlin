import VerifyModule from './VerifyModule.js'

import { expect } from 'chai'
import kotlin from '@cachet/carp-kotlin'
import toLong = kotlin.toLong
import Long = kotlin.Long
import Pair = kotlin.Pair
import Duration = kotlin.time.Duration
import KtList = kotlin.collections.KtList
import KtSet = kotlin.collections.KtSet
import KtMap = kotlin.collections.KtMap
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
            [ "KtList", KtList.fromJsArray( [ 42 ] ) ],
            [ "List", list ],
            [ "EmptyList", listOf<number>( [] ) ],
            [ "AbstractMutableList", list ],
            [ "KtSet", KtSet.fromJsSet( new Set( [ 42 ] ) ) ],
            [ "Set", set ],
            [ "EmptySet", setOf<number>( [] ) ],
            [ "HashSet", set ],
            [ "KtMap", KtMap.fromJsMap( new Map( [ [ 42, "answer" ] ] ) ) ],
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

        it( "can assign between bigint and Long for backwards compatibility", () => {
            const big = 42n
            const bigAsLong: Long = big
            const longAsBig: bigint = bigAsLong
            
            expect( bigAsLong ).equals( big )
            expect( longAsBig ).equals( big )
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

    describe( "KtList", () => {
        it( "fromJsArray and back to array succeeds", () => {
            const numbers = [ 1, 2, 3 ]
            const numbersList = KtList.fromJsArray( numbers )

            const numbersArray = numbersList.asJsReadonlyArrayView()
            expect( numbersArray[ 0 ] ).equals( 1 )

            // The view is a proxy on which Chai's deep equals fails without applying the spread operator.
            expect( [...numbersArray] ).deep.equals( numbers )
        } )

        it( "toArray provides a mutable copy of the original collection", () => {
            const numbers = [ 1, 2, 3 ]
            const numbersList = KtList.fromJsArray( numbers )

            const newArray = numbersList.toArray()
            newArray[ 0 ] = 42
            
            expect( numbers[ 0 ] ).equals( 1 )
            expect( newArray[ 0 ] ).equals( 42 )
        } )

        it( "includes succeeds", () => {
            const kotlinList = KtList.fromJsArray( [ 0, 42, 50 ] )
            const includesAnswer = kotlinList.asJsReadonlyArrayView()

            expect( includesAnswer.includes( 42 ) ).is.true
            expect( includesAnswer.includes( 100 ) ).is.false
        } )

        it( "length succeeds", () => {
            const kotlinList = KtList.fromJsArray( [ 1, 2, 3 ] )
            const three = kotlinList.asJsReadonlyArrayView()

            expect( three.length ).equals( 3 )
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

    describe( "KtMap", () => {
        it( "fromJsMap and back to map succeeds", () => {
            const answers = new Map( [ [ "answer", 42 ] ] )
            const answersMap = KtMap.fromJsMap( answers )

            expect( answersMap.asJsReadonlyMapView().get( "answer" ) ).equals( 42 )
        } )
    } )

    describe( "Duration", () => {
        it( "parseIsoString succeeds", () => {
            const oneSeconds = Duration.parseIsoString( "PT1S" )
            expect( oneSeconds.inWholeMilliseconds ).equals( 1000n )
        } )

        it( "ZERO and INFINITE succeeds", () => {
            const zero = Duration.ZERO
            expect( zero.inWholeMilliseconds ).equals( 0n )
            expect( zero.inWholeMicroseconds ).equals( 0n )

            const infinite = Duration.INFINITE
            expect( infinite.toDurationString() ).equals( "Infinity" )
        } )
    } )
} )
