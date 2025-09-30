package com.h3110w0r1d.t9launcher.data.icon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.iterator

class IconManager(
    private val context: Context,
) {
    data class IconMetadata(
        val componentId: String,
        val offset: Long, // 在文件中的起始偏移量
        val length: Int, // 数据长度
    )

    private val packedIconsFile: File
        get() = File(context.filesDir, "packed_icons.bin")

    private val tmpPackedIconsFile: File
        get() = File(context.filesDir, "packed_icons.tmp")

    private val icons = ConcurrentHashMap<String, Bitmap>()

    private var isChanged = false

    fun loadAllIcons(): Boolean {
        if (!packedIconsFile.exists()) {
            return false
        }
        if (!icons.isEmpty()) {
            return false
        }
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
        } catch (_: Exception) {
            return false
        }
    }

    fun addIcon(
        componentId: String,
        bitmap: Bitmap,
    ) {
        icons[componentId] = bitmap
        isChanged = true
    }

    fun saveIconsToFile(): Boolean {
        if (!isChanged) {
            return false
        }
        try {
            RandomAccessFile(tmpPackedIconsFile, "rw").use { randomAccessFile ->
                var currentOffset = 0L
                randomAccessFile.setLength(0)

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
                randomAccessFile.fd.sync()
            }
            if (packedIconsFile.exists()) {
                packedIconsFile.delete()
            }
            tmpPackedIconsFile.renameTo(packedIconsFile)
            isChanged = false
            return true
        } catch (_: Exception) {
            if (tmpPackedIconsFile.exists()) {
                tmpPackedIconsFile.delete()
            }
            isChanged = false
            return false
        }
    }

    fun getIcon(componentId: String): Bitmap? = icons[componentId]

    fun deleteIcon(
        packageName: String,
        className: String,
    ) {
        icons.remove("$packageName/$className")
        isChanged = true
    }
}
