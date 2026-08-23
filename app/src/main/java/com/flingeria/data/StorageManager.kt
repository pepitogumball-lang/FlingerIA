package com.flingeria.data

import android.content.Context
import java.io.File

class StorageManager(private val context: Context) {
    // App-specific external storage is Android-scoped and needs no broad permission.
    private val root: File get() = File(context.getExternalFilesDir(null), "XD")
    fun getRootDirectory() = root
    fun getModelsDirectory() = File(root, "models").also { it.mkdirs() }
    fun getScriptsDirectory() = File(root, "scripts").also { it.mkdirs() }
    fun getConversationsDirectory() = File(root, "conversations").also { it.mkdirs() }
    fun getCacheDirectory() = File(root, "cache").also { it.mkdirs() }
    fun getSettingsDirectory() = File(root, "settings").also { it.mkdirs() }
    fun getLogsDirectory() = File(root, "logs").also { it.mkdirs() }
    fun getTempDirectory() = File(root, "temp").also { it.mkdirs() }
    fun initialize() { getModelsDirectory(); getScriptsDirectory(); getConversationsDirectory(); getCacheDirectory(); getSettingsDirectory(); getLogsDirectory(); getTempDirectory() }
    fun deleteAll() { root.deleteRecursively() }
}
