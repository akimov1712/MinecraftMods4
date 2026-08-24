package dev.mod.store.minecraft.core.common.error

/**
 * The closed set of failures the domain layer understands. Anything thrown by the outside
 * world (Ktor, SQLDelight, IO) is funnelled into one of these before it reaches a use case,
 * so presentation code never sees raw exceptions or stack traces.
 */
sealed interface AppError {

    /** Failures originating from talking to the backend or the network. */
    enum class NetworkError : AppError {
        TIMEOUT,
        NO_CONNECTION,
        SERVER,
        SERIALIZATION,
        BAD_REQUEST,
        NOT_FOUND,
        UNKNOWN,
    }

    /** Failures produced by input validation before any IO happens. */
    sealed interface ValidationError : AppError {
        data object EmptyInput : ValidationError
        data object MalformedEmail : ValidationError
        data object MalformedLink : ValidationError
        data class TooShort(val minLength: Int) : ValidationError
        data class TooLong(val maxLength: Int) : ValidationError
    }
}
