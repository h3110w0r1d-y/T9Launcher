package com.h3110w0r1d.t9launcher.vo;

import java.util.Comparator;

public class AppInfoComparator implements Comparator<AppInfo> {
    @Override
    public int compare(AppInfo p1, AppInfo p2) {
        if (p1.getMatchRate() - p2.getMatchRate() == 0) {
            return 0;
        }
        if (p1.getMatchRate() - p2.getMatchRate() > 0) {
            return -1;
        }
        if (p1.getMatchRate() - p2.getMatchRate() < 0) {
            return 1;
        }
        return 0;
    }
}
