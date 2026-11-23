package com.example.chikchik.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import com.example.chikchik.data.SecurityManager

class ChikChikAccessibilityService : AccessibilityService() {

    private lateinit var securityManager: SecurityManager
    private var lastVolumeUpTime: Long = 0
    private var lastVolumeDownTime: Long = 0
    private val DOUBLE_PRESS_INTERVAL = 400L
    private val MAX_SCROLLS = 5
    private val MAX_TEXT_LENGTH = 6000

    override fun onCreate() {
        super.onCreate()
        securityManager = SecurityManager(this)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!securityManager.isServiceEnabled()) {
            return super.onKeyEvent(event)
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (currentTime - lastVolumeUpTime < DOUBLE_PRESS_INTERVAL) {
                        lastVolumeUpTime = 0
                        triggerCapture(shouldScroll = false)
                        return true
                    }
                    lastVolumeUpTime = currentTime
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (currentTime - lastVolumeDownTime < DOUBLE_PRESS_INTERVAL) {
                        lastVolumeDownTime = 0
                        triggerCapture(shouldScroll = true)
                        return true
                    }
                    lastVolumeDownTime = currentTime
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun triggerCapture(shouldScroll: Boolean) {
        Log.d("ChikChik", "Triggered capture (scroll=$shouldScroll)")
        provideHapticFeedback()
        
        var fullText = captureScreenContent()
        
        if (shouldScroll) {
            var lastCapturedText = fullText
            var scrolls = 0

            while (scrolls < MAX_SCROLLS && fullText.length < MAX_TEXT_LENGTH) {
                if (!performScroll()) break
                
                // Wait for scroll to complete
                Thread.sleep(500) 
                
                val newCapture = captureScreenContent()
                if (newCapture == lastCapturedText) break // Reached bottom
                
                val uniqueNewText = getUniqueTrailingText(lastCapturedText, newCapture)
                fullText += uniqueNewText
                lastCapturedText = newCapture
                scrolls++
            }
        }
        
        val intent = Intent(this, ChikChikForegroundService::class.java).apply {
            action = ChikChikForegroundService.ACTION_PROCESS_TEXT
            putExtra(ChikChikForegroundService.EXTRA_TEXT, fullText)
        }
        startForegroundService(intent)
    }

    private fun getUniqueTrailingText(oldText: String, newText: String): String {
        // Simple overlap matching: find longest suffix of oldText that is a prefix of newText
        val maxOverlap = oldText.length.coerceAtMost(newText.length)
        for (i in maxOverlap downTo 1) {
            val suffix = oldText.takeLast(i)
            if (newText.startsWith(suffix)) {
                return newText.substring(i)
            }
        }
        return newText
    }

    private fun performScroll(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(rootNode) ?: return false
        
        return if (scrollableNode.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
            scrollableNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        } else {
            // Fallback to swipe gesture if ACTION_SCROLL_FORWARD is not supported
            dispatchSwipe()
        }
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollableNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun dispatchSwipe(): Boolean {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        
        val path = Path().apply {
            moveTo(width / 2f, height * 0.8f)
            lineTo(width / 2f, height * 0.2f)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
            
        return dispatchGesture(gesture, null, null)
    }

    private fun provideHapticFeedback() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun captureScreenContent(): String {
        val rootNode = rootInActiveWindow ?: return ""
        val sb = StringBuilder()
        walkNodeTree(rootNode, sb)
        return sb.toString()
    }

    private fun walkNodeTree(node: AccessibilityNodeInfo?, sb: StringBuilder) {
        if (node == null) return
        
        // Skip system UI elements like status bar and navigation bar
        val pkg = node.packageName?.toString() ?: ""
        if (pkg.contains("systemui") || pkg.contains("launcher")) return

        val text = node.text?.toString() ?: node.contentDescription?.toString()
        if (!text.isNullOrBlank()) {
            // Filter out system UI, status bar, and common photo/screenshot metadata
            val isTime = text.matches(Regex(".*\\d{1,2}:\\d{2}.*"))
            val isDate = text.matches(Regex(".*(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec).*\\d{4}.*"))
            val isMetadata = text.contains("Photo taken", ignoreCase = true) || 
                             text.contains("Screenshot", ignoreCase = true)
            
            if (!isTime && !isDate && !isMetadata) {
                sb.append(text).append("\n")
            }
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            walkNodeTree(child, sb)
            child?.recycle()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
