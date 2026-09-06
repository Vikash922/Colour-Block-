package com.example.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.io.File

data class UpdateInfo(val versionCode: Int, val versionName: String, val apkUrl: String, val releaseNotes: String)

@Composable
fun AppUpdater(updateJsonUrl: String) {
    val context = LocalContext.current
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateJsonUrl) {
        withContext(Dispatchers.IO) {
            try {
                // Background fetch
                val jsonString = URL(updateJsonUrl).readText()
                val json = JSONObject(jsonString)
                val serverVersionCode = json.getInt("versionCode")
                
                // Get local version code
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = packageInfo.versionCode
                
                if (serverVersionCode > currentVersionCode) {
                    updateInfo = UpdateInfo(
                        versionCode = serverVersionCode,
                        versionName = json.getString("versionName"),
                        apkUrl = json.getString("apkUrl"),
                        releaseNotes = json.getString("releaseNotes")
                    )
                    showDialog = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showDialog && updateInfo != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Update Available") },
            text = { Text("Version ${updateInfo!!.versionName} is available!\n\n${updateInfo!!.releaseNotes}") },
            confirmButton = {
                TextButton(onClick = {
                    downloadAndInstall(context, updateInfo!!.apkUrl, "Update_${updateInfo!!.versionName}.apk")
                    showDialog = false
                }) {
                    Text("Update Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Later") }
            }
        )
    }
}

private fun downloadAndInstall(context: Context, apkUrl: String, fileName: String) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val uri = Uri.parse(apkUrl)
    val request = DownloadManager.Request(uri).apply {
        setTitle("Downloading Update")
        setDescription("Downloading latest version of the app...")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        setMimeType("application/vnd.android.package-archive")
    }

    val downloadId = downloadManager.enqueue(request)

    val onComplete = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                val apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                if (apkFile.exists()) {
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(
                            FileProvider.getUriForFile(ctxt, "${ctxt.packageName}.provider", apkFile),
                            "application/vnd.android.package-archive"
                        )
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    ctxt.startActivity(installIntent)
                }
                ctxt.unregisterReceiver(this)
            }
        }
    }
    ContextCompat.registerReceiver(
        context,
        onComplete,
        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        ContextCompat.RECEIVER_EXPORTED
    )
}
