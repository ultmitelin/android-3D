package com.example.a3dcompare.web

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

/**
 * 负责：
 *  1. 通过 WebViewAssetLoader 拦截本地资源请求（assets + 私有模型目录）
 *  2. 限制导航：只允许应用自身的虚拟域名，外部 http(s) 链接交给系统浏览器
 *  3. 记录页面加载错误；渲染进程崩溃（内存不足）时不让应用整体崩溃
 */
class AppWebViewClient(
    private val assetLoader: WebViewAssetLoader,
    private val onPageFinishedCallback: () -> Unit = {},
    private val onRenderGone: () -> Unit = {}
) : WebViewClientCompat() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        if (isAppOrigin(uri)) {
            return false
        }
        if (uri.scheme == "http" || uri.scheme == "https") {
            try {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) {
                Log.w(TAG, "无法打开外部链接: $uri", e)
            }
        }
        // 无论如何都不在 WebView 内跳转到外部地址
        return true
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        // 仅本应用域名交给 AssetLoader；其它请求直接拒绝（返回 403 空响应）
        if (!isAppOrigin(request.url)) {
            Log.w(TAG, "拦截外部请求: ${request.url}")
            return WebResourceResponse("text/plain", "utf-8", 403, "Forbidden", emptyMap(), null)
        }
        return assetLoader.shouldInterceptRequest(request.url)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        Log.i(TAG, "页面加载完成: $url")
        onPageFinishedCallback()
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        Log.e(TAG, "WebView 渲染进程退出（可能是内存不足）")
        onRenderGone()
        // 返回 true 表示应用已自行处理，避免进程被系统终止
        return true
    }

    private fun isAppOrigin(uri: Uri): Boolean =
        uri.scheme == "https" && uri.host == WebAssetConfig.DOMAIN

    companion object {
        private const val TAG = "AppWebViewClient"
    }
}
