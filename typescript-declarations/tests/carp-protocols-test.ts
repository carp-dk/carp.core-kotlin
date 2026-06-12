import { expect } from 'chai'

import kxs from '@cachet/carp-kotlinx-serialization'
import getSerializer = kxs.serialization.getSerializer

import kotlin from '@cachet/carp-kotlin'
import Clock = kotlin.time.Clock

import carp from '@cachet/carp-protocols-core'
import dk = carp.dk

import common = dk.cachet.carp.common
import JSON = common.infrastructure.serialization.JSON
import UUID = common.application.UUID

import protocols = dk.cachet.carp.protocols
import StudyProtocolSnapshot = protocols.application.StudyProtocolSnapshot
import ProtocolServiceRequest = protocols.infrastructure.ProtocolServiceRequest

const serializedSnapshot = `{"id":"ad4ca03a-6f69-4a95-8701-488dc511925b","createdOn":"2023-01-30T21:02:56.068710100Z","version":0,"ownerId":"27879e75-ccc1-4866-9ab3-4ece1b735052","name":"Test protocol","description":"Test description","primaryDevices":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration","isPrimaryDevice":true,"roleName":"Stub primary device"}],"connectedDevices":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration","roleName":"Stub device"},{"__type":"dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration","isPrimaryDevice":true,"roleName":"Chained primary"},{"__type":"dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration","roleName":"Chained connected"}],"connections":[{"roleName":"Stub device","connectedToRoleName":"Stub primary device"},{"roleName":"Chained primary","connectedToRoleName":"Stub primary device"},{"roleName":"Chained connected","connectedToRoleName":"Chained primary"}],"tasks":[{"__type":"dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration","name":"Task","measures":[{"__type":"dk.cachet.carp.common.application.tasks.Measure.DataStream","type":"dk.cachet.carp.stubpoint"}]}],"triggers":{"0":{"__type":"dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration","sourceDeviceRoleName":"Stub device"}},"taskControls":[{"triggerId":0,"taskName":"Task","destinationDeviceRoleName":"Stub primary device","control":"Start"}],"participantRoles":[{"role":"Role","isOptional":false},{"role":"Optional role","isOptional":true}],"assignedDevices":{"Stub primary device":["Role"]},"expectedParticipantData":[{"attribute":{"__type":"dk.cachet.carp.common.application.users.ParticipantAttribute.DefaultParticipantAttribute","inputDataType":"some.type"}},{"attribute":{"__type":"dk.cachet.carp.common.application.users.ParticipantAttribute.DefaultParticipantAttribute","inputDataType":"dk.cachet.carp.input.sex"},"assignedTo":{"__type":"dk.cachet.carp.common.application.users.AssignedTo.Roles","roleNames":["Role"]}}]}`

describe( "carp-protocols-core", () => {
    describe( "StudyProtocolSnapshot", () => {
        it( "can deserialize", () => {
            const serializer = getSerializer( StudyProtocolSnapshot )
            const parsed = JSON.decodeFromString( serializer, serializedSnapshot )
            expect( parsed ).is.instanceOf( StudyProtocolSnapshot )
        } )

        it( "can deserialize and serialize unknown types", () => {
            const snapshotWithUnknownTypes = serializedSnapshot.replace(
                "dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration",
                "com.unknown.CustomTaskConfiguration"
            )
            const serializer = getSerializer( StudyProtocolSnapshot )

            const parsed = JSON.decodeFromString( serializer, snapshotWithUnknownTypes )
            const serialized = JSON.encodeToString( serializer, parsed )
            
            expect( snapshotWithUnknownTypes ).equals( serialized )
        } )

        it( "can access snapshot properties", () => {
            const id = UUID.Companion.randomUUID()
            const now = Clock.System.now()
            const version = 42
            const snapshot = new StudyProtocolSnapshot( id, now, version, UUID.Companion.randomUUID(), "Stub" )

            expect( snapshot.id ).equals( id )
            expect( snapshot.createdOn ).equals( now )
            expect( snapshot.version ).equals( version )
        } )
    } )

    describe( "ProtocolServiceRequest", () => {
        it( "add request has default version tag", () => {
            const serializer = getSerializer( StudyProtocolSnapshot )
            const snapshot = JSON.decodeFromString( serializer, serializedSnapshot )

            const addProtocol = new ProtocolServiceRequest.Add( snapshot )
            const versionTag = (addProtocol as any).versionTag
            expect( versionTag ).equals( "Initial" )
        } )
    } )
} )
