package dk.cachet.carp.common.application.users

import dk.cachet.carp.common.application.EmailAddress
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.js.JsName


/**
 * Identifies an account.
 */
@Polymorphic
@JsExport
interface AccountIdentity
{
    companion object
    {
        /**
         * Create an [AccountIdentity] identified by an [emailAddress] somebody has access to.
         */
        fun fromEmailAddress( emailAddress: String ) = EmailAccountIdentity( emailAddress )

        /**
         * Create an [AccountIdentity] identified by a unique [username].
         */
        fun fromUsername( username: String ) = UsernameAccountIdentity( username )
    }
}

/**
 * Identifies an account by an [emailAddress] somebody has access to.
 */
@Serializable
@JsExport
data class EmailAccountIdentity( val emailAddress: EmailAddress ) : AccountIdentity
{
    @JsName( "create" )
    constructor( emailAddress: String ) : this( EmailAddress( emailAddress ) )
}

/**
 * Identifies an account by a unique [username].
 */
@Serializable
@JsExport
data class UsernameAccountIdentity( val username: Username ) : AccountIdentity
{
    @JsName( "create" )
    constructor( username: String ) : this( Username( username ) )
}
