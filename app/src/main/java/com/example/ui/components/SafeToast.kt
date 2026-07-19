package com.example.ui.components

import android.content.Context
import android.widget.Toast

private var lastToast: Toast? = null
private var lastToastTime: Long = 0L
private var lastToastMessage: String? = null

/**
 * Shows a safe, debounced, and queue-clearing Toast message.
 * Prevents multiple Toast messages from being queued in quick succession
 * which causes the Android NotificationService "already queued 5 toasts" error.
 */
fun Context.showSafeToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val currentTime = System.currentTimeMillis()
    
    // Debounce exact same message within 2000ms
    if (lastToastMessage == message && (currentTime - lastToastTime) < 2000) {
        return
    }
    
    // Debounce any message within 400ms to prevent rapid queue build up
    if ((currentTime - lastToastTime) < 400) {
        return
    }

    try {
        // Cancel the previously shown toast to immediately clear the queue before showing the new one
        lastToast?.cancel()
    } catch (e: Exception) {
        // Safe catch for any cancellation issues
    }

    try {
        val toast = Toast.makeText(this.applicationContext, message, duration)
        lastToast = toast
        lastToastTime = currentTime
        lastToastMessage = message
        toast.show()
    } catch (e: Exception) {
        // Fallback context in case of any issues with context mapping
    }
}
