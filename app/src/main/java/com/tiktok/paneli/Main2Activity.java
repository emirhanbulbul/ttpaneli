package com.tiktok.paneli;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.onesignal.OneSignal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Main2Activity extends AppCompatActivity {
    // File Upload
    private static String file_type     = "*/*";
    private String cam_file_data = null;
    private ValueCallback<Uri> file_data;
    private ValueCallback<Uri[]> file_path;
    private final static int file_req_code = 1;
    // File Upload End
    private RewardedAd mRewardedAd;
    BottomNavigationView bottomNavigationView;

    SharedPreferences preferences;
    private String webUrl;
    private WebView webView;
    private ProgressBar mProgressBar;
    ProgressDialog progressDialog;
    private String ONESIGNAL_APP_ID;
    private AdView mAdView;
    String bottomBarMenu;
    String admobBanner;
    String actionbar;
    LocalDataManager localDataManager;
    TextView durum, puan;
    private InterstitialAd mInterstitialAd;

    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        actionbar = getString(R.string.actionBar);
        ActionBar actionBar = getSupportActionBar();
        localDataManager = new LocalDataManager();

        durum = findViewById(R.id.durum);
        puan = findViewById(R.id.puan);
        puan.setText("Toplam Puan: " + localDataManager.getSharedPreference(getApplicationContext(),"point", "0"));

        if(Integer.parseInt(localDataManager.getSharedPreference(getApplicationContext(),"point", "0")) < 500){
            durum.setText("Yeni Kullanıcı");
        }
        else{
            durum.setText("Premium");
        }

        if(actionbar.equals("true")){
            actionBar.show();
        }
        else{
            actionBar.hide();
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        bottomNavigationView = findViewById(R.id.menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(menulistener);
        webUrl = getString(R.string.siteURL);

        // PROGRESS BAR
        mProgressBar = findViewById(R.id.pb);


        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url = preferences.getString("url","");


        Intent loginpage = getIntent();
        String tempUrl = loginpage.getStringExtra("link");
        if(tempUrl != null) {
            webUrl = tempUrl;
        }
        // DEEPLINK GELEN VERİ
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        String intentData = intent.getDataString();
        if(intentData != null){
            webUrl = intentData;
        }




        // ADMOB BANNER
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        admobBanner = getString(R.string.admobBanner);
        if(admobBanner.equals("true")){
            mAdView.setVisibility(View.VISIBLE);
        }
        else{
            mAdView.setVisibility(View.GONE);
        }


        // ONE SIGNAL
        ONESIGNAL_APP_ID = getString(R.string.oneSignalAppId);
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);




        webView = findViewById(R.id.webview);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);



        WebSettings webSettings = webView.getSettings();
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCachePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/cache");
        webSettings.setDatabaseEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setUserAgentString(USER_AGENT);

        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (Build.VERSION.SDK_INT > 17) { webSettings.setMediaPlaybackRequiresUserGesture(false); } // mp3

        if(savedInstanceState !=null){
            webView.restoreState(savedInstanceState);
        }
        else
        {
            checkConnection();
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl(webUrl);
                }
            });
        }




        //SWIPE REFRESH LAYOUT
        final SwipeRefreshLayout finalMySwipeRefreshLayout1;
        finalMySwipeRefreshLayout1 = findViewById(R.id.swiperefresh);
        finalMySwipeRefreshLayout1.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.loadUrl(webView.getUrl());

            }
        });



        webView.setWebViewClient(new WebViewClient() {


            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                try {
                    webView.stopLoading();
                } catch (Exception e) {
                }

                if (webView.canGoBack()) {
                    webView.goBack();
                }

                webView.loadUrl("about:blank");
                Intent intent2 = new Intent(Main2Activity.this,Connection.class);
                startActivity(intent2);
                finish();
                super.onReceivedError(webView, errorCode, description, failingUrl);
            }


            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.contains(".pdf")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "application/pdf");
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {

                    }
                }

                if(url.startsWith("tel:") || url.startsWith("whatsapp:") || url.startsWith("facebook.com")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                if(url.startsWith("https://www.instagram.com/")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    intent.setPackage("com.instagram.android");
                    startActivity(intent);
                    return true;

                }


                else {
                    webView.loadUrl(url);
                }

                return false;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                interstialAds();
                // Visible the progressbar
                mProgressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError er) {
                handler.proceed();
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                finalMySwipeRefreshLayout1.setRefreshing(false);
                mProgressBar.setVisibility(View.GONE);
            }



        });

        webView.loadUrl(webUrl);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                progressDialog.dismiss();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimeType,
                                        long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading File...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                assert dm != null;
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Dosya İndiriliyor", Toast.LENGTH_LONG).show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {


            private View mCustomView;
            private WebChromeClient.CustomViewCallback mCustomViewCallback;
            private int mOriginalOrientation;
            private int mOriginalSystemUiVisibility;

            public Bitmap getDefaultVideoPoster()
            {
                if (mCustomView == null) {
                    return null;
                }
                return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
            }

            public void onHideCustomView()
            {
                ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                setRequestedOrientation(this.mOriginalOrientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            }

            public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
            {
                if (this.mCustomView != null)
                {
                    onHideCustomView();
                    return;
                }
                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                this.mOriginalOrientation = getRequestedOrientation();
                this.mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(3846);
            }

            /*-- handling input[type="file"] requests for android API 21+ --*/
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                if(file_permission() && Build.VERSION.SDK_INT >= 21) {
                    file_path = filePathCallback;
                    Intent takePictureIntent = null;
                    Intent takeVideoIntent = null;

                    boolean includeVideo = false;
                    boolean includePhoto = false;

                    /*-- checking the accept parameter to determine which intent(s) to include --*/

                    paramCheck:
                    for (String acceptTypes : fileChooserParams.getAcceptTypes()) {
                        String[] splitTypes = acceptTypes.split(", ?+");
                        /*-- although it's an array, it still seems to be the whole value; split it out into chunks so that we can detect multiple values --*/
                        for (String acceptType : splitTypes) {
                            switch (acceptType) {
                                case "*/*":
                                    includePhoto = true;
                                    includeVideo = true;
                                    break paramCheck;
                                case "image/*":
                                    includePhoto = true;
                                    break;
                                case "video/*":
                                    includeVideo = true;
                                    break;
                            }
                        }
                    }

                    if (fileChooserParams.getAcceptTypes().length == 0) {

                        /*-- no `accept` parameter was specified, allow both photo and video --*/

                        includePhoto = true;
                        includeVideo = true;
                    }

                    if (includePhoto) {
                        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(Main2Activity.this.getPackageManager()) != null) {
                            File photoFile = null;
                            try {
                                photoFile = create_image();
                                takePictureIntent.putExtra("PhotoPath", cam_file_data);
                            } catch (IOException ex) {
                            }
                            if (photoFile != null) {
                                cam_file_data = "file:" + photoFile.getAbsolutePath();
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            } else {
                                cam_file_data = null;
                                takePictureIntent = null;
                            }
                        }
                    }

                    if (includeVideo) {
                        takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        if (takeVideoIntent.resolveActivity(Main2Activity.this.getPackageManager()) != null) {
                            File videoFile = null;
                            try {
                                videoFile = create_video();
                            } catch (IOException ex) {
                            }
                            if (videoFile != null) {
                                cam_file_data = "file:" + videoFile.getAbsolutePath();
                                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
                            } else {
                                cam_file_data = null;
                                takeVideoIntent = null;
                            }
                        }
                    }

                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType(file_type);


                    Intent[] intentArray;
                    if (takePictureIntent != null && takeVideoIntent != null) {
                        intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
                    } else if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else if (takeVideoIntent != null) {
                        intentArray = new Intent[]{takeVideoIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooserIntent, file_req_code);
                    return true;
                } else {
                    return false;
                }
            }




        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;

            /*-- if file request cancelled; exited camera. we need to send null value to make future attempts workable --*/
            if (resultCode == Activity.RESULT_CANCELED) {
                file_path.onReceiveValue(null);
                return;
            }

            /*-- continue if response is positive --*/
            if(resultCode== Activity.RESULT_OK){
                if(null == file_path){
                    return;
                }
                ClipData clipData;
                String stringData;

                try {
                    clipData = intent.getClipData();
                    stringData = intent.getDataString();
                }catch (Exception e){
                    clipData = null;
                    stringData = null;
                }
                if (clipData == null && stringData == null && cam_file_data != null) {
                    results = new Uri[]{Uri.parse(cam_file_data)};
                }else{
                    if (clipData != null) {
                        final int numSelectedFiles = clipData.getItemCount();
                        results = new Uri[numSelectedFiles];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            results[i] = clipData.getItemAt(i).getUri();
                        }
                    } else {
                        try {
                            Bitmap cam_photo = (Bitmap) intent.getExtras().get("data");
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            cam_photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            stringData = MediaStore.Images.Media.insertImage(this.getContentResolver(), cam_photo, null, null);
                        }catch (Exception ignored){}

                        results = new Uri[]{Uri.parse(stringData)};
                    }
                }
            }

            file_path.onReceiveValue(results);
            file_path = null;
        }else{
            if(requestCode == file_req_code){
                if(null == file_data) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                file_data.onReceiveValue(result);
                file_data = null;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
    public boolean file_permission(){
        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, 1);
            return false;
        }else{
            return true;
        }
    }
    private File create_image() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }
    private File create_video() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name    = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name     = "file_"+file_name+"_";
        File sd_directory   = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".3gp", sd_directory);
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {

            Intent intent = new Intent(Main2Activity.this, LoginActivity.class);
            startActivity(intent);
            finish();






        }
    }
    public void checkConnection(){

        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        if(wifi.isConnected()){
            webView.loadUrl(webUrl);



        }
        else if (mobileNetwork.isConnected()){
            webView.loadUrl(webUrl);

        }
        else{

            Intent intent2 = new Intent(Main2Activity.this,Connection.class);
            startActivity(intent2);
            finish();
        }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        webView.saveState(outState);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener menulistener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.bir:
                    rewardedAds();
                    break;
                case R.id.iki:
                    Intent intent = new Intent(Main2Activity.this, PayActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
            return true;
        }
    };

    public void interstialAds(){
        if(Integer.parseInt(localDataManager.getSharedPreference(getApplicationContext(),"point", "0")) < 500){
            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(this,"ca-app-pub-6256669589112472/2480946652", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            mInterstitialAd = interstitialAd;
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            mInterstitialAd = null;
                        }
                    });

            if (mInterstitialAd != null) {
                mInterstitialAd.show(Main2Activity.this);
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
            }

        }


    }

    public void rewardedAds(){
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-6256669589112472/4723966612",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                    }
                });
        if (mRewardedAd != null) {
           String point =  localDataManager.getSharedPreference(getApplicationContext(),"point", "0");
           System.out.println("puan: " + point);

            int pointInt =  Integer.parseInt(point);
           pointInt = pointInt + 5;


            localDataManager.setSharedPreference(getApplicationContext(), "point", Integer.toString(pointInt));


            Activity activityContext = Main2Activity.this;
            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    puan.setText("Toplam Puan: " + localDataManager.getSharedPreference(getApplicationContext(),"point", "0"));
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                }
            });
        } else {
        }
    }




}