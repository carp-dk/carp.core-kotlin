package dk.cachet.carp.analytics.domain.process


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
/**
 * Represents a command template with validation and rendering capabilities.
 * @param template Non-empty template string for commands.
 */

@Serializable
@SerialName("CommandTemplate")
data class CommandTemplate( val template: String )
{
    init
    {
        require(template.isNotBlank()) { "Command template cannot be blank" }
    }

    /**
     * Renders the command template using provided arguments.
     * @param arguments List of arguments to format the template.
     * @return A formatted command string.
     */
    fun render( arguments: List<String> ): String
    {
        return template.replace(Regex("\\{(\\d+)}"))
        { matchResult ->
            val index = matchResult.groupValues[1].toInt()
            arguments.getOrNull(index)
                ?: throw IllegalArgumentException("Missing argument for placeholder $index in template: $template")
        }
    }
}
