package com.h3110w0r1d.t9launcher.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class IconMetadata(
    val componentId: String,
    val offset: Long, // 在文件中的起始偏移量
    val length: Int, // 数据长度
)

@Singleton
class IconManager
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
    ) {
        private val packedIconsFile: File
            get() = File(context.filesDir, "packed_icons.bin")

        private val icons = ConcurrentHashMap<String, Bitmap>()

        private var changed = false

        fun loadAllIcons(): Boolean {
            if (!packedIconsFile.exists()) {
                return false
            }
            if (!icons.isEmpty()) {
                return false
            }
            icons.clear()
            try {
                RandomAccessFile(packedIconsFile, "r").use { randomAccessFile ->
                    val indexSizeBuffer = ByteBuffer.allocate(4)
                    randomAccessFile.read(indexSizeBuffer.array())
                    val indexSize = indexSizeBuffer.getInt(0)

                    val indexOffset = packedIconsFile.length() - indexSize
                    randomAccessFile.seek(indexOffset)

                    val metadataBytes = ByteArray(indexSize)
                    randomAccessFile.read(metadataBytes)
                    val metadataString = String(metadataBytes, Charsets.UTF_8)

                    val metadataList =
                        metadataString.split(",").mapNotNull { entry ->
                            val parts = entry.split(":")
                            if (parts.size == 3) {
                                IconMetadata(
                                    componentId = parts[0],
                                    offset = parts[1].toLong(),
                                    length = parts[2].toInt(),
                                )
                            } else {
                                null
                            }
                        }

                    for (metadata in metadataList) {
                        val bitmapBytes = ByteArray(metadata.length)
                        randomAccessFile.seek(metadata.offset)
                        randomAccessFile.read(bitmapBytes)

                        val bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, metadata.length)
                        if (bitmap != null) {
                            icons[metadata.componentId] = bitmap
                        }
                    }
                }
                return true
            } catch (e: Exception) {
                return false
            }
        }

        fun addIcon(
            componentId: String,
            bitmap: Bitmap,
        ) {
            icons[componentId] = bitmap
            changed = true
        }

        fun saveIconsToFile(): Boolean {
            if (!changed) {
                return false
            }
            try {
                RandomAccessFile(packedIconsFile, "rw").use { randomAccessFile ->
                    var currentOffset = 0L

                    val metadataList = mutableListOf<IconMetadata>()
                    val indexPlaceholder = ByteBuffer.allocate(4)
                    randomAccessFile.write(indexPlaceholder.array())
                    currentOffset += indexPlaceholder.array().size.toLong()

                    for ((componentId, bitmap) in icons) {
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        val bitmapBytes = byteArrayOutputStream.toByteArray()

                        val metadata = IconMetadata(componentId, currentOffset, bitmapBytes.size)
                        metadataList.add(metadata)

                        randomAccessFile.seek(currentOffset)
                        randomAccessFile.write(bitmapBytes)
                        currentOffset += bitmapBytes.size
                    }

                    val metadataJson =
                        metadataList
                            .joinToString(",") {
                                "${it.componentId}:${it.offset}:${it.length}"
                            }.toByteArray(Charsets.UTF_8)

                    randomAccessFile.seek(currentOffset)
                    randomAccessFile.write(metadataJson)

                    val indexSize = metadataJson.size
                    randomAccessFile.seek(0)
                    val indexSizeByteBuffer = ByteBuffer.allocate(4).putInt(indexSize)
                    randomAccessFile.write(indexSizeByteBuffer.array())
                }
                return true
            } catch (_: Exception) {
                return false
            }
        }

        fun getIcon(componentId: String): Bitmap? = icons[componentId]

        fun deleteIcon(
            packageName: String,
            className: String,
        ) {
            icons.remove("$packageName/$className")
            changed = true
        }
    }
