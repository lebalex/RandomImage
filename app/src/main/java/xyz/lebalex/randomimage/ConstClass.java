package xyz.lebalex.randomimage;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class ConstClass {
    private static Map<String, Integer> screenMap = new HashMap<String, Integer>();
    private static Map<String, Integer> eroticMap = new HashMap<String, Integer>();
    private static boolean useApi=false;
    private static String api_erotic_url;
    private static String api_lockscreen_url;
    private static String erotic_url;
    private static String lockscreen_url;


    public static Map<String, Integer> getScreenMap() {
        return screenMap;
    }

    public static Map<String, Integer> getEroticMap() {
        return eroticMap;
    }

    public static Map<String, Integer> getScreenMap(Context context) {
        try {
            SharedPreferences sp = getDefaultSharedPreferences(context);
            String screen_json = sp.getString("screen_json", null);
            JSONArray jsonArray = new JSONArray(screen_json);
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONArray oneObject = new JSONArray(jsonArray.getString(i));
                    String url = oneObject.getString(0);
                    int count = oneObject.getInt(1);
                    screenMap.put(url, count);
                } catch (JSONException e) {
                }
            }
        } catch (Exception e) {
        }
        return screenMap;
    }

    public static void setScreenMap(Map<String, Integer> screenMap, Context context) {
        ConstClass.screenMap = screenMap;
        try {
            JSONArray mJSONArray = new JSONArray();
            for (Map.Entry entry : screenMap.entrySet()) {
                JSONArray step = new JSONArray();
                step.put(entry.getKey().toString());
                step.put(entry.getValue().toString());
                mJSONArray.put(step);
            }
            SharedPreferences sp = getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("screen_json", mJSONArray.toString());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Integer> getEroticMap(Context context) {
        try {
            SharedPreferences sp = getDefaultSharedPreferences(context);
            String screen_json = sp.getString("erotic_json", null);
            JSONArray jsonArray = new JSONArray(screen_json);
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONArray oneObject = new JSONArray(jsonArray.getString(i));
                    String url = oneObject.getString(0);
                    int count = oneObject.getInt(1);
                    eroticMap.put(url, count);
                } catch (JSONException e) {
                }
            }
        } catch (Exception e) {
        }
        return eroticMap;
    }

    public static void setEroticMap(Map<String, Integer> eroticMap, Context context) {
        ConstClass.eroticMap = eroticMap;
        JSONArray mJSONArray = new JSONArray();
        for (Map.Entry entry : screenMap.entrySet()) {
            JSONArray step = new JSONArray();
            step.put(entry.getKey().toString());
            step.put(entry.getValue().toString());
            mJSONArray.put(step);
        }
        SharedPreferences sp = getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("erotic_json", mJSONArray.toString());
        editor.commit();

    }

    public static boolean isUseApi() {
        return useApi;
    }

    public static void setUseApi(boolean useApi) {
        ConstClass.useApi = useApi;
    }

    public static String getApi_erotic_url() {
        return api_erotic_url;
    }

    public static void setApi_erotic_url(String api_erotic_url) {
        ConstClass.api_erotic_url = api_erotic_url;
    }

    public static String getApi_lockscreen_url() {
        return api_lockscreen_url;
    }

    public static void setApi_lockscreen_url(String api_lockscreen_url) {
        ConstClass.api_lockscreen_url = api_lockscreen_url;
    }

    public static String getErotic_url() {
        return erotic_url;
    }

    public static void setErotic_url(String erotic_url) {
        ConstClass.erotic_url = erotic_url;
    }

    public static String getLockscreen_url() {
        return lockscreen_url;
    }

    public static void setLockscreen_url(String lockscreen_url) {
        ConstClass.lockscreen_url = lockscreen_url;
    }

}
