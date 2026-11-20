package com.example.a3dcompare.web

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 承载原 3D 对比网页的 Compose 容器。
 *
 * 生命周期：
 *  - ON_PAUSE  -> webView.onPause() + pauseTimers()：暂停 WebGL / JS 定时器，降低后台耗电与内存
 *  - ON_RESUME -> webView.onResume() + resumeTimers()
 *  - onDispose -> 停止加载、清空历史、移除并 destroy WebView，释放渲染资源
 *
 * 返回键：WebView 有历史记录时先后退，否则交给系统。
 */
@Composable
fun WebViewScreen(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = context as LifecycleOwner

    val assetLoader = remember(context) { WebAssetConfig.create(context) }

    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    // 生命周期：暂停 / 恢复渲染
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val wv = webView ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    wv.onResume()
                    wv.resumeTimers()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    wv.onPause()
                    wv.pauseTimers()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 销毁：释放 WebView 资源
    DisposableEffect(Unit) {
        onDispose {
            webView?.let { destroyWebView(it) }
            webView = null
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                configureWebView(this)
                webViewClient = AppWebViewClient(assetLoader)
                webChromeClient = AppWebChromeClient(activity)
                loadUrl(url)
                webView = this
            }
        }
    )
}

/** WebView 安全与性能配置（仅 WebView 自身设置，不注册任何 JavascriptInterface） */
@SuppressLint("SetJavaScriptEnabled")
private fun configureWebView(webView: WebView) {
    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true

        // 禁止文件访问：页面只通过 https://appassets 加载资源
        allowFileAccess = false
        allowContentAccess = false
        @Suppress("DEPRECATION")
        allowFileAccessFromFileURLs = false
        @Suppress("DEPRECATION")
        allowUniversalAccessFromFileURLs = false

        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        // 3D 手势由页面自行处理，关闭 WebView 的缩放控件
        builtInZoomControls = false
        displayZoomControls = false
        setSupportZoom(false)

        useWideViewPort = true
        loadWithOverviewMode = true
        mediaPlaybackRequiresUserGesture = true
        cacheMode = WebSettings.LOAD_DEFAULT
    }
}

private fun destroyWebView(webView: WebView) {
    webView.stopLoading()
    webView.loadUrl("about:blank")
    webView.clearHistory()
    (webView.parent as? ViewGroup)?.removeView(webView)
    webView.destroy()
}
