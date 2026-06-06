package dk.cachet.carp.common.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CUSTOM_INPUT_TYPE_NAME
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.data.input.elements.AnyInputElement
import dk.cachet.carp.common.application.data.input.elements.InputElement
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Describes expected data to be input by users related to one or multiple participants in a study.
 */
@Serializable
@JsExport
sealed class ParticipantAttribute
{
    /**
     * Uniquely identifies the type of data represented by this participant attribute.
     */
    abstract val inputDataType: InputDataType


    /**
     * A participant attribute for which [inputDataType] determines what data needs to be input.
     */
    @Serializable
    data class DefaultParticipantAttribute( override val inputDataType: InputDataType ) : ParticipantAttribute()


    /**
     * A custom participant attribute, for which an [input] element is specified, and for which an [inputDataType] is generated.
     */
    @Serializable
    data class CustomParticipantAttribute<T : Any>( val input: InputElement<T> ) : ParticipantAttribute()
    {
        override val inputDataType: InputDataType = InputDataType(
            CUSTOM_INPUT_TYPE_NAME,
            UUID.randomUUID().toString().replace( "-", "" )
        )
    }


    /**
     * Get an [InputElement] which describes the data to be input by the user for this attribute.
     * In case this is a default attribute, the input element is retrieved through [registeredInputDataTypes].
     *
     * @throws UnsupportedOperationException when no input element is registered for this attribute.
     */
    @Suppress( "NON_EXPORTABLE_TYPE" )
    fun getInputElement( registeredInputDataTypes: InputDataTypeList ): AnyInputElement =
        when ( this )
        {
            is DefaultParticipantAttribute -> registeredInputDataTypes.inputElements[ inputDataType ]
                ?: throw UnsupportedOperationException( "No input element for '$inputDataType' registered." )
            is CustomParticipantAttribute<*> -> input
        }

    /**
     * Determines whether [input] is valid and can be converted to a matching [Data] object associated to this attribute
     * as registered in [registeredInputDataTypes] or [CustomInput] in case this is a [CustomParticipantAttribute].
     *
     * @throws UnsupportedOperationException when no input element is registered for this attribute.
     */
    fun <TInput> isValidInput( registeredInputDataTypes: InputDataTypeList, input: TInput ): Boolean
    {
        @Suppress( "UNCHECKED_CAST" )
        val inputElement = getInputElement( registeredInputDataTypes ) as InputElement<Any>

        // TODO: For now, consider null always a valid option.
        if ( input == null ) return true

        // TODO: `getDataClass` is a trivial implementation in extending classes, but could this be enforced by using the type system instead?
        //       On the JVM runtime, `isValidConfiguration` throws a `ClassCastException` when the wrong type were to be passed, but not on JS runtime.
        val isExpectedDataType = inputElement.getDataClass().isInstance( input )
        return isExpectedDataType && inputElement.isValid( input )
    }

    /**
     * Convert [input] to the matching [Data] object associated to this attribute as registered in [registeredInputDataTypes],
     * or [CustomInput] in case this is a [CustomParticipantAttribute].
     *
     * @throws IllegalArgumentException when the [input] does not match the constraints determined by the [InputElement] associated to this attribute.
     * @throws UnsupportedOperationException when no input element or data converter is registered for the input data type of this attribute.
     */
    fun <TInput> inputToData( registeredInputDataTypes: InputDataTypeList, input: TInput ): Data?
    {
        require( isValidInput( registeredInputDataTypes, input ) )
            { "Input value does not match constraints for the specified input data type." }

        // TODO: Add 'isRequired' to `InputElement` and validate whether 'null' input (not set) is a valid option.
        if ( input == null ) return null

        // Custom input which is simply wrapped.
        if ( this is CustomParticipantAttribute<*> ) return CustomInput( input )

        // Convert to concrete Data object.
        val converter = registeredInputDataTypes.inputToDataConverters[ inputDataType ]
            ?: throw UnsupportedOperationException( "No data converter for '$inputDataType' registered." )
        return converter( input )
    }

    /**
     * Determines whether [data] is valid input for this attribute.
     *
     * @throws UnsupportedOperationException when no input element is registered for this attribute.
     */
    fun <TData : Data?> isValidData( registeredInputDataTypes: InputDataTypeList, data: TData ): Boolean
    {
        val inputElement = getInputElement( registeredInputDataTypes )

        // TODO: For now, consider null always a valid option.
        if ( data == null ) return true

        // Early out in case data is of the wrong type.
        val isCorrectDataType = when ( this )
        {
            is CustomParticipantAttribute<*> -> data is CustomInput && isValidCustomData( inputElement, data )
            is DefaultParticipantAttribute ->
            {
                val dataClass = checkNotNull( registeredInputDataTypes.dataClasses[ inputDataType ] )
                dataClass.isInstance(data)
            }
        }
        if ( !isCorrectDataType ) return false

        val input: Any? = dataToInput( registeredInputDataTypes, data )
        return isValidInput( registeredInputDataTypes, input )
    }

    /**
     * Convert [data] to the corresponding input representation.
     * The returned input is not necessarily valid as it may be constrained further by the registered input element.
     *
     * @throws UnsupportedOperationException when no input element is registered for this attribute.
     */
    fun <TData : Data?> dataToInput( registeredInputDataTypes: InputDataTypeList, data: TData ): Any?
    {
        @Suppress( "UNCHECKED_CAST" )
        val inputElement = getInputElement( registeredInputDataTypes ) as InputElement<Any>

        // TODO: For now, consider null always a valid option.
        if ( data == null ) return null

        // Custom input should be wrapped by `CustomInput` and contain an object of the expected input data type.
        if ( this is CustomParticipantAttribute<*> )
        {
            require( data is CustomInput && isValidCustomData( inputElement, data ) )
                { "Data is not of expected type for this attribute." }
            return data.input
        }

        // Convert to input data.
        val converter = registeredInputDataTypes.dataToInputConverters[ inputDataType ]
            ?: throw UnsupportedOperationException( "No data converter for '$inputDataType' registered." )
        val dataClass = checkNotNull( registeredInputDataTypes.dataClasses[ inputDataType ] )
        require( dataClass.isInstance( data ) ) { "Data is not of expected type for this attribute." }
        return converter( data )
    }

    private fun isValidCustomData( inputElement: InputElement<*>, data: CustomInput ) =
        inputElement.getDataClass().isInstance( data.input )
}
