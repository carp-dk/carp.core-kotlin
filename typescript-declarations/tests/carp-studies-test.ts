import { expect } from 'chai'

import kotlin from '@cachet/carp-kotlin'
import listOf = kotlin.collections.listOf

import kxd from '@cachet/carp-kotlinx-datetime'
import Clock = kxd.datetime.Clock

import kxs from '@cachet/carp-kotlinx-serialization'
import getSerializer = kxs.serialization.getSerializer
import ListSerializer = kxs.serialization.builtins.ListSerializer

import carp from '@cachet/carp-studies-core'
import dk = carp.dk

import KtList = carp.kotlin.collections.KtList
import KtSet = carp.kotlin.collections.KtSet

import common = dk.cachet.carp.common
import UUID = common.application.UUID
import Username = common.application.users.Username
import AssignedTo = common.application.users.AssignedTo
import UsernameAccountIdentity = common.application.users.UsernameAccountIdentity
import JSON = common.infrastructure.serialization.JSON

import deployments = dk.cachet.carp.deployments
import DeviceDeploymentStatus = deployments.application.DeviceDeploymentStatus
import StudyDeploymentStatus = deployments.application.StudyDeploymentStatus
import ParticipantStatus = deployments.application.users.ParticipantStatus
import StudyInvitation = deployments.application.users.StudyInvitation

import studies = dk.cachet.carp.studies
import StudyStatus = studies.application.StudyStatus
import AssignedParticipantRoles = studies.application.users.AssignedParticipantRoles
import Participant = studies.application.users.Participant
import ParticipantGroupStatus = studies.application.users.ParticipantGroupStatus
import participantIds = studies.application.users.participantIds
import participantRoles = studies.application.users.participantRoles
import StudyServiceRequest = studies.infrastructure.StudyServiceRequest
import RecruitmentServiceRequest = studies.infrastructure.RecruitmentServiceRequest


describe( "carp-studies-core", () => {
    describe( "AssignedParticipantRoles", () => {
        it( "getAssigned participantIds and participantRoles works", () => {
            const participant1 = UUID.Companion.randomUUID()
            const participant2 = UUID.Companion.randomUUID()
            const roles = new Set( [ "Test" ] );
            const assigned1 = new AssignedParticipantRoles( participant1, new AssignedTo.Roles( KtSet.fromJsSet( roles ) ) )
            const assigned2 = new AssignedParticipantRoles( participant2, AssignedTo.All )
            const assignedGroup = listOf( [ assigned1, assigned2 ] )
            expect( participantIds( assignedGroup ).asJsReadonlySetView().has( participant1 ) ).is.true
            expect( participantRoles( assignedGroup ).asJsReadonlySetView().has( "Test" ) ).is.true
        } )
    } )

    
    describe( "StudyStatus", () => {
        it ( "can typecheck StudyStatus", () => {
            const configuring = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", Clock.System.now(), null, true, true, false, true )
            const configuringStatus: StudyStatus = configuring
            expect( configuringStatus instanceof StudyStatus.Configuring ).is.true
            expect( configuringStatus instanceof StudyStatus.Live ).is.false

            const live = new StudyStatus.Live( UUID.Companion.randomUUID(), "Test", Clock.System.now(), UUID.Companion.randomUUID(), false, false, true )
            const liveStatus: StudyStatus = live
            expect( liveStatus instanceof StudyStatus.Live ).is.true
            expect( liveStatus instanceof StudyStatus.Configuring ).is.false
        } )
    } )

    describe( "StudyServiceRequest", () => {
        it( "can serialize requests with polymorphic serializer", () => {
            const createStudy = new StudyServiceRequest.CreateStudy(
                UUID.Companion.randomUUID(),
                "Test study",
                "This is a study description",
                new StudyInvitation( "Some study" )
            )

            const serialized = JSON.encodeToString( StudyServiceRequest.Serializer, createStudy )
            expect( serialized ).has.string( "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.CreateStudy" )
        } )

        it( "can serialize getStudiesOverview response", () => {
            const status = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", Clock.System.now(), null, true, true, false, true )
            const statusList = listOf( [ status ] )

            const serializer = ListSerializer( getSerializer( StudyStatus ) )
            expect( serializer ).is.not.undefined
            const serialized = JSON.encodeToString( serializer, statusList )
            expect( serialized ).is.not.not.undefined
        } )
    } )


    describe( "RecruitmentServiceRequest", () => {
        it( "can serialize DeployParticipantGroup", () => {
            const assignedRoles = new Set( [ new AssignedParticipantRoles( UUID.Companion.randomUUID(), AssignedTo.All ) ] );
            const deployGroup = new RecruitmentServiceRequest.InviteNewParticipantGroup(
                UUID.Companion.randomUUID(),
                KtSet.fromJsSet( assignedRoles ),
                "Test group"
            )

            const serializer = RecruitmentServiceRequest.Serializer
            const serialized = JSON.encodeToString( serializer, deployGroup )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize Participant", () => {
            const participant = new Participant( new UsernameAccountIdentity( new Username( "Test" ) ), UUID.Companion.randomUUID() )

            const serializer = getSerializer( Participant )
            const serialized = JSON.encodeToString( serializer, participant )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize ParticipantGroupStatus", () => {
            const deploymentId = UUID.Companion.randomUUID()
            const now = Clock.System.now()
            const emptyDeploymentStatusList = KtList.fromJsArray( [] )
            const emptyParticipantStatusList = KtList.fromJsArray( [] )
            const deploymentStatus = new StudyDeploymentStatus.Running( now, deploymentId, emptyDeploymentStatusList, emptyParticipantStatusList, now )
            const participantsSet = [ new Participant( new UsernameAccountIdentity( new Username( "Test" ) ) ) ]
            const participants = KtSet.fromJsSet( new Set( participantsSet ) )
            const roleAssignmentsSet = new Set( [ new AssignedParticipantRoles( participantsSet[0].id, AssignedTo.All ) ] )
            const roleAssignments = KtSet.fromJsSet( roleAssignmentsSet )
            const group = new ParticipantGroupStatus.Invited( deploymentId, participants, roleAssignments, now, deploymentStatus, "Test group" )

            const serializer = getSerializer( ParticipantGroupStatus )
            const serialized = JSON.encodeToString( serializer, group )
            expect( serialized ).is.not.undefined
        } )
    } )
} )
