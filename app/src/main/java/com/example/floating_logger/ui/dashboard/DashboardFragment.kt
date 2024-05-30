package com.example.floating_logger.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.floating_logger.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        textView.setOnClickListener {

        }

        initWebView()

        return root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        with(binding.webView) {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

            // interface 추가
            addJavascriptInterface(WebAppInterface(), "Android")

            // html 콘텐츠 로드
            loadUrl("file:///android_asset/sample.html")
        }
        /**
         * webView Log 형태
         * chromium                com.example.floating_logger          I  [INFO:CONSOLE(9)] "Hello from WebView!", source: file:///android_asset/sample.html (9)
         */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class WebAppInterface {
        @JavascriptInterface
        fun logMessage(message: String) {
            Log.d("WebView", "[WEBVIEW] $message")
        }
    }
}