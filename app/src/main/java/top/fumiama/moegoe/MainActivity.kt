package top.fumiama.moegoe

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.endrnd.view.*
import kotlinx.android.synthetic.main.startrnd.view.*
import top.fumiama.moegoe.tools.DownloadTools
import java.io.File
import java.net.URLEncoder


class MainActivity : Activity() {
    private var firstItem = true
    private var speaker = 0
    private var isjp = true
    private var iscleanonly = false
    private var isdling = false
    private var name = ""
    private val spkApiUrl get() = getString(if(isjp) R.string.jpspkapi else R.string.krspkapi)
    private val clnSpkApiUrl get() = getString(if(isjp) R.string.jpspkclnapi else R.string.krspkclnapi)
    private val clnApiUrl get() = getString(if(isjp) R.string.jpclnapi else R.string.krclnapi)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListenerToRootView()
        val jpspeakers = resources.getStringArray(R.array.jpspks)
        val krspeakers = resources.getStringArray(R.array.krspks)
        yaru.setOnClickListener {
            if(inptxt.text.isNotEmpty()){
                val iscleaned = inptxt.text.startsWith("[已清理] ")
                if(iscleaned && inptxt.text.length == "[已清理] ".length) {
                    Toast.makeText(this, "无效输入", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val chat = layoutInflater.inflate(R.layout.startrnd, ln, false)
                val rchat = layoutInflater.inflate(R.layout.endrnd, ln, false)
                if(iscleanonly) {
                    val cleantask = DownloadTools.getHttpContentTask(String.format(clnApiUrl, URLEncoder.encode(inptxt.text.toString(), "utf-8")))
                    chat.tl.text = "清理中..."
                    Thread{
                        val t = cleantask.get()?.let {
                            "[已清理] ${it.decodeToString()}"
                        }?:"清理失败"
                        runOnUiThread {
                            chat.tl.text = t
                        }
                    }.start()
                }
                else {
                    val speaktask = DownloadTools.getHttpContentTask(
                        if(iscleaned) String.format(clnSpkApiUrl, URLEncoder.encode(inptxt.text.toString().substringAfter("[已清理] "), "utf-8"), speaker)
                        else String.format(spkApiUrl, URLEncoder.encode(inptxt.text.toString(), "utf-8"), speaker)
                    )
                    chat.tl.text = "\uD83C\uDFA4 by ${if (isjp) jpspeakers[speaker] else krspeakers[speaker]}"
                    val adviceName = "${inptxt.text}-${if (isjp) jpspeakers[speaker] else krspeakers[speaker]}.ogg"
                    chat.tl.setTextIsSelectable(false)
                    var hasdata = false
                    val mp = MediaPlayer()
                    chat.tl.setOnClickListener {
                        if(!hasdata) {
                            Toast.makeText(this, "计算中...", Toast.LENGTH_SHORT).show()
                            chat.pbl.visibility = View.VISIBLE
                        }
                        Thread {
                            try {
                                if(hasdata) {
                                    runOnUiThread {
                                        ObjectAnimator.ofInt(chat.pbl, "progress", 0, 100).setDuration(
                                            mp.duration.toLong()
                                        ).start()
                                    }
                                    mp.seekTo(0)
                                    mp.start()
                                } else if(!isdling) {
                                    isdling = true
                                    speaktask.get()?.let {
                                        val n = System.currentTimeMillis().toString()
                                        (externalCacheDir?:cacheDir)?.apply {
                                            if(!canWrite()) {
                                                setWritable(true)
                                            }
                                            val f = File(this, n)
                                            f.writeBytes(it)
                                            mp.setDataSource(f.absolutePath)
                                            mp.prepare()
                                            mp.start()
                                            runOnUiThread {
                                                ObjectAnimator.ofInt(chat.pbl, "progress", 0, 100).setDuration(
                                                    mp.duration.toLong()
                                                ).start()
                                            }
                                            hasdata = true
                                            chat.tl.setOnLongClickListener {
                                                createFile(n, adviceName)
                                                true
                                            }
                                        }
                                    }
                                    isdling = false
                                }
                            } catch (e: Exception) {
                                runOnUiThread{
                                    Toast.makeText(this, "错误: $e", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.start()
                    }
                }
                rchat.tr.text = inptxt.text
                inptxt.text.clear()
                inptxt.text.clear()
                if(ln.childCount > 0) firstItem = false
                ln.addView(
                    rchat, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                ln.addView(TextView(this))
                ln.addView(
                    chat, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                ln.addView(TextView(this))
                setColor(chat.cl)
                setColor(rchat.cr)
                scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN); inptxt.requestFocus(); }
                if(!firstItem && scroll.translationY == 0f) scroll.upOverScroll()
            }
        }
        yaru.setOnLongClickListener {
            callLanguageModeList()
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) when (requestCode) {
            CREATE_FILE_FEEDBACK -> data?.let {
                if(name != "") {
                    it.data?.let { d ->
                        save2Uri(File((externalCacheDir ?: cacheDir), name), d)
                    }
                }
            }
        }
    }

    private fun setColor(v: CardView){
        when((0..5).random()){
            0 -> v.setCardBackgroundColor(resources.getColor(R.color.colora, theme))
            1 -> v.setCardBackgroundColor(resources.getColor(R.color.colorb, theme))
            2 -> v.setCardBackgroundColor(resources.getColor(R.color.colorc, theme))
            3 -> v.setCardBackgroundColor(resources.getColor(R.color.colord, theme))
            4 -> v.setCardBackgroundColor(resources.getColor(R.color.colore, theme))
            5 -> v.setCardBackgroundColor(resources.getColor(R.color.colorf, theme))
        }
    }

    private fun setListenerToRootView() {
        val rootView: View = window.decorView.rootView
        val softKeyboardHeight = 100
        var heightDiff = 0
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val dm: DisplayMetrics = rootView.resources.displayMetrics
            val hd: Int = rootView.bottom - r.bottom
            if(hd != heightDiff){
                heightDiff = hd
                val isKeyboardShown = heightDiff > softKeyboardHeight * dm.density
                if (isKeyboardShown) {
                    //Toast.makeText(this, "弹出$heightDiff", Toast.LENGTH_SHORT).show()
                    if(!firstItem || scroll.getChildAt(0).height > rootView.height) scroll.upOverScroll()
                    inptxt.requestFocus()
                } else {
                    //Toast.makeText(this, "收回$heightDiff", Toast.LENGTH_SHORT).show()
                    if(!firstItem || scroll.getChildAt(0).height > rootView.height) scroll.downToNormal()
                }
            }
            if(scroll.maxOverScrollY == 0) scroll.maxOverScrollY = yaru.height / 64 * 150
        }
    }

    private fun save2Uri(f: File, uri: Uri) = Thread{
        Log.d("MyMain", "save $f to $uri")
        contentResolver.openOutputStream(uri)?.let {
            val fi = f.inputStream()
            fi.copyTo(it)
            fi.close()
            it.close()
        }
        runOnUiThread {
            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
        }
    }.start()

    private fun createFile(fileName: String, adviceName: String, type: String = "audio/ogg") = Thread{
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type
        intent.putExtra(Intent.EXTRA_TITLE, adviceName)
        name = fileName
        startActivityForResult(intent, CREATE_FILE_FEEDBACK)
    }.start()

    private fun callLanguageModeList(){
        val lst = arrayOf("日语", "韩语", "日语(仅清理)", "韩语(仅清理)")
        AlertDialog.Builder(this)
            .setTitle("选择语言")
            .setIcon(R.mipmap.ic_launcher)
            .setSingleChoiceItems(ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, lst), when {
                isjp && !iscleanonly -> 0
                !isjp && !iscleanonly -> 1
                isjp && iscleanonly -> 2
                !isjp && iscleanonly -> 3
                else -> 0
            }){ d, p ->
                isjp = p == 0 || p == 2
                iscleanonly = p >= 2
                d.cancel()
                if(!iscleanonly) {
                    val list = resources.getStringArray(if (isjp) R.array.jpspks else R.array.krspks)
                    if(!isjp && speaker >= 6) speaker = 5
                    AlertDialog.Builder(this)
                        .setTitle("选择对象")
                        .setIcon(R.mipmap.ic_launcher)
                        .setSingleChoiceItems(ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, list), speaker){ d, p ->
                            speaker = p
                            if (isjp) im.setImageResource(when(p) {
                                0 -> R.drawable.jp0
                                1 -> R.drawable.jp1
                                2 -> R.drawable.jp2
                                3 -> R.drawable.jp3
                                4 -> R.drawable.jp4
                                5 -> R.drawable.jp5
                                6 -> R.drawable.jp6
                                else -> R.drawable.jp0
                            }) else im.setImageResource(when(p) {
                                0 -> R.drawable.kr0
                                1 -> R.drawable.kr1
                                2 -> R.drawable.kr2
                                3 -> R.drawable.kr3
                                4 -> R.drawable.kr4
                                5 -> R.drawable.kr5
                                else -> R.drawable.kr0
                            })
                            d.cancel()
                        }.show()
                }
            }.show()
    }

    companion object {
        const val CREATE_FILE_FEEDBACK = 2
    }
}
