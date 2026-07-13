package science.credo.mobiledetector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.anko.doAsync
import science.credo.mobiledetector.database.DataManager
import science.credo.mobiledetector.database.DetectionStateWrapper
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LogsActivity : AppCompatActivity() {
    companion object {
        const val TAG = "LogsActivity"
        const val REQ_WRITE = 1234
    }

    private lateinit var textView: TextView
    private lateinit var btnSave: Button
    private lateinit var btnShare: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_logs)

            // Setup action bar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.title_logs)

            textView = findViewById(R.id.logs_text)
            btnSave = findViewById(R.id.btn_save)
            btnShare = findViewById(R.id.btn_share)

            loadLogs()

            btnSave.setOnClickListener {
                if (needsWritePermission()) {
                    requestWritePermission()
                } else {
                    saveToDownloads()
                }
            }

            btnShare.setOnClickListener {
                shareLogs()
            }
        } catch (e: Exception) {
            Log.e(TAG, "LogsActivity onCreate failed", e)
            Toast.makeText(this, getString(R.string.error_prefix) + " " + (e.message ?: ""), Toast.LENGTH_LONG).show()
            // prevent app restart loop by finishing gracefully
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun needsWritePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android Q+ can write via MediaStore, but we'll request no permission and try fallback to share
            false
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestWritePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQ_WRITE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_WRITE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToDownloads()
            } else {
                Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadLogs() {
        doAsync {
            val sb = StringBuilder()
            sb.append("=== Credo Detector - Logs snapshot ${Date()} ===\n\n")

            // crash log from internal file if exists
            try {
                val fin = openFileInput("crash_log.txt")
                val crash = fin.bufferedReader().use { it.readText() }
                sb.append("--- Crash log (last) ---\n")
                sb.append(crash)
                sb.append("\n\n")
            } catch (e: Exception) {
                // ignore
            }

            // detection state wrappers
            try {
                val total = DetectionStateWrapper.getTotal(this@LogsActivity)
                val session = DetectionStateWrapper.getLatestSession(this@LogsActivity)
                sb.append("--- DetectionState (total) ---\n")
                sb.append("startDetectionTimestamp=${Date(total.startDetectionTimestamp)}\n")
                sb.append("lastHitTimestamp=${Date(total.lastHitTimestamp)}\n")
                sb.append("performedFrames=${total.performedFrames}\n")
                sb.append("averageStats=${total.averageStats}\n")
                sb.append("maxStats=${total.maxStats}\n")
                sb.append("blacksStats=${total.blacksStats}\n\n")

                sb.append("--- DetectionState (session) ---\n")
                sb.append("startDetectionTimestamp=${Date(session.startDetectionTimestamp)}\n")
                sb.append("lastHitTimestamp=${Date(session.lastHitTimestamp)}\n")
                sb.append("performedFrames=${session.performedFrames}\n")
                sb.append("averageStats=${session.averageStats}\n")
                sb.append("maxStats=${session.maxStats}\n")
                sb.append("blacksStats=${session.blacksStats}\n\n")
            } catch (e: Exception) {
                Log.w(TAG, "loadLogs: detection state read failed", e)
            }

            // last N hits from DB
            try {
                val dm = DataManager.getDefault(this@LogsActivity)
                val hits = dm.getHits()
                sb.append("--- Stored hits (count=${hits.size}) ---\n")
                val limit = 20
                val start = if (hits.size > limit) hits.size - limit else 0
                for (i in start until hits.size) {
                    val h = hits[i]
                    val ts = if (h.detectionTimestamp != 0) Date(h.detectionTimestamp.toLong() * 10000L) else Date(0)
                    sb.append("id=${h.id}, ts=${ts}, center=(${h.mX},${h.mY}), max=${h.mMaxValue}, toSent=${h.toSent}\n")
                }
                sb.append("\n")
            } catch (e: Exception) {
                Log.w(TAG, "loadLogs: getHits failed", e)
            }

            val txt = sb.toString()
            runOnUiThread {
                textView.text = txt
            }
        }
    }

    private fun saveToDownloads() {
        val content = textView.text.toString()
        if (content.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_logs), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "credo_logs_$ts.txt"

            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloads.exists()) downloads.mkdirs()
            val outFile = File(downloads, fileName)
            FileOutputStream(outFile).use { fos ->
                fos.write(content.toByteArray())
            }

            Toast.makeText(this, getString(R.string.saved_to_downloads, outFile.absolutePath), Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e(TAG, "saveToDownloads failed", e)
            Toast.makeText(this, getString(R.string.error_save_failed), Toast.LENGTH_LONG).show()
        }
    }

    private fun shareLogs() {
        val content = textView.text.toString()
        if (content.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_logs), Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "CredoDetector logs")
            intent.putExtra(Intent.EXTRA_TEXT, content)
            startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
        } catch (e: Exception) {
            Log.e(TAG, "shareLogs failed", e)
            Toast.makeText(this, getString(R.string.error_share_failed), Toast.LENGTH_LONG).show()
        }
    }
}
