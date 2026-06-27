package com.altf4.tetriswebview_kt

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TetrisWebView(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var wasAlreadyPaused = false

    val webView = remember {
        WebView(context).apply {

            setBackgroundColor(android.graphics.Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = TetrisWebViewClient()
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
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
                    webView.evaluateJavascript(TetrisScripts.CHECK_IS_PAUSED) { result ->
                        wasAlreadyPaused = result == "true"
                        toggleGamePause(webView)
                        webView.postDelayed(pauseExecution, 100)
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    webView.removeCallbacks(pauseExecution)
                    webView.onResume()
                    webView.resumeTimers()
                    webView.postDelayed({
                        if (!wasAlreadyPaused) {
                            toggleGamePause(webView)
                        }
                    }, 300)
                }

                Lifecycle.Event.ON_DESTROY -> {
                    webView.destroy()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = true) {
        toggleGamePause(webView)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { webView }
    )
}

private fun toggleGamePause(webView: WebView) {
    webView.evaluateJavascript(TetrisScripts.TOGGLE_PAUSE, null)
}
