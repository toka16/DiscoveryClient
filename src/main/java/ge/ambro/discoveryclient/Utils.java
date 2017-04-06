/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author tabramishvili
 */
public class Utils {

    public static String concat(String str1, String str2) {
        String full = str1 + '/' + str2;
        String after = full.substring(full.indexOf("://") + 3);
        return full.substring(0, full.indexOf("://")) + "://" + after.replaceAll("//+", "/");
    }

    public static Object strToJSON(String str) {
        if (str == null) {
            return null;
        }
        if (str.startsWith("{")) {
            return new JSONObject(str);
        }
        if (str.startsWith("[")) {
            return new JSONArray(str);
        }
        return str;
    }

    public static String readAll(InputStream in) {
        return new BufferedReader(new InputStreamReader(in))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    public static JSONArray sort(JSONArray arr, String key) {
        List<Object> list = arr.toList();
        list.sort((o1, o2) -> {
            long l1 = Long.valueOf(String.valueOf(((Map<String, Object>) o1).getOrDefault(key, "0")));
            long l2 = Long.valueOf(String.valueOf(((Map<String, Object>) o2).getOrDefault(key, "0")));
            if (l1 > l2) {
                return 1;
            }
            if (l2 > l1) {
                return -1;
            }
            return 0;

        });
        return new JSONArray(list);
    }
}
