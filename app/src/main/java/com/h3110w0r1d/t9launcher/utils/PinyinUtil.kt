package com.h3110w0r1d.t9launcher.utils

import android.content.Context
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.app.AppInfo

class PinyinUtil(
    context: Context,
) {
    private val charPinyinMapping: ArrayList<ArrayList<String>> = ArrayList()

    init {
        val pinyinFile = context.resources.openRawResource(R.raw.pinyin)
        val pinyinBufferReader = pinyinFile.bufferedReader()
        val pinyinList = pinyinBufferReader.readLines()
        pinyinBufferReader.close()
        pinyinFile.close()

        val pinyinIndexFile = context.resources.openRawResource(R.raw.pinyin_index)
        var byte: Int
        var cursor = 0
        var index = 0
        while (pinyinIndexFile.read().also { byte = it } != -1) {
            if (cursor < 20902) {
                charPinyinMapping.add(arrayListOf(pinyinList[byte]))
            } else {
                if ((cursor - 20902) % 3 == 0) {
                    index = byte * 256
                } else if ((cursor - 20902) % 3 == 1) {
                    index += byte
                } else {
                    charPinyinMapping[index].add(pinyinList[byte])
                }
            }
            cursor += 1
        }
    }

    private fun letter2Number(letters: String): String {
        val letters = letters.lowercase()
        val number = StringBuilder()
        for (i in 0..<letters.length) {
            val c = letters[i]
            when (c) {
                'a', 'b', 'c' -> number.append("2")
                'd', 'e', 'f' -> number.append("3")
                'g', 'h', 'i' -> number.append("4")
                'j', 'k', 'l' -> number.append("5")
                'm', 'n', 'o' -> number.append("6")
                'p', 'q', 'r', 's' -> number.append("7")
                't', 'u', 'v' -> number.append("8")
                'w', 'x', 'y', 'z' -> number.append("9")
                else -> number.append("0")
            }
        }
        return number.toString()
    }

    /**
     * 获取字符串的拼音列表
     * @param str 需要转换的字符串
     * @return 拼音列表, 每个字/词对应一个列表, 列表为[该字/词的所有拼音...,原始字/词]
     */
    fun getPinYin(str: String): ArrayList<ArrayList<String>> {
        val result: ArrayList<ArrayList<String>> = ArrayList()
        val word = StringBuilder()
        for (i in 0..<str.length) {
            val c = str[i]
            if ('a' <= c && c <= 'z') {
                word.append(c)
                continue
            }
            if (word.isNotEmpty()) {
                val newList: ArrayList<String> = ArrayList()
                newList.add(letter2Number(word.substring(0, 1)))
                newList.add("e" + letter2Number(word.toString()))

                // 添加原始单词
                newList.add(word.toString())
                result.add(newList)
                word.clear()
            }
            if ('A' <= c && c <= 'Z') {
                word.append(c)
                continue
            }
            if (' ' == c || '-' == c || '_' == c) {
                if (result.isNotEmpty()) {
                    result.last()[result.last().size - 1] += c.toString()
                }
                continue
            }
            if ('0' <= c && c <= '9') {
                result.add(arrayListOf(c.toString(), c.toString()))
                continue
            }
            if (0x4E00 <= c.code && c.code <= 0x9FA5) {
                val pinyinArray = charPinyinMapping[c.code - 0x4E00]
                val newList: ArrayList<String> = ArrayList()
                pinyinArray.forEach { pinyin ->
                    if (newList.indexOf(pinyin.substring(0, 1)) == -1) {
                        newList.add(pinyin.substring(0, 1))
                    }
                    newList.add(pinyin)
                }
                // 添加原始汉字
                newList.add(c.toString())
                result.add(newList)
                continue
            } else if (c.code == 0x3007) {
                result.add(arrayListOf("5", "5464", c.toString())) // 〇 ling
                continue
            }
            result.add(arrayListOf("0", c.toString()))
        }

        if (word.isNotEmpty()) {
            val newList: ArrayList<String> = ArrayList()
            // 最后一个字/词不需要单独加首字母
            // if (latterAndNumber.length > 1) {
            //     newList.add(letter2Number(latterAndNumber.substring(0, 1)))
            // }
            newList.add("e" + letter2Number(word.toString()))
            newList.add(word.toString())
            result.add(newList)
        }
        return result
    }

    /**
     * 搜索
     * @param app
     * @param searchText
     * @return 匹配度 匹配度为0时匹配失败
     */
    fun search(
        app: AppInfo,
        searchText: String,
        englishFuzzyMatch: Boolean = false,
    ): Float {
        val data = app.searchData
        for (i in data.indices) {
            val position = searchPosition(data, searchText, i, 0, englishFuzzyMatch)
            if (position > 0) {
                app.setMatchRange(i, position)
                return (position - i).toFloat() / data.size.toFloat()
            }
        }
        app.setMatchRange(0, 0)
        return 0f
    }

    /**
     * 递归搜索 返回0匹配失败, 否则返回匹配到了第几个字
     * @param data 拼音数据
     * @param searchText 搜索字符串
     * @param i 当前匹配到第几个字
     * @param k 当前匹配到搜索字符串的第几个字符
     * @param englishFuzzyMatch 英文模糊匹配
     */
    private fun searchPosition(
        data: ArrayList<ArrayList<String>>,
        searchText: String,
        i: Int,
        k: Int,
        englishFuzzyMatch: Boolean,
    ): Int {
        if (searchText.length <= k) { // 全部匹配完成
            return i // 返回匹配到了第几个字
        }
        if (i >= data.size) { // 没有下一个字了
            return 0
        }
        // size-2 忽略原始字/词
        for (j in 0..data[i].size - 2) {
            val matchLength = match(searchText, data[i][j], k, englishFuzzyMatch)
            if (matchLength > 0) { // 当前字匹配, 匹配下一个字
                val position = searchPosition(data, searchText, i + 1, k + matchLength, englishFuzzyMatch)
                if (position != 0) {
                    return position
                }
            }
        }
        return 0
    }

    private fun match(
        searchText: String,
        chr: String,
        k: Int,
        englishFuzzyMatch: Boolean,
    ): Int {
        if (searchText.startsWith(chr, k) ||
            chr.startsWith(searchText.substring(k))
        ) {
            return chr.length
        }
        if (chr.startsWith("e")) {
            for (i in 1..<chr.length) {
                if (searchText.startsWith(chr.substring(i), k) ||
                    chr.substring(i).startsWith(searchText.substring(k))
                ) {
                    return chr.length - i
                }
                if (!englishFuzzyMatch) {
                    break
                }
            }
        }
        return 0
    }
}
