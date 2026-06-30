import { expect } from 'chai'

import kotlin from '@cachet/carp-kotlin'
import Nullable = kotlin.Nullable

import carp from '@cachet/carp-deployments-core'
import dk = carp.dk

import KtMap = carp.kotlin.collections.KtMap

import common = dk.cachet.carp.common
import NamespacedId = common.application.NamespacedId
import Data = common.application.data.Data
import CarpInputDataTypes = common.application.data.input.CarpInputDataTypes
import Sex = common.application.data.input.Sex
import UUID = common.application.UUID

import deployments = dk.cachet.carp.deployments
import ParticipationServiceRequest = deployments.infrastructure.ParticipationServiceRequest


describe( "carp-deployments-core", () => {
    describe( "ParticipationServiceRequest", () => {
        it( "can initialize SetParticipantData", () => {
            const inputByParticipantRole: string | null = null // Shorter alternative to using `Nullable<string>`.
            const participantDataMap = new Map<NamespacedId, Nullable<Data>>( [ // So, `Map<NamespacedId, Data | null>` would work as well.
                [CarpInputDataTypes.SEX, Sex.Male],
            ] );
            const participantData = KtMap.fromJsMap( participantDataMap )
            const setParticipantData = new ParticipationServiceRequest.SetParticipantData(
                UUID.Companion.randomUUID(),
                participantData,
                inputByParticipantRole
            )
        } )
    } )
} )
