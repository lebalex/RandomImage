package xyz.lebalex.randomimage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class MainActivity extends AppCompatActivity {
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Bitmap bitmap = null;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private InterstitialAd mInterstitialAd;
    private int countImage = 0;
    private int countImageMax = 10;
    private int tapCountForAds = 0;
    private Menu mMenu;
    private SharedPreferences sp;
    private Timer mTimer;
    private Map<String, Integer> hashMapUrls = new HashMap<String, Integer>();
    Random rdm = new Random();
    private String globalUrlsApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mImageView = (ImageView) findViewById(R.id.imgView);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        //mProgressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.progressBar), android.graphics.PorterDuff.Mode.MULTIPLY);
        checkPermision();
        hashMapUrls = ConstClass.getScreenMap();
        globalUrlsApi = ConstClass.getApi_lockscreen_url();

        checkInetConnetion();


        sp = getDefaultSharedPreferences(this);
        MobileAds.initialize(this, "ca-app-pub-6392397454770928~4811740801");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6392397454770928/9366612223");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        final GestureDetector gesture = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        tapCountForAds++;
                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                        if (tapCountForAds == 7) {
                            tapCountForAds = 0;
                            mMenu.findItem(R.id.erotic_content).setVisible(true);
                            if (!sp.getBoolean("erotic_content", false)) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putBoolean("erotic_content", true);
                                editor.commit();
                                Toast.makeText(getApplicationContext(), R.string.unlock, Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(getApplicationContext(), R.string.already_unlock, Toast.LENGTH_SHORT).show();
                        } else {
                            mTimer = new Timer();
                            mTimer.schedule(new RandomImageTimerTask(), 1000);
                        }
                        return super.onSingleTapUp(e);
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        //Log.i("fling", "onFling has been called!");
                        final int SWIPE_MIN_DISTANCE = 120;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MIN_DISTANCE
                                    ||
                                    Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE) {
                                if (mInterstitialAd.isLoaded() && countImage >= countImageMax) {
                                    mInterstitialAd.show();
                                    countImage = 0;
                                    if (countImageMax < 30)
                                        countImageMax = countImageMax + 10;
                                }
                                loadDate();
                                countImage++;
                            }

                        } catch (Exception e) {
                            // nothing
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });


    }

    private void checkInetConnetion()
    {
        if(!isOnline())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.error)
                    .setMessage(R.string.not_inet_connection)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    private void checkPermision() {
        int permission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //Log.i(TAG, "Permission to record denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.permision_description)
                        .setTitle(R.string.permission);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        //Log.i(TAG, "Clicked");
                        makeRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRequest();
            }
        }
    }

    private void loadDate() {
        mImageView.setEnabled(false);
        mImageView.setAlpha(0.5F);
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            public void run() {
                String photoUrl = (ConstClass.isUseApi())?loadImageFromNetworkApi():loadImageFromNetwork();
                int countGet=0;
                while (photoUrl == null && hashMapUrls.size()>0 && countGet<5) {
                    photoUrl = (ConstClass.isUseApi())?loadImageFromNetworkApi():loadImageFromNetwork();
                    countGet++;
                }
                bitmap = getBitMapFromUrl(photoUrl, 10);
                mImageView.post(new Runnable() {
                    public void run() {
                        if (bitmap != null) {
                            android.graphics.Matrix matrix = new android.graphics.Matrix();
                            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            setImageToView(bitmap);
                        }
                        try {

                            mImageView.setEnabled(true);
                            mImageView.setAlpha(1F);
                            mProgressBar.setVisibility(View.GONE);
                        } catch (Exception ea) {
                            ea.printStackTrace();
                        }

                    }
                });

            }
        }).start();

    }
    public String loadImageFromNetworkApi() {
        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            String urls = globalUrlsApi;

            int timeout = 10;

            BufferedReader inputStream = null;

            URL jsonUrl = new URL(urls);
            URLConnection dc = jsonUrl.openConnection();
            dc.setConnectTimeout(timeout*1000);
            dc.setReadTimeout(timeout*1000);
            inputStream = new BufferedReader(new InputStreamReader(
                    dc.getInputStream()));
            String resultJson = inputStream.readLine();



            try {
                JSONArray jsonArray = new JSONArray(resultJson);
                return jsonArray.getString(1);



            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }


        } catch (Exception eee) {
            eee.printStackTrace();
            return null;
        }
    }

    public String loadImageFromNetwork() {
        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);


            int timeout = 10;
            List keys = new ArrayList(hashMapUrls.keySet());
            Object obj = keys.get(rdm.nextInt(hashMapUrls.size() - 1));

            String urls = obj.toString() + "/api/read/?num=1&type=photo&start=" + rdm.nextInt(hashMapUrls.get(obj) - 1);

            URL url = new URL(urls);
            HttpURLConnection dc = (HttpURLConnection) url.openConnection();
            dc.setConnectTimeout(timeout * 1000);
            dc.setReadTimeout(timeout * 1000);
            dc.setRequestMethod("GET");
            dc.connect();
            int code = dc.getResponseCode();
            //Log.d("code", code+"");
            String totalCount = null;
            String photoUrl = null;
            if (code == 200) {
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(dc.getInputStream(), null);
                    parser.nextTag();

                    boolean isPhotoUrl = false;

                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) //начальный тег
                        {
                            if (parser.getName().compareTo("posts") == 0)
                                totalCount = parser.getAttributeValue(null, "total");
                            else if (parser.getName().compareTo("photo-url") == 0 && !isPhotoUrl) {
                                isPhotoUrl = true;
                            }
                        } else if (eventType == XmlPullParser.TEXT) {
                            if (isPhotoUrl && photoUrl == null) {
                                photoUrl = parser.getText();
                            }
                        }
                        eventType = parser.next();
                    }




                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (totalCount != null) {
                    if(hashMapUrls.get(obj)!=Integer.parseInt(totalCount))
                        hashMapUrls.put(obj.toString(), Integer.parseInt(totalCount));
                }
            }
            //Log.d("photoUrl",urls);
            return photoUrl;


        } catch (Exception eee) {
            eee.printStackTrace();
            return null;
        }
    }


    private static Bitmap getBitMapFromUrl(String urls, int timeout) {
        try {
            URL url = new URL(urls);
            HttpURLConnection dc = (HttpURLConnection) url.openConnection();
            dc.setConnectTimeout(timeout * 1000);
            dc.setReadTimeout(timeout * 1000);
            dc.setRequestMethod("GET");
            dc.connect();

            int code = dc.getResponseCode();
            //Log.d("code", code+"");
            if (code == 200) {
                InputStream imageStream = dc.getInputStream();
                return BitmapFactory.decodeStream(imageStream);
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setImageToView(Bitmap bitmap) {

        mImageView.invalidate();
        mImageView.setScrollX(0);
        mImageView.setScrollY(0);
        //mImageView.setDrawingCacheEnabled(false);
        mImageView.setImageBitmap(bitmap);
        //mImageView.setDrawingCacheEnabled(true);
        //mImageView.buildDrawingCache();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem erotic_content = menu.findItem(R.id.erotic_content);
        boolean r = sp.getBoolean("erotic_content", false);
        erotic_content.setVisible(r);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if (bitmap != null)
                addPicToGallery();
            return true;
        }
        if (id == R.id.action_share) {
            if (bitmap != null)
                sendToImage();

            return true;
        }
        if (id == R.id.erotic_content) {
            item.setChecked(!item.isChecked());
            if (item.isChecked()) {
                hashMapUrls = ConstClass.getEroticMap();
                globalUrlsApi = ConstClass.getApi_erotic_url();
            }else {
                hashMapUrls = ConstClass.getScreenMap();
                globalUrlsApi = ConstClass.getApi_lockscreen_url();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendToImage() {
        try {
            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
            Uri bitmapUri = Uri.parse(bitmapPath);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            shareIntent.setType("image/jpeg");
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.action_give)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPicToGallery() {
        try {
            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
            Uri bitmapUri = Uri.parse(bitmapPath);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        /*File f = new File(bitmapPath);
        Uri contentUri = Uri.fromFile(f);*/
            mediaScanIntent.setData(bitmapUri);
            sendBroadcast(mediaScanIntent);
            Toast.makeText(this, R.string.save_ok, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show();
        }
    }

    class RandomImageTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tapCountForAds = 0;
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(hashMapUrls.size()>0) {
            MenuItem item = mMenu.findItem(R.id.erotic_content);
            if (item != null) {
                if (item.isChecked())
                    ConstClass.setEroticMap(hashMapUrls, this);
                else
                    ConstClass.setScreenMap(hashMapUrls, this);
            } else
                ConstClass.setScreenMap(hashMapUrls, this);
        }
    }
}
