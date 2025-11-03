package dk.cachet.carp.analytics.domain.environment

/**
 * Interface representing a generic environment configuration.
 * Note on the dependencies, for now we will keep them as a list of strings, but we may need to implement structured Dependency class
 * Advantages:
    * - Explicit Structure: Each dependency has a clear name and optional version, which improves clarity and validation.
    * - Flexibility: Easier to extend in the future if more attributes are needed (e.g., build options).
    * - Consistency: The generator doesn’t need to parse strings, reducing potential for errors.
* Disadvantages:
    * - Increased Complexity: Requires additional structures (Dependency class) and changes to existing code.
    * - Command Generation Overhead: The generator must map Dependency objects to strings for the final command.
 */
interface Environment
{
    val name: String
    val dependencies: List<String>
}
