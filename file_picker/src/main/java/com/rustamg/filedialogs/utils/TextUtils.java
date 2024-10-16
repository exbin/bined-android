package com.rustamg.filedialogs.utils;
import java.util.ArrayList;
import java.util.List;


/**
 * Created at 17/02/15 18:10
 *
 * @author rustamg
 */
public class TextUtils {

    public static boolean isEmpty(String string) {

        return string == null || string.trim().length() == 0;
    }


    public static String getIntegersCommaSeparated(List<Integer> array) {

        StringBuilder sb = new StringBuilder();

        sb.append(array.get(0));

        for (int i = 1; i < array.size(); i++) {
            sb.append(",");
            sb.append(array.get(i));
        }

        return sb.toString();
    }

    public static String getLongsCommaSeparated(List<Long> array) {

        StringBuilder sb = new StringBuilder();

        sb.append(array.get(0));

        for (int i = 1; i < array.size(); i++) {
            sb.append(",");
            sb.append(array.get(i));
        }

        return sb.toString();
    }

    public static List<Long> getLongs(String commaSeparatedString) {

        List<Long> longs = new ArrayList<>();

        String[] strings = commaSeparatedString.split(",");

        for (String s : strings) {
            longs.add(Long.parseLong(s));
        }

        return longs;
    }
}
