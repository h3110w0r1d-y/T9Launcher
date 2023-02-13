package com.h3110w0r1d.t9launcher;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pinyin4jUtil {
    public static HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();

    public static String Letter2Number(String letters) {
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            switch (c) {
                case 'a':
                case 'b':
                case 'c':
                    number.append("2");
                    break;
                case 'd':
                case 'e':
                case 'f':
                    number.append("3");
                    break;
                case 'g':
                case 'h':
                case 'i':
                    number.append("4");
                    break;
                case 'j':
                case 'k':
                case 'l':
                    number.append("5");
                    break;
                case 'm':
                case 'n':
                case 'o':
                    number.append("6");
                    break;
                case 'p':
                case 'q':
                case 'r':
                case 's':
                    number.append("7");
                    break;
                case 't':
                case 'u':
                case 'v':
                    number.append("8");
                    break;
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    number.append("9");
                    break;
                default:
                    number.append("0");
                    break;
            }
        }
        return number.toString();
    }

    public static List<List<String>> getPinYin(String str) {
        List<List<String>> result = new ArrayList<>();
        StringBuilder LatterAndNumber = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ("abcdefghijklmnopqrstuvwxyz".contains(String.valueOf(c))) {
                LatterAndNumber.append(String.valueOf(c).toLowerCase());
                continue;
            }
            if (LatterAndNumber.length() != 0) {
                List<String> newlist = new ArrayList<>();
                newlist.add(Letter2Number(LatterAndNumber.toString()));
                newlist.add(Letter2Number(LatterAndNumber.substring(0, 1)));
                result.add(newlist);
                LatterAndNumber = new StringBuilder();
            }
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(String.valueOf(c))) {
                LatterAndNumber.append(String.valueOf(c).toLowerCase());
                continue;
            }

            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c); // 获取拼音列表
            if (pinyinArray != null) {
                List<String> pinyinList = Arrays.asList(pinyinArray);
                pinyinList.replaceAll(s -> s.substring(0, s.length() - 1)); // 去掉声调
                List<String> newlist = new ArrayList<>();
                int size = pinyinList.size();

                Set<String> pinyinset = new HashSet<>();
                for (int j = 0; j < size; j++) {
                    if(pinyinset.add(pinyinList.get(j).substring(0, 1))) {
                        newlist.add(Letter2Number(pinyinList.get(j).substring(0, 1)));
                    }
                    if(pinyinset.add(pinyinList.get(j))) {
                        newlist.add(Letter2Number(pinyinList.get(j)));
                    }
                }
                result.add(newlist);
                continue;
            }
            if (" ".equals(String.valueOf(c))) {
                continue;
            }
            if ("1234567890".contains(String.valueOf(c))){
                result.add(Collections.singletonList(String.valueOf(c)));
                continue;
            }
            result.add(Collections.singletonList("0"));
        }

        if (LatterAndNumber.length() != 0) {
            List<String> newlist = new ArrayList<>();
            newlist.add(Letter2Number(LatterAndNumber.toString()));
            result.add(newlist);
        }
        return result;
    }

    public static boolean Search(AppInfo app, String searchText) {
        List<List<String>> data = app.getSeatchData();
        for (int i = 0; i < data.size(); i++) {
            if (search(data, searchText, i, 0)){
                return true;
            }
        }
        return false;
    }

    private static boolean search(List<List<String>> data, String searchText, int i, int k) {
        if (searchText.length() <= k) { // 全部匹配完成
            return true;
        }
        if (i >= data.size()) { // 没有下一个字了
            return false;
        }

        for (int j = 0; j < data.get(i).size(); j++) {
            if (pipei(searchText, data.get(i).get(j), k)) { // 当前字匹配, 匹配下一个字
                if (search(data, searchText, i + 1, k + data.get(i).get(j).length())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean pipei(String searchText, String chr, int k) {
        return searchText.startsWith(chr, k) || chr.startsWith(searchText.substring(k));
    }
}
