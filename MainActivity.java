package com.bikemeter.app;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Opens the Bike Meter page served by the ESP32 speedometer.
 *  - Sends this app's traffic over the BikeMeter Wi-Fi even when Android says
 *    "no internet" (otherwise Android quietly falls back to mobile data).
 *  - Keeps the screen on while the app is open.
 *  - Retries every 3 seconds until the speedometer answers.
 */
public class MainActivity extends Activity {

    private WebView web;
    private ConnectivityManager cm;
    private ConnectivityManager.NetworkCallback callback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String url;
    private boolean started = false;

    private final Runnable retry = new Runnable() {
        @Override
        public void run() {
            web.loadUrl(url);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        url = getString(R.string.app_url);

        web = new WebView(this);
        setContentView(web);

        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    showOffline();
                }
            }
        });

        bindToWifiThenLoad();
    }

    private void bindToWifiThenLoad() {
        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                cm.bindProcessToNetwork(network);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        started = true;
                        web.loadUrl(url);
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                cm.bindProcessToNetwork(null);
            }
        };
        try {
            cm.requestNetwork(request, callback);
        } catch (Exception ignored) {
            // fall through: the timer below still opens the page
        }

        // If Android gives no Wi-Fi callback, open the page anyway.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!started) {
                    started = true;
                    web.loadUrl(url);
                }
            }
        }, 2500);
    }

    private void showOffline() {
        String html = "<html><body style='margin:0;padding:32px;font-family:sans-serif;"
                + "background:#0F2923;color:#E6F4EE'>"
                + "<h2 style='color:#62EBBD'>Looking for your speedometer</h2>"
                + "<p>Join the <b>BikeMeter</b> Wi-Fi on your phone and make sure the bike is switched on."
                + " This screen retries by itself.</p></body></html>";
        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        handler.removeCallbacks(retry);
        handler.postDelayed(retry, 3000);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        try {
            cm.unregisterNetworkCallback(callback);
        } catch (Exception ignored) {
        }
        cm.bindProcessToNetwork(null);
        web.destroy();
        super.onDestroy();
    }
}
