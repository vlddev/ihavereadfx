package com.vlad.ihaveread.util;

public class Util {

    public static String trimOrNull(String str) {
        return str != null ? str.trim() : null;
    }

    public static String trimOrEmpty(String str) {
        return str != null ? str.trim() : "";
    }
}
