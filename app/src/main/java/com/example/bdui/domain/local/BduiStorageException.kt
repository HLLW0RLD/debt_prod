package com.example.bdui.domain.local

sealed class BduiStorageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class KeyNotFound(
        key: String
    ) : BduiStorageException(
        "Storage key '$key' does not exist"
    )

    class KeyAlreadyExists(
        key: String
    ) : BduiStorageException(
        "Storage key '$key' already exists"
    )

    class InvalidKey :
        BduiStorageException(
            "Storage key must not be blank"
        )

    class InvalidData(
        cause: Throwable
    ) : BduiStorageException(
        "Storage file contains invalid JSON",
        cause
    )

    class OperationFailed(
        operation: String,
        cause: Throwable
    ) : BduiStorageException(
        "Storage operation '$operation' failed",
        cause
    )
}