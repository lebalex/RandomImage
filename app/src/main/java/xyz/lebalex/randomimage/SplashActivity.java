package xyz.lebalex.randomimage;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.ads.internal.gmsg.HttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by ivc_lebedevav on 27.01.2017.
 */
public class SplashActivity extends Activity {

    private static int SPLASH_TIME_OUT = 2000;
    private String url =     "http://lebalex.xyz/randomimage.json";
    private String url_apk = "http://lebalex.xyz/randomimage.apk";
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=this;


        getJsonFromUrl(url);

        if (!ConstClass.isUseApi()) {
            getJsonFromUrl(ConstClass.getLockscreen_url(), false);
            getJsonFromUrl(ConstClass.getErotic_url(), true);
        }



        /*new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_TIME_OUT);*/

    }

    private void MessageBox(String error) {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(error)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();*/
    }

    private void getJsonFromUrl(String urls) {

        String resultJson = null;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL jsonUrl = new URL(urls);
            URLConnection dc = jsonUrl.openConnection();
            dc.setConnectTimeout(10 * 1000);
            dc.setReadTimeout(10 * 1000);
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(
                    dc.getInputStream()));
            resultJson = inputStream.readLine();
            try {
                JSONObject jsonObject = new JSONObject(resultJson);
                ConstClass.setUseApi(jsonObject.getBoolean("useapi"));
                ConstClass.setApi_erotic_url(jsonObject.getString("api_erotic_url"));
                ConstClass.setApi_lockscreen_url(jsonObject.getString("api_lockscreen_url"));
                ConstClass.setErotic_url(jsonObject.getString("erotic_url"));
                ConstClass.setLockscreen_url(jsonObject.getString("lockscreen_url"));

                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    if(!pInfo.versionName.equalsIgnoreCase(jsonObject.getString("ver")))
                    {
                        //need update
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(R.string.info)
                                .setMessage(R.string.need_update)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( url_apk ) );
                                        startActivity( browse );
                                        finish();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                        Intent intent = new Intent(ctx, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }else
                    {
                        Intent intent = new Intent(ctx, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

            } catch (JSONException e) {
                MessageBox(e.getMessage());
            }
        } catch (UnsupportedEncodingException e1) {
            MessageBox(e1.getMessage());
        } catch (IllegalStateException e3) {
            MessageBox(e3.getMessage());
        } catch (IOException e4) {
            MessageBox(e4.getMessage());
        } catch (Exception e4) {
            MessageBox(e4.getMessage());
        }
    }

    private void getJsonFromUrl(String urls, boolean erotic) {

        String resultJson = null;

        try {
            URL jsonUrl = new URL(urls);
            URLConnection dc = jsonUrl.openConnection();
            dc.setConnectTimeout(10 * 1000);
            dc.setReadTimeout(10 * 1000);
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(
                    dc.getInputStream()));
            resultJson = inputStream.readLine();


        } catch (UnsupportedEncodingException e1) {
            MessageBox(e1.getMessage());
        } catch (IllegalStateException e3) {
            MessageBox(e3.getMessage());
        } catch (IOException e4) {
            MessageBox(e4.getMessage());
        } catch (Exception e4) {
            MessageBox(e4.getMessage());
        }
        Map<String, Integer> hashMap = new HashMap<String, Integer>();
        Map<String, Integer> hashMapLocal = new HashMap<String, Integer>();
        if (!erotic) hashMapLocal = ConstClass.getScreenMap(this);
        if (erotic) hashMapLocal = ConstClass.getEroticMap(this);
        if (resultJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(resultJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONArray oneObject = new JSONArray(jsonArray.getString(i));
                        String url = oneObject.getString(0);
                        int count = oneObject.getInt(1);
                        hashMap.put(url, count);
                    } catch (JSONException e) {
                    }
                }


                for (Map.Entry entry : hashMap.entrySet()) {
                    if (hashMapLocal.containsKey(entry.getKey()))
                        entry.setValue(max(hashMapLocal.get(entry.getKey()), (int) entry.getValue()));
                }


                if (!erotic)
                    ConstClass.setScreenMap(hashMap, this);
                if (erotic)
                    ConstClass.setEroticMap(hashMap, this);

                //Log.d("splash", urls);

            } catch (JSONException e) {
                MessageBox(e.getMessage());
            }
        }
    }

    private int max(int a, int b) {
        if (a > b) return a;
        else return b;
    }
}