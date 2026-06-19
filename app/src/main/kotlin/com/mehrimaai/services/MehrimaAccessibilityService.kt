package com.mehrimaai.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MehrimaAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Event logging
    }

    override fun onInterrupt() {
        // Handle service interruption
    }

    fun clickOnTextOrButton(targetText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        return findAndClickNode(rootNode, targetText)
    }

    private fun findAndClickNode(node: AccessibilityNodeInfo?, text: String): Boolean {
        if (node == null) return false

        if (node.text?.toString()?.contains(text, ignoreCase = true) == true && node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return true
        }

        for (i in 0 until (node.childCount ?: 0)) {
            val child = node.getChild(i) ?: continue
            if (findAndClickNode(child, text)) {
                child.recycle()
                return true
            }
        }

        node.recycle()
        return false
    }

    fun typeTextInField(targetElementText: String, textToType: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        return findAndTypeNode(rootNode, targetElementText, textToType)
    }

    private fun findAndTypeNode(node: AccessibilityNodeInfo?, fieldHint: String, text: String): Boolean {
        if (node == null) return false

        if ((node.text?.toString()?.contains(fieldHint, ignoreCase = true) == true ||
             node.hint?.toString()?.contains(fieldHint, ignoreCase = true) == true) &&
            node.isEditable) {
            node.text = text
            return true
        }

        for (i in 0 until (node.childCount ?: 0)) {
            val child = node.getChild(i) ?: continue
            if (findAndTypeNode(child, fieldHint, text)) {
                child.recycle()
                return true
            }
        }

        node.recycle()
        return false
    }

    fun openAppByPackageName(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        }
    }
}
