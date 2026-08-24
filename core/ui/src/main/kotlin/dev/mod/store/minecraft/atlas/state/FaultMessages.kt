package dev.mod.store.minecraft.core.ui.state

import android.content.Context
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.common.error.AppError

/**
 * Turns an [AppError] into a user-facing, localized string. Lives here (not in a store) so the
 * presentation layer can resolve messages without dragging Android resources into MVIKotlin.
 */
interface FaultMessages {
    fun text(fault: AppError): String
}

class AndroidFaultMessages(private val context: Context) : FaultMessages {

    override fun text(fault: AppError): String = context.getString(resOf(fault))

    private fun resOf(fault: AppError): Int = when (fault) {
        AppError.NetworkError.TIMEOUT -> R.string.fault_timeout
        AppError.NetworkError.NO_CONNECTION -> R.string.fault_no_connection
        AppError.NetworkError.SERVER -> R.string.fault_server
        AppError.NetworkError.SERIALIZATION -> R.string.fault_serialization
        AppError.NetworkError.BAD_REQUEST -> R.string.fault_bad_request
        AppError.NetworkError.NOT_FOUND -> R.string.fault_not_found
        AppError.NetworkError.UNKNOWN -> R.string.fault_unknown
        is AppError.ValidationError -> R.string.fault_validation
    }
}
