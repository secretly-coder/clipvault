package com.clipvault.app

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class ClipVaultAccessibilityService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private var lastCaptured: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        clipboardManager.addPrimaryClipChangedListener {
            try {
                val clipData = clipboardManager.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val rawText = clipData.getItemAt(0).text?.toString() ?: ""
                    if (rawText.isNotBlank() && rawText != lastCaptured) {
                        lastCaptured = rawText
                        processAndStoreClip(rawText)
                    }
                }
            } catch (e: Exception) {
                Log.e("ClipVault", "Clipboard read error: ${e.message}")
            }
        }
    }

    private fun processAndStoreClip(text: String) {
        val (cleanedText, wasCleaned) = UrlCleaner.clean(text)
        
        if (wasCleaned) {
            // Overwrite system clipboard with the sanitized URL
            val clip = android.content.ClipData.newPlainText("Cleaned URL", cleanedText)
            clipboardManager.setPrimaryClip(clip)
            lastCaptured = cleanedText
        }

        // Load existing items, append new clip, and save securely via EncryptedSharedPreferences
        val existingData = CryptoHelper.loadVaultData(applicationContext)
        // Note: In production, parse JSON array, prepend new item, and re-save.
        val updatedData = "[$cleanedText] $existingData" 
        CryptoHelper.saveVaultData(applicationContext, updatedData)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not required for clipboard sniffing, but mandatory to override
    }

    override fun onInterrupt() {
        Log.w("ClipVault", "Accessibility Service Interrupted")
    }
}
