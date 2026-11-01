package com.madstudy.app.notifications

import android.app.*
import android.content.*
import android.os.Build
import com.madstudy.app.MainActivity
import com.madstudy.app.data.AppState

object StudyNotifications {
    private fun alarm(context: Context, action: String, id: Int, isBreak: Boolean = false): PendingIntent = PendingIntent.getBroadcast(
        context, id, Intent(context, StudyReminderReceiver::class.java).setAction(action).putExtra("isBreak", isBreak),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun timer(context: Context, deadline: Long, isBreak: Boolean = false) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, deadline, alarm(context, "timer", 100, isBreak))
    }
    fun cancelTimer(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(alarm(context, "timer", 100))
    }
    fun scheduleReminders(context: Context, state: AppState) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = alarm(context, "tasks", 101)
        manager.cancel(pending)
        if (!state.settings.remind || state.tasks.none { !it.done }) return
        val next = java.time.LocalDate.now().atTime(18, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            if (next > System.currentTimeMillis()) next else next + 86400000, pending)
    }
}

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("study", "Belajar & fokus", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val isBreak = intent.getBooleanExtra("isBreak", false)
        val title = when {
            isBreak -> "Istirahat selesai"
            intent.action == "timer" -> "Waktu sesi berakhir"
            else -> "Waktunya cek tugas"
        }
        val body = when {
            isBreak -> "Siap mulai sesi fokus berikutnya?"
            intent.action == "timer" -> "Buka MadStudy untuk checkout atau melanjutkan belajar."
            else -> "Periksa tugas dan rencana belajarmu hari ini."
        }
        val notification = Notification.Builder(context, "study")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(open).setAutoCancel(true).build()
        runCatching { manager.notify(if (intent.action == "timer") 100 else 101, notification) }
    }
}
