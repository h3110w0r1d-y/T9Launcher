package com.h3110w0r1d.t9launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Data {

    public static HashMap<String, String> map = new HashMap<>();
    public static ArrayList<AppInfo> AppArrayList = new ArrayList<AppInfo>();

    public static MainActivity mainActivity;
    public static ArrayList<AppInfo> SearchResult = new ArrayList<AppInfo>();
    public static String SearchText = "";

    public static void Search() {
        Data.SearchResult.clear();
        if (Objects.equals(SearchText, "")){
            return;
        }
        for (AppInfo app : AppArrayList) {
            if (Pinyin4jUtil.Search(app, SearchText)) {
                Data.SearchResult.add(app);
            }
        }
    }
}
