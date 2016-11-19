package com.forfan.bigbang.component.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.forfan.bigbang.R;
import com.forfan.bigbang.component.base.BaseActivity;
import com.forfan.bigbang.component.contentProvider.SPHelper;
import com.forfan.bigbang.util.ConstantUtil;
import com.forfan.bigbang.util.DensityUtils;
import com.forfan.bigbang.util.LogUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;


public class WebActivity
        extends BaseActivity {
    private static final java.lang.String TAG = "webActivity";
    private LinearLayout mContentLayout;
    private ObjectAnimator mEnterAnim;
    private FrameLayout mFrameLayout;
    private ContentLoadingProgressBar mProgressBar;
    private AppCompatSpinner mTitleSpinner;
    private String mUrl;
    private WebView mWebView;
    private int browserSelection;
    private String mQuery;

    private void initAnim() {
        this.mEnterAnim = ObjectAnimator.ofFloat(this.mFrameLayout, "_enter", new float[]{0.0F, 1.0F}).setDuration(250L);
        this.mEnterAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator paramAnonymousValueAnimator) {
                float f = ((Float) paramAnonymousValueAnimator.getAnimatedValue()).floatValue();
                WebActivity.this.mFrameLayout.setScaleX(f);
                WebActivity.this.mFrameLayout.setScaleY(f);
                if (f == 0.0F) {
                    WebActivity.this.mFrameLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private  boolean isFistIn = true;
    private void initViews() {
        this.mTitleSpinner = ((AppCompatSpinner) findViewById(R.id.title));
        mTitleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SPHelper.save(ConstantUtil.BROWSER_SELECTION, position);
                if(isFistIn){
                    isFistIn = false;
                    return;
                }
                toLoadUrl("", mQuery);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                LogUtil.d(TAG, "onNothingSelected:");

            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.browser_list, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mTitleSpinner.setAdapter(adapter);
        browserSelection = SPHelper.getInt(ConstantUtil.BROWSER_SELECTION, 0);
        mTitleSpinner.setSelection(browserSelection);
        this.mFrameLayout = ((FrameLayout) findViewById(android.R.id.content));
        this.mContentLayout = ((LinearLayout) findViewById(R.id.content_view));
        this.mWebView = new WebView(this);
        this.mContentLayout.addView(this.mWebView, -1, -1);
        this.mProgressBar = ((ContentLoadingProgressBar) findViewById(R.id.progress));
        mProgressBar.onAttachedToWindow();
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        this.mWebView.setBackgroundColor(-1);
        findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.open_chrome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = getUri();
                if (uri != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }
        });
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.mWebView.getLayoutParams();
        int i = DensityUtils.dp2px(this, 2.0F);
        localLayoutParams.setMargins(i, 0, i, i);
        this.mWebView.setLayoutParams(localLayoutParams);
        this.mWebView.setWebViewClient(new WebViewClient() {
            public void onFormResubmission(WebView paramAnonymousWebView, Message paramAnonymousMessage1, Message paramAnonymousMessage2) {
                paramAnonymousMessage2.sendToTarget();
            }

            public boolean shouldOverrideUrlLoading(WebView paramAnonymousWebView, String paramAnonymousString) {
                mUrl = paramAnonymousWebView.getUrl();
                return super.shouldOverrideUrlLoading(paramAnonymousWebView, paramAnonymousString);

            }
        });
        this.mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView paramAnonymousWebView, int paramAnonymousInt) {
                if (paramAnonymousInt == 100) {
                    WebActivity.this.mProgressBar.hide();
                    return;
                }
                WebActivity.this.mProgressBar.setProgress(paramAnonymousInt);
                WebActivity.this.mProgressBar.show();
            }
        });
        this.mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private Uri getUri() {
        if (!TextUtils.isEmpty(mUrl)) {
            return Uri.parse(mUrl);
        } else {
            if (!TextUtils.isEmpty(mQuery))
                return Uri.parse(getUrlStrBySelect(mQuery));
        }
        return null;
    }

    /**
     *
     */
    private void toLoadUrl(String url, String query) {
        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        } else {
            String url_ = getUrlStrBySelect(query);
            mWebView.loadUrl(url_);
        }

    }


    private String getUrlStrBySelect(String query) {
        String url = "";
        switch (SPHelper.getInt(ConstantUtil.BROWSER_SELECTION, 0)) {
            case 0:
                url = "https://m.baidu.com/s?word=";
                break;
            case 1:
                url = "https://www.google.com/search?q=";
                break;
//            case 2:
//                url ="http://m.so.com/s?q=";
//                break;
            case 2:
                url = "https://www.bing.com/search?q=";
                break;
            case 3:
                url = "https://s.m.taobao.com/h5?event_submit_do_new_search_auction=1&_input_charset=utf-8&topSearch=1&atype=b&searchfrom=1&action=home%3Aredirect_app_action&from=1&sst=1&n=20&buying=buyitnow&q=";
                break;
        }

        try {
            return url + URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        }
        return url + query;
    }


    private void initWindow() {
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(localDisplayMetrics);
        localLayoutParams.width = ((int) (localDisplayMetrics.widthPixels * 0.99D));
        localLayoutParams.gravity = 17;
        localLayoutParams.height = ((int) (localDisplayMetrics.heightPixels * 0.8D));
        getWindow().setAttributes(localLayoutParams);
        getWindow().setGravity(17);
        getWindow().getAttributes().windowAnimations = R.anim.anim_scale_in;
    }

    private void setConfigCallback(WindowManager paramWindowManager) {
        try {
            Field localField = WebView.class.getDeclaredField("mWebViewCore").getType().getDeclaredField("mBrowserFrame").getType().getDeclaredField("sConfigCallback");
            localField.setAccessible(true);
            Object localObject = localField.get(null);
            if (localObject == null) {
                return;
            }
            localField = localField.getType().getDeclaredField("mWindowManager");
            localField.setAccessible(true);
            localField.set(localObject, paramWindowManager);
            return;
        } catch (Exception ex) {
        }
    }


    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setFinishOnTouchOutside(true);
        setContentView(R.layout.activity_web);
        this.mUrl = getIntent().getStringExtra("url");
        this.mQuery = getIntent().getStringExtra("query");
        initWindow();
        initViews();
        initAnim();
        this.mEnterAnim.start();

        toLoadUrl(mUrl, mQuery);
        setConfigCallback((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
    }

    protected void onDestroy() {
        setConfigCallback(null);
        super.onDestroy();
        if (this.mWebView != null) {
            ((ViewGroup) this.mWebView.getParent()).removeView(this.mWebView);
            this.mWebView.removeAllViews();
            this.mWebView.destroy();
            this.mWebView = null;
        }
        if (mProgressBar != null) {
            mProgressBar.onDetachedFromWindow();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.isFocused() && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}