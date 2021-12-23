package com.ub.utils.ui.main

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ub.utils.*
import moxy.MvpAppCompatActivity
import moxy.MvpView
import moxy.ktx.moxyPresenter
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import java.util.*

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainView : MvpView {
    fun done()
    fun isEquals(equals: Boolean)
    fun showPush(content: Pair<String, String>)
    fun showImage(image: Bitmap)
    fun onConnectivityChange(state: String)
}

class MainActivity : MvpAppCompatActivity(), MainView {

    private val presenter: MainPresenter by moxyPresenter {
        MainPresenter(images[random.nextInt(images.size)])
    }

    private val images: Array<String> by lazy {
        arrayOf(
            "https://tagline.ru/file/company/logo/unitbean-logo_tagline.png",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Kotlin_logo_2021.svg/2880px-Kotlin_logo_2021.svg.png"
        )
    }
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.load()

        presenter.loadImage()

        presenter.networkTest(this)
    }

    override fun done() {
        AlertDialog.Builder(this)
            .setMessage(spannableBuilder {
                append("20sp text with underline and strikethrough")
                partialSpan("20sp text") {
                    size(20F)
                }
                partialSpan("underline") {
                    underline()
                }
                partialSpan("strikethrough") {
                    strikethrough()
                }
                appendLn("Bold text blue yeti")
                partialSpan("Bold text") {
                    typeface(Typeface.DEFAULT_BOLD)
                }
                partialSpan("blue yeti") {
                    color(android.R.color.holo_blue_light)
                    size(10F)
                }
                appendLn("Partial clickable clickable span")
                partialSpan("clickable", searchFromIndex = 80) {
                    click(
                        isNeedUnderline = true
                    ) {
                        Toast.makeText(this@MainActivity, "Click on span", Toast.LENGTH_LONG).show()
                    }
                }
            })
            .setPositiveButton(android.R.string.ok) { _, _ ->
                presenter.isEquals()
            }
            .show()
    }

    override fun isEquals(equals: Boolean) {
        AlertDialog.Builder(this)
            .setMessage("${UbUtils.getString(R.string.app_name)}. Equals $equals")
            .show()
    }

    override fun showPush(content: Pair<String, String>) {
        UbNotify
            .create(this, android.R.drawable.ic_dialog_alert, content.first, content.second)
            .setChannelParams(content.first, content.second, null)
            .setParams {
                setAutoCancel(true)
                setStyle(NotificationCompat.BigTextStyle().bigText(content.second))
            }
            .show(id = random.nextInt())
    }

    override fun showImage(image: Bitmap) {
        findViewById<ImageView>(R.id.iv_image).setImageBitmap(image)
    }

    override fun onConnectivityChange(state: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = when (state) {
                CNetwork.NetworkState.ACTIVE -> ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryDark)
                CNetwork.NetworkState.DISABLE -> Color.RED
                CNetwork.NetworkState.CAPTIVE -> Color.GREEN
                else -> Color.YELLOW
            }
        }
        supportActionBar?.setBackgroundDrawable(
            GradientDrawable().apply {
                setColor(when (state) {
                    CNetwork.NetworkState.ACTIVE -> ContextCompat.getColor(this@MainActivity, R.color.colorPrimary)
                    CNetwork.NetworkState.DISABLE -> Color.RED
                    CNetwork.NetworkState.CAPTIVE -> Color.GREEN
                    else -> Color.YELLOW
                })
            }
        )
    }

    fun showPush(v : View) {
        presenter.generatePushContent()
    }

    fun hideTest(v : View) {
        val tvText = findViewById<TextView>(R.id.tv_text)
        val btnTextAction = findViewById<Button>(R.id.btn_text_action)
        if (tvText.visibility == View.GONE) {
            tvText.visible
            btnTextAction.text = "HIDE TEXT"
        } else {
            tvText.gone
            btnTextAction.text = "SHOW TEXT"
        }
    }
}
