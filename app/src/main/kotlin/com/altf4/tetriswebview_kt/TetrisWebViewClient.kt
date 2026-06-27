package com.altf4.tetriswebview_kt

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

class TetrisWebViewClient : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val requestUrl = request?.url?.toString() ?: return null
        val allowedDomains = arrayOf("tetris.com", "play.tetris.com")
        
        if (allowedDomains.any { requestUrl.contains(it) }) {
            return super.shouldInterceptRequest(view, request)
        }
        
        return WebResourceResponse("text/plain", "utf-8", null)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        view?.evaluateJavascript(TetrisScripts.INJECT_CSS, null)

    }
}
