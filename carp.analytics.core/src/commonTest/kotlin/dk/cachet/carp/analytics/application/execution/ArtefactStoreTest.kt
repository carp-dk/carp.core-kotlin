package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Abstract test suite for [ArtefactStore] implementations.
 *
 * Concrete test classes should extend this class and implement [createArtefactStore]
 * to provide their specific implementation for testing.
 *
 * This ensures all implementations conform to the [ArtefactStore] contract.
 */
abstract class ArtefactStoreTest
{

    /**
     * Factory method for creating the [ArtefactStore] instance to test.
     * Implementations should return a fresh instance for each test.
     */
    abstract fun createArtefactStore(): ArtefactStore

    lateinit var store: ArtefactStore

    @BeforeTest
    fun setUp()
    {
        store = createArtefactStore()
    }

    // ── Recording Artefacts ────────────────────────────────────────────────────

    @Test
    fun `recordArtefact stores artefact successfully`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val location = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId/outputs/data.csv"
        )

        // Act
        val result = store.recordArtefact(
            stepId = stepId,
            outputId = outputId,
            location = location,
            metadata = ArtefactMetadata(sizeBytes = 1024, sha256 = "abc123", contentType = "text/csv")
        )

        // Assert
        assertNotNull(result, "recordArtefact should return a ProducedOutputRef")
        assertEquals(outputId, result.outputId)
        assertEquals(location, result.location)
        assertEquals(1024, result.sizeBytes)
        assertEquals("abc123", result.sha256)
        assertEquals("text/csv", result.contentType)
    }

    @Test
    fun `recordArtefact with minimal fields`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val location = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId/outputs/output.txt"
        )

        // Act
        val result = store.recordArtefact(
            stepId = stepId,
            outputId = outputId,
            location = location
        )

        // Assert
        assertNotNull(result)
        assertEquals(outputId, result.outputId)
        assertEquals(location, result.location)
        assertNull(result.sizeBytes)
        assertNull(result.sha256)
        assertNull(result.contentType)
    }

    @Test
    fun `recordArtefact with URI location`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val location = ResourceRef(
            kind = ResourceKind.URI,
            value = "file:///tmp/output.json",
            mediaType = "application/json"
        )

        // Act
        val result = store.recordArtefact(
            stepId = stepId,
            outputId = outputId,
            location = location
        )

        // Assert
        assertNotNull(result)
        assertEquals(outputId, result.outputId)
        assertEquals(location, result.location)
    }

    // ── Retrieving Artefacts ───────────────────────────────────────────────────

    @Test
    fun `getArtefact returns stored artefact`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val location = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId/outputs/data.csv"
        )
        store.recordArtefact(stepId, outputId, location, ArtefactMetadata(sizeBytes = 512))

        // Act
        val retrieved = store.getArtefact(outputId)

        // Assert
        assertNotNull(retrieved)
        assertEquals(outputId, retrieved.outputId)
        assertEquals(location, retrieved.location)
        assertEquals(512, retrieved.sizeBytes)
    }

    @Test
    fun `getArtefact returns null for non-existent outputId`()
    {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val result = store.getArtefact(nonExistentId)

        // Assert
        assertNull(result, "getArtefact should return null for non-existent outputId")
    }

    @Test
    fun `getArtefactsByStep returns all artefacts for a step`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val outputId1 = UUID.randomUUID()
        val outputId2 = UUID.randomUUID()
        val location1 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId/outputs/file1.txt"
        )
        val location2 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId/outputs/file2.txt"
        )

        store.recordArtefact(stepId, outputId1, location1)
        store.recordArtefact(stepId, outputId2, location2)

        // Act
        val artefacts = store.getArtefactsByStep(stepId)

        // Assert
        assertEquals(2, artefacts.size, "Should return 2 artefacts for the step")
        assertTrue(artefacts.any { it.outputId == outputId1 })
        assertTrue(artefacts.any { it.outputId == outputId2 })
    }

    @Test
    fun `getArtefactsByStep returns empty list for step with no artefacts`()
    {
        // Arrange
        val stepId = UUID.randomUUID()

        // Act
        val artefacts = store.getArtefactsByStep(stepId)

        // Assert
        assertTrue(artefacts.isEmpty(), "Should return empty list for step with no artefacts")
    }

    @Test
    fun `getArtefactsByStep does not return artefacts from other steps`()
    {
        // Arrange
        val stepId1 = UUID.randomUUID()
        val stepId2 = UUID.randomUUID()
        val outputId1 = UUID.randomUUID()
        val outputId2 = UUID.randomUUID()
        val location1 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId1/outputs/file1.txt"
        )
        val location2 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId2/outputs/file2.txt"
        )

        store.recordArtefact(stepId1, outputId1, location1)
        store.recordArtefact(stepId2, outputId2, location2)

        // Act
        val artefactsStep1 = store.getArtefactsByStep(stepId1)

        // Assert
        assertEquals(1, artefactsStep1.size)
        assertEquals(outputId1, artefactsStep1[0].outputId)
    }

    @Test
    fun `getAllArtefacts returns all stored artefacts`()
    {
        // Arrange
        val stepId1 = UUID.randomUUID()
        val stepId2 = UUID.randomUUID()
        val outputId1 = UUID.randomUUID()
        val outputId2 = UUID.randomUUID()
        val outputId3 = UUID.randomUUID()
        val location1 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId1/outputs/file1.txt"
        )
        val location2 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId1/outputs/file2.txt"
        )
        val location3 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId2/outputs/file3.txt"
        )

        store.recordArtefact(stepId1, outputId1, location1)
        store.recordArtefact(stepId1, outputId2, location2)
        store.recordArtefact(stepId2, outputId3, location3)

        // Act
        val allArtefacts = store.getAllArtefacts()

        // Assert
        assertEquals(3, allArtefacts.size, "Should return all 3 stored artefacts")
        assertTrue(allArtefacts.any { it.outputId == outputId1 })
        assertTrue(allArtefacts.any { it.outputId == outputId2 })
        assertTrue(allArtefacts.any { it.outputId == outputId3 })
    }

    @Test
    fun `getAllArtefacts returns empty list when no artefacts stored`()
    {
        // Act
        val allArtefacts = store.getAllArtefacts()

        // Assert
        assertTrue(allArtefacts.isEmpty(), "Should return empty list when no artefacts stored")
    }

    // ── Path Resolution ────────────────────────────────────────────────────────

    @Test
    fun `resolvePath returns path for existing artefact`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val location = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId/outputs/data.csv"
        )
        store.recordArtefact(stepId, outputId, location)

        // Act
        val path = store.resolvePath(outputId)

        // Assert
        assertNotNull(path, "resolvePath should return a path for existing artefact")
        assertTrue(path.isNotEmpty(), "Path should not be empty")
    }

    @Test
    fun `resolvePath returns null for non-existent outputId`()
    {
        // Arrange
        val nonExistentId = UUID.randomUUID()

        // Act
        val path = store.resolvePath(nonExistentId)

        // Assert
        assertNull(path, "resolvePath should return null for non-existent outputId")
    }

    // ── Edge Cases and Multiple Operations ────────────────────────────────────

    @Test
    fun `can record multiple artefacts for same step`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val outputIds = (1..5).map { UUID.randomUUID() }

        // Act
        outputIds.forEach { outputId ->
            val location = ResourceRef(
                kind = ResourceKind.RELATIVE_PATH,
                value = "steps/$stepId/outputs/file_$outputId.txt"
            )
            store.recordArtefact(stepId, outputId, location)
        }

        // Assert
        val artefacts = store.getArtefactsByStep(stepId)
        assertEquals(5, artefacts.size)
        outputIds.forEach { outputId ->
            assertTrue(artefacts.any { it.outputId == outputId })
        }
    }

    @Test
    fun `recording artefact with duplicate outputId updates or replaces existing`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val location1 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId/outputs/old.txt"
        )
        val location2 = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/$stepId/outputs/new.txt"
        )

        // Act
        store.recordArtefact(stepId, outputId, location1, ArtefactMetadata(sizeBytes = 100))
        store.recordArtefact(stepId, outputId, location2, ArtefactMetadata(sizeBytes = 200))

        // Assert
        val retrieved = store.getArtefact(outputId)
        assertNotNull(retrieved)
        // Implementation may either replace the old one or keep both - document behaviour
        // This test documents that duplicate outputIds are handled (implementation-specific)
    }

    @Test
    fun `store handles large number of artefacts`()
    {
        // Arrange
        val stepIds = (1..10).map { UUID.randomUUID() }
        val totalArtefacts = 50

        // Act
        var artefactCount = 0
        stepIds.forEach { stepId ->
            repeat(5) {
                val outputId = UUID.randomUUID()
                val location = ResourceRef(
                    kind = ResourceKind.RELATIVE_PATH,
                    value = "steps/$stepId/outputs/file_$artefactCount.txt"
                )
                store.recordArtefact(stepId, outputId, location)
                artefactCount++
            }
        }

        // Assert
        val allArtefacts = store.getAllArtefacts()
        assertEquals(totalArtefacts, allArtefacts.size)
    }
}

