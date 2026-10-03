package com.henryshop.affiliatevideo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.henryshop.affiliatevideo.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null
    private var videoUri: Uri? = null

    private val pickVideo = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        videoUri = uri
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
        }
        showVideo(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSelect.setOnClickListener { pickVideo.launch(arrayOf("video/*")) }
        binding.btnCopy.setOnClickListener { copyCaption() }
        binding.btnShare.setOnClickListener { shareToTikTok() }
    }

    private fun showVideo(uri: Uri) {
        binding.emptyState.visibility = android.view.View.GONE
        binding.playerView.visibility = android.view.View.VISIBLE
        if (player == null) {
            player = ExoPlayer.Builder(this).build().also { binding.playerView.player = it }
        }
        player?.setMediaItem(MediaItem.fromUri(uri))
        player?.prepare()
        player?.playWhenReady = true
        binding.statusText.text = "Video ready. Copy caption, then share sa TikTok."
    }

    private fun fullCaption(): String {
        val caption = binding.captionInput.text.toString().trim()
        val tags = binding.hashtagInput.text.toString().trim()
        return listOf(caption, tags).filter { it.isNotEmpty() }.joinToString("\n\n")
    }

    private fun copyCaption() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("caption", fullCaption()))
        Toast.makeText(this, "Caption copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareToTikTok() {
        val uri = videoUri ?: run {
            Toast.makeText(this, "Pumili muna ng video", Toast.LENGTH_SHORT).show()
            return
        }
        copyCaption()
        val shareUri = copyToCache(uri)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_TEXT, fullCaption())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(contentResolver, "video", shareUri)
        }
        val tiktok = listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
            .firstOrNull { packageManager.getLaunchIntentForPackage(it) != null }
        if (tiktok != null) {
            send.setPackage(tiktok)
            startActivity(send)
        } else {
            startActivity(Intent.createChooser(send, "Share video"))
        }
    }

    private fun copyToCache(source: Uri): Uri {
        val dir = File(cacheDir, "shared").apply { mkdirs() }
        val out = File(dir, "affiliate_${System.currentTimeMillis()}.mp4")
        contentResolver.openInputStream(source).use { input ->
            out.outputStream().use { output -> input?.copyTo(output) }
        }
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", out)
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        binding.playerView.player = null
        player?.release()
        player = null
        super.onDestroy()
    }
}
