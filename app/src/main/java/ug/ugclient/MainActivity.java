package ug.ugclient;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity  {
    @BindView(R.id.webView)
    WebView mWebView;
    @BindView(R.id.not_net_img)
    ImageView net_img;
    private boolean isBackKeyPressed = false;
    private ZLoadingDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        intDialog();
        iniData();
    }

    private void intDialog() {
        dialog = new ZLoadingDialog(MainActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)
                .setLoadingColor(Color.GRAY)
                .setHintText("Loading...")
//              .setHintTextSize(16) // 设置字体大小
                .setHintTextColor(Color.WHITE); // 设置字体颜色
//                .show();
    }


    public void iniData(){
        boolean isNetAble = NetUtil.isNetConnection(this);
        if (!isNetAble) {
            mWebView.setVisibility(View.GONE);
            net_img.setVisibility(View.VISIBLE);
            net_img.setImageResource(R.mipmap.ic_launcher);
            Toast.makeText(this, getResources().getText(R.string.error_net), Toast.LENGTH_LONG).show();
        }else {
            net_img.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            initWebView();

        }
    }

    @OnClick({R.id.not_net_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.not_net_img:
                iniData();
                break;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {

        mWebView.setVisibility(View.VISIBLE);
        // 开启JavaScript支持
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

       // mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");//添加javaScript回掉

        // 设置WebView是否支持使用屏幕控件或手势进行缩放，默认是true，支持缩放
        webSettings.setSupportZoom(true);

        // 设置WebView是否使用其内置的变焦机制，该机制集合屏幕缩放控件使用，默认是false，不使用内置变焦机制。
        webSettings.setBuiltInZoomControls(true);

        // 设置是否开启DOM存储API权限，默认false，未开启，设置为true，WebView能够使用DOM storage API
        webSettings.setDomStorageEnabled(true);

        // 触摸焦点起作用.如果不设置，则在点击网页文本输入框时，不能弹出软键盘及不响应其他的一些事件。
        mWebView.requestFocus();

        // 设置此属性,可任意比例缩放,设置webview推荐使用的窗口
        webSettings.setUseWideViewPort(true);

        // 设置webview加载的页面的模式,缩放至屏幕的大小
        // webSettings.setLoadWithOverviewMode(true);

        // 优先使用缓存
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        // 加载链接
        mWebView.loadUrl(Constant.BASE_URL);
        mWebView.setWebChromeClient(new WebChromeClient());

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // 在开始加载网页时会回调
                dialog.show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 拦截 url 跳转,在里边添加点击链接跳转或者操作
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 在结束加载网页时会回调
                dialog.cancel();
                // 获取页面内容
                view.loadUrl("javascript:window.java_obj.showSource("
                        + "document.getElementsByTagName('html')[0].innerHTML);");

                // 获取解析<meta name="share-description" content="获取到的值">
                view.loadUrl("javascript:window.java_obj.showDescription("
                        + "document.querySelector('meta[name=\"share-description\"]').getAttribute('content')"
                        + ");");
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // 加载错误的时候会回调，在其中可做错误处理，比如再请求加载一次，或者提示404的错误页面
                super.onReceivedError(view, errorCode, description, failingUrl);
                mWebView.loadUrl("file:///android_asset/error.html");
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // 在每一次请求资源时，都会通过这个函数来回调
                return super.shouldInterceptRequest(view, request);
            }

        });
    }


    public final class InJavaScriptLocalObj
    {
        @JavascriptInterface
        public void showSource(String html) {
            System.out.println("====>html=" + html);
        }

        @JavascriptInterface
        public void showDescription(String str) {
            System.out.println("====>html=" + str);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(isBackKeyPressed) {
              //  finish();
                System.exit(0);//退出程序
            }
            else {
                if(mWebView.canGoBack()) {
                    mWebView.goBack();//返回上一页面
                    return true;
                } else {
                    isBackKeyPressed = true;
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        public void run() {
                            isBackKeyPressed = false;
                        }
                    };
                    timer.schedule(timerTask, 2000);//600毫秒后无再次点击，则复位
                }
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
