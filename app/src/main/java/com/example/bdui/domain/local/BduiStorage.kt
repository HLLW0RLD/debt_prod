package com.example.bdui.domain.local

import kotlinx.serialization.json.JsonElement

interface BduiStorage {

    /**
     * Создаёт новую запись.
     * Если ключ уже существует, выбрасывает ошибку.
     */
    suspend fun create(
        key: String,
        value: JsonElement
    )

    /**
     * Возвращает значение по ключу.
     * Если ключ отсутствует, выбрасывает ошибку.
     */
    suspend fun load(
        key: String
    ): JsonElement

    /**
     * Обновляет существующую запись.
     * Если ключ отсутствует, выбрасывает ошибку.
     */
    suspend fun update(
        key: String,
        value: JsonElement
    )

    /**
     * Удаляет запись по ключу.
     */
    suspend fun delete(
        key: String
    )

    /**
     * Очищает всё хранилище.
     */
    suspend fun clear()
}