package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.studies.domain.users.Recruitment
import dk.cachet.carp.studies.domain.users.RecruitmentSnapshot


class InMemoryParticipantRepository : ParticipantRepository
{
    private val recruitments: MutableMap<UUID, RecruitmentSnapshot> = mutableMapOf()


    /**
     * Add a new [Recruitment] to the repository.
     *
     * @throws IllegalArgumentException when a recruitment with the same studyId already exists.
     */
    override suspend fun addRecruitment( recruitment: Recruitment )
    {
        require( recruitment.studyId !in recruitments )
        recruitments[ recruitment.studyId ] = recruitment.getSnapshot()
    }

    /**
     * Returns the [Recruitment] for the specified [studyId], or null when no recruitment is found.
     */
    override suspend fun getRecruitment( studyId: UUID ): Recruitment? =
        recruitments[ studyId ]?.let { Recruitment.fromSnapshot( it ) }

    /**
     * Returns the [Recruitment] which contains the participant group with the specified [groupId],
     * or null when no recruitment containing the specified participant group exists.
     */
    override suspend fun getRecruitmentWithParticipantGroup( groupId: UUID ): Recruitment? =
        recruitments.values.firstOrNull { groupId in it.participantGroups.keys }
            ?.let { Recruitment.fromSnapshot( it ) }

    /**
     * Update a [Recruitment] which is already stored in this repository.
     *
     * @throws IllegalArgumentException when no previous version of this recruitment is stored in the repository.
     */
    override suspend fun updateRecruitment( recruitment: Recruitment )
    {
        require( recruitment.studyId in recruitments )
        recruitments[ recruitment.studyId ] = recruitment.getSnapshot()
    }

    /**
     * Remove all data (only recruitment for now) for the study with [studyId].
     *
     * @return True when data was removed; false when no data for the study is present in the repository.
     */
    override suspend fun removeStudy( studyId: UUID ): Boolean = recruitments.remove( studyId ) != null
}
