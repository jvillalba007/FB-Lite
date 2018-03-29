package com.jvillalba.fblite;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String facebook_url = "https://m.facebook.com/";

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private WebView webView;
    private BottomNavigationView bottomNavigationView;

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
        webView.setWebChromeClient(new ChromeClient(this));
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
