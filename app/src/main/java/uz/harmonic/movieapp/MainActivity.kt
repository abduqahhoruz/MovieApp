package uz.harmonic.movieapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.findFragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper
import uz.harmonic.movieapp.common.Constants
import uz.harmonic.movieapp.databinding.ActivityMainBinding
import uz.harmonic.movieapp.home.notification.NotificationItem
import uz.harmonic.movieapp.home.notification.NotificationUtils


private const val REQ_CODE_DM = 1001

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var navController: NavController
    val notificationHelper: FileDownloadNotificationHelper<NotificationItem> =
        FileDownloadNotificationHelper<NotificationItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpNavigation()
        checkUpPermission()
        FileDownloader.setupOnApplicationOnCreate(application)
    }

    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun checkUpPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_CODE_DM
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQ_CODE_DM -> {
                if (grantResults[0] != PackageManager.PERMISSION_DENIED) {
                    checkUpPermission()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationHelper.clear()
        NotificationUtils.deleteNotificationChannel(Constants.CHANNEL_ID, applicationContext)
    }
}