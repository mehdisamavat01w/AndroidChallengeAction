package com.mahdisamavat.core.common.result


sealed class Error {
    
    abstract val message: String
    abstract val throwable: Throwable?

    data class Database(
        override val message: String,
        override val throwable: Throwable? = null
    ) : Error()
    

    data class Permission(
        override val message: String,
        val permission: String? = null,
        override val throwable: Throwable? = null
    ) : Error()
    

    data class Security(
        override val message: String,
        override val throwable: Throwable? = null
    ) : Error()
    

    data class IPC(
        override val message: String,
        override val throwable: Throwable? = null
    ) : Error()
    

    data class Service(
        override val message: String,
        override val throwable: Throwable? = null
    ) : Error()
    

    data class Location(
        override val message: String,
        override val throwable: Throwable? = null
    ) : Error()
    

    data class Validation(
        override val message: String,
        val field: String? = null,
        override val throwable: Throwable? = null
    ) : Error()
    

    data class Unknown(
        override val message: String,
        override val throwable: Throwable? = null
    ) : Error()
}
