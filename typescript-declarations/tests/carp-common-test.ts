import { expect } from 'chai'

import kotlin from '@cachet/carp-kotlin'
import Duration = kotlin.time.Duration

import kotlinx from '@cachet/carp-kotlinx-serialization'
import getSerializer = kotlinx.serialization.getSerializer

import carp from '@cachet/carp-common'

import KtSet = carp.kotlin.collections.KtSet

import dk = carp.dk
import Trilean = dk.cachet.carp.common.application.Trilean
import toTrilean = dk.cachet.carp.common.application.toTrilean
import EmailAddress = dk.cachet.carp.common.application.EmailAddress
import NamespacedId = dk.cachet.carp.common.application.NamespacedId
import AccountIdentity = dk.cachet.carp.common.application.users.AccountIdentity
import AssignedTo = dk.cachet.carp.common.application.users.AssignedTo
import EmailAccountIdentity = dk.cachet.carp.common.application.users.EmailAccountIdentity
import ExpectedParticipantData = dk.cachet.carp.common.application.users.ExpectedParticipantData
import ParticipantAttribute = dk.cachet.carp.common.application.users.ParticipantAttribute
import CarpInputDataTypes = dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import Text = dk.cachet.carp.common.application.data.input.elements.Text
import CustomInput = dk.cachet.carp.common.application.data.input.CustomInput
import IntervalSamplingConfiguration = dk.cachet.carp.common.application.sampling.IntervalSamplingConfiguration
import JSON = dk.cachet.carp.common.infrastructure.serialization.JSON


describe( "carp-common", () => {
    describe( "Trilean", () => {
        it( "has values TRUE, FALSE, UNKNOWN", () => {
            const trileanValues = Trilean.values()
            expect( trileanValues ).to.have.members( [ Trilean.TRUE, Trilean.FALSE, Trilean.UNKNOWN ] )
        } )

        it( "toTrilean works", () => {
            expect( toTrilean( true ) ).equals( Trilean.TRUE )
            expect( toTrilean( false ) ).equals( Trilean.FALSE )
        } )
    } )

    describe( "EmailAccountIdentity", () => {
        it( "can initialize from email address", () => {
            const identity = new EmailAccountIdentity( new EmailAddress( "test@test.com" ) )
            expect( identity.emailAddress ).instanceOf( EmailAddress )
        } )

        it( "can initialize from string", () => {
            const identity = EmailAccountIdentity.create( "test@test.com" )
            expect( identity.emailAddress ).instanceOf( EmailAddress )
        } )

        it( "can cast to AccountIdentity base type", () => {
            const identity: AccountIdentity = EmailAccountIdentity.create( "test@test.com" )
        } )
    } )

    describe( "ParticipantAttribute", () => {
        const attribute = new ParticipantAttribute.CustomParticipantAttribute( new Text( "Prompt" ) )

        it( "getInputElement works", () => {
            const inputElement = attribute.getInputElement( CarpInputDataTypes )
            expect( inputElement ).instanceOf( Text )
        } )

        it( "isValidInput works", () => {
            const isNumberValid = attribute.isValidInput( CarpInputDataTypes, 42 )
            expect( isNumberValid ).is.false

            const isStringValid = attribute.isValidInput( CarpInputDataTypes, "Steven" )
            expect( isStringValid ).is.true
        } )

        it( "inputToData works", () => {
            const data = attribute.inputToData( CarpInputDataTypes, "Steven" )
            const customInput = data as CustomInput
            expect( customInput.input ).equals( "Steven" )
        } )
    } )

    describe( "ExpectedParticipantData", () => {
        it( "can serialize and deserialize", () => {
            const roles = new Set( [ "Roles are added" ] );
            const expectedData = new ExpectedParticipantData(
                new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ),
                new AssignedTo.Roles( KtSet.fromJsSet( roles ) )
            )

            const serializer = getSerializer( ExpectedParticipantData )
            const serialized = JSON.encodeToString( serializer, expectedData )
            const deserialized = JSON.decodeFromString( serializer, serialized ) as ExpectedParticipantData
            expect( deserialized.equals( expectedData ) ).is.true
        } )
    } )

    describe( "IntervalSamplingConfiguration", () => {
        it( "can construct", () => {
            const config = new IntervalSamplingConfiguration( Duration.parseIsoString( "PT1S" ) )
            expect( config.interval.inWholeMilliseconds ).equals( 1000n )
        } )
    } )
} )
