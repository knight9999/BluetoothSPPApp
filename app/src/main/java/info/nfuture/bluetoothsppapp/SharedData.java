package info.nfuture.bluetoothsppapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by knaito on 2015/08/26.
 */
public class SharedData {
    public static final String SHARED_NAME = "BLUETOOTH_SPP_APP";

    public static void saveString(Context context,String key,String value) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public static String loadString(Context context,String key) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_NAME,Context.MODE_PRIVATE);
        String value = prefs.getString(key, null);
        return value;
    }

    public static void remove(Context context,String key) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();
    }
}
