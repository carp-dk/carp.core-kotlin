package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.domain.createComplexRecruitment
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/**
 * Tests for implementations of [ParticipantRepository].
 */
interface ParticipantRepositoryTest
{
    fun createRepository(): ParticipantRepository


    @Test
    fun adding_recruitment_and_retrieving_it_succeeds() = runTest {
        val repo = createRepository()

        val studyId = UUID.randomUUID()
        val recruitment = Recruitment( studyId )
        repo.addRecruitment( recruitment )
        val retrieved = repo.getRecruitment( studyId )

        assertEquals( recruitment.getSnapshot(), retrieved?.getSnapshot() )
    }

    @Test
    fun addRecruitment_for_existing_studyId_fails() = runTest {
        val repo = createRepository()
        val recruitment = Recruitment( UUID.randomUUID() )
        repo.addRecruitment( recruitment )

        assertFailsWith<IllegalArgumentException> { repo.addRecruitment( recruitment ) }
    }

    @Test
    fun getRecruitment_returns_null_when_not_found() = runTest {
        val repo = createRepository()

        val unknownId = UUID.randomUUID()
        assertNull( repo.getRecruitment( unknownId ) )
    }

    @Test
    fun getRecruitmentWithParticipantGroup_returns_matching_recruitment() = runTest {
        val repo = createRepository()
        val recruitment = createComplexRecruitment()
        repo.addRecruitment( recruitment )
        val groupId = recruitment.participantGroups.keys.first()

        val retrieved = repo.getRecruitmentWithParticipantGroup( groupId )

        assertEquals( recruitment.getSnapshot(), retrieved?.getSnapshot() )
    }

    @Test
    fun getRecruitmentWithParticipantGroup_returns_null_when_not_found() = runTest {
        val repo = createRepository()

        val unknownId = UUID.randomUUID()
        assertNull( repo.getRecruitmentWithParticipantGroup( unknownId ) )
    }

    @Test
    fun updateRecruitment_succeeds() = runTest {
        val repo = createRepository()
        val recruitment = Recruitment( UUID.randomUUID() )
        repo.addRecruitment( recruitment )

        // TODO: Add and verify modifications once recruitment can be modified.
        repo.updateRecruitment( recruitment )
        val updatedRecruitment = repo.getRecruitment( recruitment.studyId )
        assertNotNull( updatedRecruitment )
    }

    @Test
    fun updateRecruitment_for_nonexisting_studyId_fails() = runTest {
        val repo = createRepository()

        val unknownRecruitment = Recruitment( UUID.randomUUID() )
        assertFailsWith<IllegalArgumentException> { repo.updateRecruitment( unknownRecruitment ) }
    }

    @Test
    fun removeStudy_succeeds() = runTest {
        val repo = createRepository()
        val studyId = UUID.randomUUID()
        val recruitment = Recruitment( studyId )
        repo.addRecruitment( recruitment )

        val isRemoved = repo.removeStudy( studyId )

        assertTrue( isRemoved )
        assertNull( repo.getRecruitment( studyId ) )
    }

    @Test
    fun removeStudy_returns_false_when_study_not_present() = runTest {
        val repo = createRepository()

        val isRemoved = repo.removeStudy( UUID.randomUUID() )

        assertFalse( isRemoved )
    }
}
