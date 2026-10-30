package com.example.bdui.data.local

import android.content.Context
import android.util.AtomicFile
import com.example.bdui.domain.local.BduiStorage
import com.example.bdui.domain.local.BduiStorageException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class BduiStorageImpl(
    context: Context
) : BduiStorage {

    private val appContext = context.applicationContext

    private val storageDirectory =
        File(appContext.filesDir, "bdui").apply {
            if (!exists() && !mkdirs()) {
                throw IllegalStateException(
                    "Could not create BDUI storage directory"
                )
            }
        }

    private val atomicFile = AtomicFile(
        storageDirectory.resolve("data.json")
    )

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    private val actorScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val commands = Channel<StorageCommand<*>>(
        Channel.BUFFERED
    )

    init {
        actorScope.launch {
            for (command in commands) {
                command.execute()
            }
        }
    }

    override suspend fun create(
        key: String,
        value: JsonElement
    ) {
        execute {
            validateKey(key)

            val data = readData()

            if (data.containsKey(key)) {
                throw BduiStorageException.KeyAlreadyExists(key)
            }

            writeData(data + (key to value))
        }
    }

    override suspend fun load(
        key: String
    ): JsonElement = execute {
        validateKey(key)

        val data = readData()

        data[key] ?: throw BduiStorageException.KeyNotFound(key)
    }

    override suspend fun update(
        key: String,
        value: JsonElement
    ) {
        execute {
            validateKey(key)

            val data = readData()

            if (!data.containsKey(key)) {
                throw BduiStorageException.KeyNotFound(key)
            }

            writeData(data + (key to value))
        }
    }

    override suspend fun delete(
        key: String
    ) {
        execute {
            validateKey(key)

            val data = readData()

            if (!data.containsKey(key)) {
                throw BduiStorageException.KeyNotFound(key)
            }

            writeData(data - key)
        }
    }

    override suspend fun clear() {
        execute {
            writeData(emptyMap())
        }
    }

    private suspend fun <T> execute(
        action: () -> T
    ): T {
        val result = CompletableDeferred<T>()

        commands.send(
            StorageCommand(
                action = action,
                result = result
            )
        )

        return result.await()
    }

    /*
     * Читает весь JSON-документ.
     *
     * Если файл ещё не создан, возвращает пустой объект.
     */
    private fun readData(): Map<String, JsonElement> {
        val source = try {
            atomicFile.openRead().bufferedReader().use {
                it.readText()
            }
        } catch (_: FileNotFoundException) {
            return emptyMap()
        }

        return try {
            val root = json.parseToJsonElement(source)

            val objectData = root as? JsonObject
                ?: throw IllegalStateException(
                    "Storage root must be a JSON object"
                )

            objectData.toMap()
        } catch (error: Exception) {
            throw BduiStorageException.InvalidData(error)
        }
    }

    /*
     * Записывает обновлённый JSON-документ.
     *
     * AtomicFile защищает файл от повреждения
     * при прерывании записи.
     */
    private fun writeData(
        data: Map<String, JsonElement>
    ) {
        val content = json.encodeToString(
            JsonObject.serializer(),
            JsonObject(data)
        )

        var output: FileOutputStream? = null

        try {
            output = atomicFile.startWrite()

            val writer = OutputStreamWriter(
                output,
                StandardCharsets.UTF_8
            )

            writer.write(content)
            writer.flush()

            atomicFile.finishWrite(output)
            output = null

        } catch (error: Exception) {
            output?.let {
                atomicFile.failWrite(it)
            }

            throw error
        }
    }

    private fun validateKey(key: String) {
        if (key.isBlank()) {
            throw BduiStorageException.InvalidKey()
        }
    }

    private class StorageCommand<T>(
        private val action: () -> T,
        private val result: CompletableDeferred<T>
    ) {

        fun execute() {
            try {
                result.complete(action())
            } catch (error: Exception) {
                result.completeExceptionally(error)
            }
        }
    }
}