package com.alptrosoft.dexcom_data_collector_android_frontend.domain.error

sealed class GlucoseDataException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(message: String, cause: Throwable? = null) : GlucoseDataException(message, cause)
    class ServerError(val code: Int, message: String) : GlucoseDataException(message)
    class ParsingError(message: String, cause: Throwable? = null) : GlucoseDataException(message, cause)
    class UnknownError(message: String, cause: Throwable? = null) : GlucoseDataException(message, cause)
}