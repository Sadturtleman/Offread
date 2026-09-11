package com.android.offread.translate.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.android.offread.translate.domain.model.VisibleText
import org.json.JSONArray
import org.json.JSONObject

/**
 * 원문 페이지를 그대로 띄우는 웹뷰(#58).
 *
 * 본문만 뽑아 다시 그리는 대신 페이지를 그대로 두고 텍스트 노드만 번역문으로 바꾼다 —
 * 이미지·링크·레이아웃이 살아 있어 원서를 읽는 느낌이 유지된다.
 *
 * 자바스크립트를 켜야 하고([collect.js] 를 주입해야 하므로) 브리지도 페이지에 노출된다.
 * 브리지가 받는 것은 "번역할 텍스트 목록" 하나뿐이라 페이지가 할 수 있는 최악은
 * 번역 요청을 많이 보내는 정도다.
 */
@Composable
internal fun TranslateWebView(
    url: String,
    onPageLoad: () -> Unit,
    onCollectTexts: (List<VisibleText>) -> Unit,
    onWebViewReady: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val script = remember { context.readCollectScript() }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            WebView(viewContext).apply {
                configure(script, onPageLoad, onCollectTexts)
                onWebViewReady(this)
            }
        },
        update = { webView -> if (webView.url != url) webView.loadUrl(url) },
    )
}

/** 번역문을 페이지 제자리에 채운다. 노드 참조는 페이지 쪽 스크립트가 들고 있다. */
internal fun WebView.applyTranslation(
    id: String,
    text: String,
) {
    evaluateJavascript("window.__offread && window.__offread.apply(${JSONObject.quote(id)}, ${JSONObject.quote(text)});", null)
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configure(
    script: String,
    onPageLoad: () -> Unit,
    onCollectTexts: (List<VisibleText>) -> Unit,
) {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    // 원본 레이아웃을 그대로 보되 화면 폭에 맞춘다.
    settings.useWideViewPort = true
    settings.loadWithOverviewMode = true
    addJavascriptInterface(OffreadBridge(onCollectTexts), OffreadBridge.NAME)
    webViewClient =
        object : WebViewClient() {
            override fun onPageFinished(
                view: WebView,
                url: String,
            ) {
                onPageLoad()
                // 페이지가 다 그려진 뒤에 긁어야 사이트 스크립트가 채운 본문까지 잡힌다.
                view.evaluateJavascript(script, null)
            }
        }
}

private fun Context.readCollectScript(): String = assets.open(SCRIPT_PATH).bufferedReader().use { it.readText() }

/**
 * 페이지 → 앱 방향 통로. 페이지 쪽 스크립트가 모은 텍스트를 넘겨받는다.
 *
 * 웹뷰 스레드에서 불리므로 넘겨받은 콜백은 메인 스레드 가정을 하지 않는다.
 */
private class OffreadBridge(
    private val onCollectTexts: (List<VisibleText>) -> Unit,
) {
    @JavascriptInterface
    fun onTextsCollected(json: String) {
        val array = runCatching { JSONArray(json) }.getOrNull() ?: return
        val texts =
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val text = item.optString("text").trim()
                if (text.isEmpty()) null else VisibleText(id = item.optString("id"), text = text)
            }
        onCollectTexts(texts)
    }

    companion object {
        /** 페이지 스크립트가 부르는 이름. collect.js 와 맞춰야 한다. */
        const val NAME = "OffreadBridge"
    }
}

private const val SCRIPT_PATH = "offread/collect.js"
