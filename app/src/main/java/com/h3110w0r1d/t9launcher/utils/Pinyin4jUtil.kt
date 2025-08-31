package com.h3110w0r1d.t9launcher.utils

import com.h3110w0r1d.t9launcher.vo.AppInfo
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import java.util.Locale

object Pinyin4jUtil {
    var defaultFormat: HanyuPinyinOutputFormat = HanyuPinyinOutputFormat()

    fun letter2Number(letters: String): String {
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

    fun getPinYin(str: String): MutableList<MutableList<String>> {
        val result: MutableList<MutableList<String>> = ArrayList()
        var latterAndNumber = StringBuilder()
        for (i in 0..<str.length) {
            val c = str.get(i)
            if ('a' <= c && c <= 'z') {
                latterAndNumber.append(c.toString().lowercase(Locale.getDefault()))
                continue
            }
            if (latterAndNumber.isNotEmpty()) {
                val newList: MutableList<String> = ArrayList()
                newList.add(letter2Number(latterAndNumber.toString()))
                newList.add(letter2Number(latterAndNumber.substring(0, 1)))
                result.add(newList)
                latterAndNumber = StringBuilder()
            }
            if ('A' <= c && c <= 'Z') {
                latterAndNumber.append(c.toString().lowercase(Locale.getDefault()))
                continue
            }

            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c) // 获取拼音列表
            if (pinyinArray != null) {
                val pinyinList = pinyinArray.map { s: String -> s.substring(0, s.length - 1) } // 去掉声调
                val newList: MutableList<String> = ArrayList()
                val size = pinyinList.size

                val pinyinSet: MutableSet<String> = HashSet()
                for (j in 0..<size) {
                    if (pinyinSet.add(pinyinList[j].substring(0, 1))) {
                        newList.add(letter2Number(pinyinList[j].substring(0, 1)))
                    }
                    if (pinyinSet.add(pinyinList[j])) {
                        newList.add(letter2Number(pinyinList[j]))
                    }
                }
                result.add(newList)
                continue
            }
            if (" " == c.toString()) {
                continue
            }
            if ('0' <= c && c <= '9') {
                result.add(mutableListOf(c.toString()))
                continue
            }
            result.add(mutableListOf("0"))
        }

        if (latterAndNumber.isNotEmpty()) {
            val newList: MutableList<String> = ArrayList()
            newList.add(letter2Number(latterAndNumber.toString()))
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
    fun Search(app: AppInfo, searchText: String): Float {
        val data = app.searchData
        for (i in data.indices) {
            val position = search(data, searchText, i, 0)
            if (position > 0) {
                return (position - i).toFloat() / data.size.toFloat()
            }
        }
        return 0f
    }

    // 递归搜索 返回0匹配失败, 否则返回匹配到了第几个字
    private fun search(
        data: MutableList<MutableList<String>>,
        searchText: String,
        i: Int,
        k: Int
    ): Int {
        if (searchText.length <= k) { // 全部匹配完成
            return i // 返回匹配到了第几个字
        }
        if (i >= data.size) { // 没有下一个字了
            return 0
        }

        for (j in data[i].indices) {
            if (match(searchText, data[i][j], k)) { // 当前字匹配, 匹配下一个字
                val position = search(data, searchText, i + 1, k + data[i][j].length)
                if (position != 0) {
                    return position
                }
            }
        }
        return 0
    }

    private fun match(searchText: String, chr: String, k: Int): Boolean {
        return searchText.startsWith(chr, k) || chr.startsWith(searchText.substring(k))
    }
}
