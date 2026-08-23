package com.flingeria.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Stable seam for the native llama.cpp/GGUF implementation. UI never owns native resources. */
interface LocalLLMEngine { val isReady: Boolean; suspend fun load(modelPath: String): Result<Unit>; fun generate(prompt: String, maxTokens: Int = 512): Flow<String>; fun stop(); fun release() }

class UnavailableLocalLLMEngine : LocalLLMEngine {
    override var isReady = false
    override suspend fun load(modelPath: String) = Result.failure<Unit>(IllegalStateException("Runtime nativo pendiente de habilitar"))
    override fun generate(prompt: String, maxTokens: Int) = flow { emit("El motor local aún no está instalado. Descarga el modelo y habilita llama.cpp en la siguiente iteración.") }
    override fun stop() = Unit; override fun release() = Unit
}
