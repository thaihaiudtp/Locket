package com.example.locket.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimeUtils {

    fun getTimeAgo(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "Just now"

        // Định dạng ngày tháng từ Server (Thường là ISO 8601)
        // Nếu server bạn trả về format khác, hãy sửa dòng này.
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val past = format.parse(dateString) ?: return "Just now"
            val now = Date()
            val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
            val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
            val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)

            return when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                days < 7 -> "${days}d ago"
                else -> {
                    // Nếu quá 7 ngày thì hiện ngày tháng cụ thể
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateFormat.format(past)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Just now"
        }
    }
}