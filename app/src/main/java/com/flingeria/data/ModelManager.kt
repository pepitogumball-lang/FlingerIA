package com.flingeria.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

/** Reanudable model download kept outside UI; model metadata is explicit for future llama.cpp loading. */
class ModelManager(context: Context) {
    companion object {
        const val MODEL_NAME = "Qwen2.5-Coder-1.5B-Instruct Q4_K_M"
        const val MODEL_URL = "https://huggingface.co/Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF/resolve/main/qwen2.5-coder-1.5b-instruct-q4_k_m.gguf"
        const val EXPECTED_BYTES = 1_120_000_000L
    }
    private val storage = StorageManager(context)
    private val client = OkHttpClient()
    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state = _state.asStateFlow()
    val modelFile get() = File(storage.getModelsDirectory(), "qwen2.5-coder-1.5b-instruct-q4_k_m.gguf")

    suspend fun download() = withContext(Dispatchers.IO) {
        if (modelFile.exists() && modelFile.length() > 1_000_000) { _state.value = DownloadState.Ready(modelFile.length()); return@withContext }
        val part = File(storage.getTempDirectory(), modelFile.name + ".part")
        var offset = if (part.exists()) part.length() else 0L
        val request = Request.Builder().url(MODEL_URL).apply { if (offset > 0) header("Range", "bytes=$offset-") }.build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) error("No se pudo descargar el modelo (${response.code})")
                if (offset > 0 && response.code == 200) { part.delete(); offset = 0 }
                val total = (response.body!!.contentLength().takeIf { it > 0 } ?: EXPECTED_BYTES) + offset
                response.body!!.byteStream().use { input -> RandomAccessFile(part, "rw").use { file -> file.seek(offset); val buffer=ByteArray(64*1024); var read:Int; var done=offset; while(input.read(buffer).also{read=it}!=-1){file.write(buffer,0,read); done+=read; _state.value=DownloadState.Downloading(done,total)} } }
            }
            part.renameTo(modelFile); _state.value = DownloadState.Ready(modelFile.length())
        } catch (e: Exception) { _state.value = DownloadState.Error(e.message ?: "Error de descarga") }
    }
    fun sha256(): String? {
        if (!modelFile.exists()) return null
        val digest = MessageDigest.getInstance("SHA-256")
        modelFile.inputStream().use { input -> val buffer = ByteArray(64 * 1024); var n: Int; while (input.read(buffer).also { n = it } != -1) digest.update(buffer, 0, n) }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
sealed interface DownloadState { data object Idle: DownloadState; data class Downloading(val bytes:Long,val total:Long):DownloadState; data class Ready(val bytes:Long):DownloadState; data class Error(val message:String):DownloadState }
