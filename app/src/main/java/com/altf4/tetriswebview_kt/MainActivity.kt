package com.altf4.tetriswebview_kt

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.altf4.tetriswebview_kt.ui.theme.TetrisWebViewktTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TetrisWebViewktTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TetrisWebView(
                        url = "https://play.tetris.com/",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
fun triggerGamePause(webView: WebView){
    webView.evaluateJavascript("""
    (function() {
      try {
         var frame = document.querySelector('iframe');
         if (frame && frame.contentDocument) {
            var canvas = frame.contentDocument.querySelector('canvas');
            if (canvas) {
               canvas.focus();
               var downEvent = new KeyboardEvent('keydown', { key: 'Escape', keyCode: 27, bubbles: true });
               var upEvent = new KeyboardEvent('keyup', { key: 'Escape', keyCode: 27, bubbles: true });
               canvas.dispatchEvent(downEvent);
               canvas.dispatchEvent(upEvent);
            }
         }
      } catch(e) { console.log(e); }
    })();
""".trimIndent(), null)
}
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TetrisWebView(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val requestUrl = request?.url?.toString() ?: return null
                    val allowedDomains = arrayOf("tetris.com", "play.tetris.com")
                    for (domain in allowedDomains) {
                        if (requestUrl.contains(domain)) {
                            return super.shouldInterceptRequest(view, request)
                        }
                    }
                    return WebResourceResponse("text/plain", "utf-8", null)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val cssStyles =
                        "* { background: black !important; } " +
                                ".tetris-container { position: fixed !important; top: 0 !important; left: 0 !important; width: 100vw !important; height: 100vh !important; } " +
                                "#tetris-game-iframe { width: 100%; height: 100%; border: none; } " +
                                "footer, nav, div:has(> div > video) { display: none !important; }"

                    view?.evaluateJavascript(
                        """
                        (function() {
                            var s = document.createElement('style');
                            s.textContent = '$cssStyles';
                            document.head.appendChild(s);
                        })();
                        """.trimIndent(),
                        null
                    )
                }
            }

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            loadUrl(url)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val pauseExecution = Runnable {
            webView.onPause()
            webView.pauseTimers()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    webView.dispatchWindowVisibilityChanged(android.view.View.GONE)
                    webView.visibility = android.view.View.GONE
                    webView.postDelayed(pauseExecution, 100)
                }

                Lifecycle.Event.ON_RESUME -> {
                    webView.removeCallbacks(pauseExecution)
                    webView.onResume()
                    webView.resumeTimers()
                    webView.visibility = android.view.View.VISIBLE
                    webView.dispatchWindowVisibilityChanged(android.view.View.VISIBLE)
                }

                Lifecycle.Event.ON_DESTROY -> {
                    webView.destroy()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = true) {
        triggerGamePause(webView)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { webView }
    )
}