package com.ishehui.teamviewerid;

public class StringUtil {
    public static final String TAG = StringUtil.class.getSimpleName();

    /**
     * 判断字符串是否为null或是长度为0 或是字符串null
     **/
    public static boolean IsEmptyOrNullString(String s) {
        return (s == null) || (s.trim().length() == 0) || s.equalsIgnoreCase("null");
    }

}
