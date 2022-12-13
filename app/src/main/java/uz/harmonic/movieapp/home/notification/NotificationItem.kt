package uz.harmonic.movieapp.home.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import uz.harmonic.movieapp.MainActivity
import uz.harmonic.movieapp.MyApp
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.notification.BaseNotificationItem
import com.liulishuo.filedownloader.util.FileDownloadHelper
import uz.harmonic.movieapp.MyApp.Companion.appContext
import uz.harmonic.movieapp.R

const val REQ_CODE_PEN_INTENT = 0

class NotificationItem(id: Int, title: String, desc: String, channelId: String) :
    BaseNotificationItem(id, title, desc) {
    private var builder: NotificationCompat.Builder? = null

    init {
        val intents = arrayOfNulls<Intent>(1)
        intents[0] =
            Intent.makeMainActivity(ComponentName(appContext, MainActivity::class.java))
        val pendingIntent = PendingIntent.getActivities(
            appContext,
            REQ_CODE_PEN_INTENT,
            intents,
            PendingIntent.FLAG_MUTABLE
        )

        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(
                FileDownloadHelper.getAppContext(),
                channelId
            )
        } else {
            NotificationCompat.Builder(FileDownloadHelper.getAppContext())
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationCompat.PRIORITY_MIN)

        }
        builder!!.setContentTitle(getTitle())
            .setContentText(desc)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
    }

    override fun show(statusChanged: Boolean, status: Int, isShowProgress: Boolean) {
        var desc = ""
        when (status) {
            FileDownloadStatus.pending.toInt() -> {
                desc += " pending"
                builder!!.setProgress(total, sofar, true)
            }
            FileDownloadStatus.started.toInt() -> {
                desc += " started"
                builder!!.setProgress(total, sofar, true)
            }
            FileDownloadStatus.progress.toInt() -> {
                desc += " progress"
                builder!!.setProgress(total, sofar, total <= 0)
            }
            FileDownloadStatus.retry.toInt() -> {
                desc += " retry"
                builder!!.setProgress(total, sofar, true)
            }
            FileDownloadStatus.error.toInt() -> {
                desc += " error"
                builder!!.setProgress(total, sofar, false)
            }
            FileDownloadStatus.paused.toInt() -> {
                desc += " paused"
                builder!!.setProgress(total, sofar, false)
            }
            FileDownloadStatus.completed.toInt() -> {
                desc += " completed"
                builder!!.setProgress(total, sofar, false)
            }
            FileDownloadStatus.warn.toInt() -> {
                desc += " warn"
                builder!!.setProgress(0, 0, true)
            }
            else -> {
            }
        }
        builder!!.setContentTitle(title).setContentText(desc)
        manager.notify(id, builder!!.build())
    }

}
