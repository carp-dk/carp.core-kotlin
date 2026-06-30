package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.application.tasks.WebTask.UrlVariable
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Redirects to a web page which contains the task which needs to be performed.
 * The passive [measures] are started when the website is opened and stopped when it is closed.
 */
@Serializable
@JsExport
data class WebTask(
    override val name: String,
    /**
     * The URL of the web page which contains the task to be performed.
     * The URL may contain [UrlVariable] patterns which will be replaced with the corresponding values by the client runtime.
     */
    val url: String,
    override val description: String? = null,
    override val measures: List<Measure> = emptyList(),
) : TaskConfiguration<NoData> // The execution of the task is delegated to a web page, so this task uploads no data.
{
    companion object
    {
        private fun markup( name: String ) = name.uppercase().replace( ' ', '-' ).let { "{{$it}}" }
    }


    enum class UrlVariable( val pattern: String )
    {
        /**
         * Uniquely identifies the participant in the study.
         */
        PARTICIPANT_ID( markup( "participant id" ) ),

        /**
         * Uniquely identifies the deployment (group of participants and devices) of the study.
         */
        DEPLOYMENT_ID( markup( "deployment id" ) ),

        /**
         * Identifies the condition, defined by the study protocol, which caused the [WebTask] to be triggered.
         */
        TRIGGER_ID( markup( "trigger id" ) )
    }


    /**
     * Replace the variables in [url] with the specified runtime values, if the variables are present.
     */
    fun constructUrl( participantId: UUID, studyDeploymentId: UUID, triggerId: Int ): String = url
        .replace( UrlVariable.PARTICIPANT_ID.pattern, participantId.toString() )
        .replace( UrlVariable.DEPLOYMENT_ID.pattern, studyDeploymentId.toString() )
        .replace( UrlVariable.TRIGGER_ID.pattern, triggerId.toString() )
}


/**
 * A helper class to configure and construct immutable [WebTask] instances.
 */
@JsExport
class WebTaskBuilder : TaskConfigurationBuilder<WebTask>()
{
    /**
     * The URL of the web page which contains the task to be performed.
     * The URL may contain [UrlVariable] patterns which will be replaced with the corresponding values by the client runtime.
     */
    var url: String = ""

    override fun build( name: String ): WebTask = WebTask( name, url, description, measures )
}
