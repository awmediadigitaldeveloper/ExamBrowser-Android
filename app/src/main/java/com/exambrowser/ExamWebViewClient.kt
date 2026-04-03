package com.exambrowser

import android.annotation.TargetApi
import android.content.Context
import android.net.http.SslError
import android.os.Build
import android.webkit.*

class ExamWebViewClient(
    private val context: Context,
    private val allowedUrls: List<String>,
    private val allowAll: Boolean = false,
    private val onBlockedUrl: (String) -> Unit = {}
) : WebViewClient() {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return true
        return blockIfNotAllowed(url)
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        url ?: return true
        return blockIfNotAllowed(url)
    }

    private fun blockIfNotAllowed(url: String): Boolean {
        // Allow data: and about: URLs (used internally by WebView)
        if (url.startsWith("data:") || url.startsWith("about:") || url.startsWith("blob:")) {
            return false
        }
        if (allowAll || allowedUrls.isEmpty()) return false
        return if (isUrlAllowed(url)) {
            false // Let WebView handle it
        } else {
            onBlockedUrl(url)
            true // Block navigation
        }
    }

    private fun isUrlAllowed(url: String): Boolean {
        return allowedUrls.any { allowed ->
            allowed.isNotEmpty() && url.contains(allowed.trim(), ignoreCase = true)
        }
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        // Cancel SSL errors to maintain security
        handler?.cancel()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        // Inject JavaScript to:
        // 1. Disable right-click context menu
        // 2. Disable text selection
        // 3. Disable copy/cut/paste
        // 4. Disable print screen hotkeys
        val js = """
            (function() {
                // Disable context menu
                document.addEventListener('contextmenu', function(e) {
                    e.preventDefault();
                    return false;
                }, false);

                // Disable text selection
                document.addEventListener('selectstart', function(e) {
                    e.preventDefault();
                    return false;
                }, false);

                // Disable copy
                document.addEventListener('copy', function(e) {
                    e.preventDefault();
                    return false;
                }, false);

                // Disable cut
                document.addEventListener('cut', function(e) {
                    e.preventDefault();
                    return false;
                }, false);

                // Disable paste
                document.addEventListener('paste', function(e) {
                    e.preventDefault();
                    return false;
                }, false);

                // Disable keyboard shortcuts (Ctrl+C, Ctrl+V, Ctrl+A, etc.)
                document.addEventListener('keydown', function(e) {
                    if (e.ctrlKey || e.metaKey) {
                        var key = e.key.toLowerCase();
                        if (['c', 'v', 'x', 'a', 'p', 'u', 's'].indexOf(key) !== -1) {
                            e.preventDefault();
                            return false;
                        }
                    }
                    // Disable F12 developer tools
                    if (e.key === 'F12') {
                        e.preventDefault();
                        return false;
                    }
                }, false);

                // Apply CSS to disable text selection
                var style = document.createElement('style');
                style.innerHTML = '* { -webkit-user-select: none !important; user-select: none !important; -webkit-touch-callout: none !important; }';
                document.head.appendChild(style);
            })();
        """.trimIndent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view?.evaluateJavascript(js, null)
        } else {
            @Suppress("DEPRECATION")
            view?.loadUrl("javascript:$js")
        }
    }
}
