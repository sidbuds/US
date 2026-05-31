package com.love.interaction.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {
    private val beijingZone = ZoneId.of("Asia/Shanghai")
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** 将 PocketBase 的 UTC 时间转为北京时间显示 */
    fun formatCreatedAt(utcTime: String): String {
        if (utcTime.isBlank()) return ""
        return try {
            // PocketBase 返回 "2026-05-31 06:50:38.367Z"（空格分隔），需要转为 ISO 格式
            val isoTime = utcTime.replace(" ", "T")
            val instant = Instant.parse(isoTime)
            instant.atZone(beijingZone).format(displayFormatter)
        } catch (_: Exception) {
            utcTime.take(16).replace("T", " ")
        }
    }

    /** 只显示日期部分 */
    fun formatDate(utcTime: String): String {
        if (utcTime.isBlank()) return ""
        return try {
            val isoTime = utcTime.replace(" ", "T")
            val instant = Instant.parse(isoTime)
            instant.atZone(beijingZone).format(dateFormatter)
        } catch (_: Exception) {
            utcTime.take(10)
        }
    }

    /** 获取北京时间今天的日期字符串 */
    fun todayBeijing(): String {
        return java.time.LocalDate.now(beijingZone).toString()
    }

    /** 判断是否是今天（北京时间） */
    fun isToday(utcTime: String): Boolean {
        if (utcTime.isBlank()) return false
        return try {
            val isoTime = utcTime.replace(" ", "T")
            val instant = Instant.parse(isoTime)
            val beijingDate = instant.atZone(beijingZone).toLocalDate()
            beijingDate == java.time.LocalDate.now(beijingZone)
        } catch (_: Exception) {
            utcTime.take(10) == todayBeijing()
        }
    }
}