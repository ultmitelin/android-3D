package com.example.a3dcompare.web

import android.app.Activity
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout

/**
 * 处理 WebView 的 Chrome 级回调。
 *
 * 当前阶段（第二阶段第一部分）：
 *  - 全屏 (requestFullscreen)：实现 onShowCustomView / onHideCustomView，保证 API 可用
 *  - 文件选择 (<input type=file>)：暂未实现选择器，先取消回调，避免 WebView 挂起
 *
 * 文件选择与完整全屏交互将在下一阶段实现。
 */
class AppWebChromeClient(private val activity: Activity) : WebChromeClient() {

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        if (customView != null) {
            callback.onCustomViewHidden()
            return
        }
        customView = view
        customViewCallback = callback

        val decor = activity.window.decorView as FrameLayout
        decor.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    override fun onHideCustomView() {
        val view = customView ?: return
        val decor = activity.window.decorView as FrameLayout
        decor.removeView(view)
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<android.net.Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        // 文件选择器将在下一阶段实现；这里先明确取消，避免 WebView 等待回调
        filePathCallback.onReceiveValue(null)
        return true
    }
}
