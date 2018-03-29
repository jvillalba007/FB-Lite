package com.jvillalba.fblite;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String facebook_url = "https://m.facebook.com/";

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private WebView webView;
    private ChromeClient chromeClient;
    private BottomNavigationView bottomNavigationView;

    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int PERMISSIONS_STORAGE = 1;
    private boolean result;
    /** File upload callback for platform versions prior to Android 5.0 */
    private ValueCallback<Uri> mFileUploadCallbackFirst;
    private ValueCallback<Uri[]> filePathCallbackGlobal;
    /** File upload callback for Android 5.0+ */
    private ValueCallback<Uri[]> mFileUploadCallbackSecond;
    private String mLanguageIso3;
    private static final String CHARSET_DEFAULT = "UTF-8";
    private static final int REQUEST_CODE_FILE_PICKER = 51426;
    private int mRequestCodeFilePicker = REQUEST_CODE_FILE_PICKER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar);
        swipeContainer = findViewById(R.id.swipe_container);
        webView = findViewById(R.id.webview);
        bottomNavigationView = findViewById (R.id.BottomNavigationView);

        initWebView();
        bottomNavigationListener();
        swipeContainer.setOnRefreshListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_STORAGE:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    chromeClient.checkPermission();
                }else{
                    chromeClient.checkPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fb_browser, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!webView.canGoBack()) {
            menu.getItem(0).setEnabled(false);
            menu.getItem(0).getIcon().setAlpha(130);
        } else {
            menu.getItem(0).setEnabled(true);
            menu.getItem(0).getIcon().setAlpha(255);
        }

        if (!webView.canGoForward()) {
            menu.getItem(1).setEnabled(false);
            menu.getItem(1).getIcon().setAlpha(130);
        } else {
            menu.getItem(1).setEnabled(true);
            menu.getItem(1).getIcon().setAlpha(255);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case (R.id.action_back):
                back();
                break;
            case(R.id.action_forward):
                forward();
                break;
            case (R.id.share):
                share();
                break;
            case (R.id.about):
                about();
                break;
        }
        return true;
    }

    private void back() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    private void forward() {
        if (webView.canGoForward()) {
            webView.goForward();
        }
    }

    private void share ()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "FB Lite");
        shareIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());  
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "Share"));
    }

    private void about()
    {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("FB Lite")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Copyright © 2018 Javier Villalba\nSource Code\ngithub.com/fighthawkarg/FB-Lite")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }


    private void initWebView() {
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(facebook_url);
        chromeClient = new ChromeClient(this);
        webView.setWebChromeClient(chromeClient);
        webView.setWebViewClient(new FbBrowser());
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
    }

    private class ChromeClient extends WebChromeClient {
        Context context;

        public ChromeClient(Context context) {
            super();
            this.context = context;
        }

        // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileInput(uploadMsg, null);
        }

        // file upload callback (Android 5.0 (API level 21) -- current) (public method)
        @SuppressWarnings("all")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            filePathCallbackGlobal = filePathCallback;
            return checkPermission();
        }

        public boolean checkPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Comprobar si ha aceptado, no ha aceptado, o nunca se le ha preguntado
                if(checkPermissionList()) {
                    openFileInput(null,filePathCallbackGlobal );
                    result = true;
                }
                else {
                    // Ha denegado o es la primera vez que se le pregunta
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // No se le ha preguntado aún
                        ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS, PERMISSIONS_STORAGE);
                    }else {
                        // Ha denegado
                       Snackbar snackbar = Snackbar.make(findViewById(R.id.CoordinatorLayout), "Storage Permission Fail", Snackbar.LENGTH_INDEFINITE)
                                .setAction("ENABLE", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(MainActivity.this, "Please, enable the storage permission", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        i.addCategory(Intent.CATEGORY_DEFAULT);
                                        i.setData(Uri.parse("package:" + getPackageName()));
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        startActivity(i);
                                    }
                                });
                        snackbar.setActionTextColor(Color.YELLOW);
                        snackbar.show();
                        result = false;
                    }
                }
            } else {
                openFileInput(null, filePathCallbackGlobal);
                result = true;
            }
            return result;
        }

        private boolean checkPermissionList() {
            boolean result = false;
            for (String permission:PERMISSIONS)
            {
                result = CheckPermission(permission);
            }
            return result;
        }

        private boolean CheckPermission(String permission) {
            int result = MainActivity.this.checkCallingOrSelfPermission(permission);
            return result == PackageManager.PERMISSION_GRANTED;
        }


        /** Provides localizations for the 25 most widely spoken languages that have a ISO 639-2/T code */
        private String getFileUploadPromptLabel() {
            try {
                switch (mLanguageIso3) {
                    case "zho":
                        return decodeBase64("6YCJ5oup5LiA5Liq5paH5Lu2");
                    case "spa":
                        return "Elija un archivo";
                    case "hin":
                        return decodeBase64("4KSP4KSVIOCkq+CkvOCkvuCkh+CksiDgpJrgpYHgpKjgpYfgpII=");
                    case "ben":
                        return decodeBase64("4KaP4KaV4Kaf4Ka/IOCmq+CmvuCmh+CmsiDgpqjgpr/gprDgp43gpqzgpr7gpprgpqg=");
                    case "ara":
                        return decodeBase64("2KfYrtiq2YrYp9ixINmF2YTZgSDZiNin2K3Yrw==");
                    case "por":
                        return "Escolha um arquivo";
                    case "rus":
                        return decodeBase64("0JLRi9Cx0LXRgNC40YLQtSDQvtC00LjQvSDRhNCw0LnQuw==");
                    case "jpn":
                        return decodeBase64("MeODleOCoeOCpOODq+OCkumBuOaKnuOBl+OBpuOBj+OBoOOBleOBhA==");
                    case "pan":
                        return decodeBase64("4KiH4Kmx4KiVIOCoq+CovuCoh+CosiDgqJrgqYHgqKPgqYs=");
                    case "deu":
                        return "Wähle eine Datei";
                    case "jav":
                        return "Pilih siji berkas";
                    case "msa":
                        return "Pilih satu fail";
                    case "tel":
                        return decodeBase64("4LCS4LCVIOCwq+CxhuCxluCwsuCxjeCwqOCxgSDgsI7gsILgsJrgsYHgsJXgsYvgsILgsKHgsL8=");
                    case "vie":
                        return decodeBase64("Q2jhu41uIG3hu5l0IHThuq1wIHRpbg==");
                    case "kor":
                        return decodeBase64("7ZWY64KY7J2YIO2MjOydvOydhCDshKDtg50=");
                    case "fra":
                        return "Choisissez un fichier";
                    case "mar":
                        return decodeBase64("4KSr4KS+4KSH4KSyIOCkqOCkv+CkteCkoeCkvg==");
                    case "tam":
                        return decodeBase64("4K6S4K6w4K+BIOCuleCvh+CuvuCuquCvjeCuquCviCDgrqTgr4fgrrDgr43grrXgr4E=");
                    case "urd":
                        return decodeBase64("2KfbjNqpINmB2KfYptmEINmF24zauiDYs9uSINin2YbYqtiu2KfYqCDaqdix24zaug==");
                    case "fas":
                        return decodeBase64("2LHYpyDYp9mG2KrYrtin2Kgg2qnZhtuM2K8g24zaqSDZgdin24zZhA==");
                    case "tur":
                        return "Bir dosya seçin";
                    case "ita":
                        return "Scegli un file";
                    case "tha":
                        return decodeBase64("4LmA4Lil4Li34Lit4LiB4LmE4Lif4Lil4LmM4Lir4LiZ4Li24LmI4LiH");
                    case "guj":
                        return decodeBase64("4KqP4KqVIOCqq+CqvuCqh+CqsuCqqOCrhyDgqqrgqrjgqoLgqqY=");
                }
            }
            catch (Exception e) { }

            // return English translation by default
            return "Choose a file";
        }

        private String decodeBase64(final String base64) throws IllegalArgumentException, UnsupportedEncodingException {
            final byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return new String(bytes, CHARSET_DEFAULT);
        }

        @SuppressLint("NewApi")
        private void openFileInput(final ValueCallback<Uri> fileUploadCallbackFirst, final ValueCallback<Uri[]> fileUploadCallbackSecond) {
            if (mFileUploadCallbackFirst != null) {
                mFileUploadCallbackFirst.onReceiveValue(null);
            }
            mFileUploadCallbackFirst = fileUploadCallbackFirst;

            if (mFileUploadCallbackSecond != null) {
                mFileUploadCallbackSecond.onReceiveValue(null);
            }
            mFileUploadCallbackSecond = fileUploadCallbackSecond;

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");

            startActivityForResult(Intent.createChooser(i, getFileUploadPromptLabel()), mRequestCodeFilePicker);

        }

    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == mRequestCodeFilePicker) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    if (mFileUploadCallbackFirst != null) {
                        mFileUploadCallbackFirst.onReceiveValue(intent.getData());
                        mFileUploadCallbackFirst = null;
                    }
                    else if (mFileUploadCallbackSecond != null) {
                        Uri[] dataUris;
                        try {
                            dataUris = new Uri[] { Uri.parse(intent.getDataString()) };
                        }
                        catch (Exception e) {
                            dataUris = null;
                        }

                        mFileUploadCallbackSecond.onReceiveValue(dataUris);
                        mFileUploadCallbackSecond = null;
                    }
                }
            }
            else {
                if (mFileUploadCallbackFirst != null) {
                    mFileUploadCallbackFirst.onReceiveValue(null);
                    mFileUploadCallbackFirst = null;
                }
                else if (mFileUploadCallbackSecond != null) {
                    mFileUploadCallbackSecond.onReceiveValue(null);
                    mFileUploadCallbackSecond = null;
                }
            }
        }
    }

    private class FbBrowser extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            invalidateOptionsMenu();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            progressBar.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }
    }

    private void bottomNavigationListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                    switch (item.getItemId()) {
                        case R.id.home:
                            webView.loadUrl(facebook_url);
                            break;
                        case R.id.chat:
                            concat_url("messages");
                            break;
                        case R.id.notifications:
                            concat_url("notifications");
                            break;
                    }
                    return true;
                }
            });
    }

    private void concat_url(String path) {
        String url = facebook_url.concat(path);
        webView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRefresh() {
        webView.reload();
        swipeContainer.setRefreshing(false);
    }
}
