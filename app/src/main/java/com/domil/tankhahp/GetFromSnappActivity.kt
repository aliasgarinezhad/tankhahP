package com.domil.tankhahp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import com.domil.tankhahp.ui.theme.ErrorSnackBar
import com.domil.tankhahp.ui.theme.TankhahPTheme
import com.google.gson.Gson
import com.izettle.html2bitmap.Html2Bitmap
import com.izettle.html2bitmap.content.WebViewContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream


class GetFromSnappActivity : ComponentActivity() {

    private var state = SnackbarHostState()
    private lateinit var webView: WebView
    private var goal by mutableStateOf("")
    private var openGoalDialog by mutableStateOf(false)
    private var date = ""
    private var specification = ""
    private var price = 0L
    private val url = "https://app.snapp.taxi/ride-history"
    private var imgAddress = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            loadUrl(this@GetFromSnappActivity.url)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        }

        setContent { Page() }
        CoroutineScope(Dispatchers.Default).launch {
            state.showSnackbar(
                "لطفا ابتدا به صفحه مشخصات سفر بروید و روی دکمه (رسید سفر) در پایین صفحه کلیک کنید. در صفحه جدید، روی اسکن رسید سفر کلیک کنید.",
                null,
                SnackbarDuration.Long
            )
        }
    }

    private fun scanBill(url: String) {

        val doc = Jsoup.connect(url).get()
        //get from and to
        val fromToHtml = doc.getElementsByClass("col-md-9 col-xs-7").html()

        if (fromToHtml.isEmpty()) {
            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "لطفا ابتدا وارد صفحه (رسید سفر) شوید.",
                    null,
                    SnackbarDuration.Long
                )
            }
            return
        }

        val fromStartString = "مبدا:</span><span>"
        val fromIndexStart = fromToHtml.indexOf(fromStartString, 0)
        val fromIndexEnd = fromToHtml.indexOf("</span>", fromIndexStart + fromStartString.length)
        val from = fromToHtml.substring(fromIndexStart + fromStartString.length, fromIndexEnd)

        val toStartString = "مقصد:</span><span>"
        val toIndexStart = fromToHtml.indexOf(toStartString, fromIndexEnd)
        val toIndexEnd = fromToHtml.indexOf("</span>", toIndexStart + toStartString.length)
        val to = fromToHtml.substring(toIndexStart + toStartString.length, toIndexEnd)

        //get date and price
        val datePriceHtml = doc.getElementsByClass("table table-striped").text().toString()

        val priceStartString = "مبلغ پرداختی:"
        val priceIndexStart = datePriceHtml.indexOf(priceStartString, 0)
        val priceIndexEnd =
            datePriceHtml.indexOf("تاریخ سفر", priceIndexStart + priceStartString.length)
        var price =
            datePriceHtml.substring(priceIndexStart + priceStartString.length, priceIndexEnd)

        val dateStartString = "تاریخ سفر:"
        val dateIndexStart = datePriceHtml.indexOf(dateStartString, 0)
        val dateIndexEnd =
            datePriceHtml.indexOf("زمان سفر", dateIndexStart + dateStartString.length)
        val date = datePriceHtml.substring(dateIndexStart + dateStartString.length, dateIndexEnd)

        price = convertPersianNumbersToEnglish(price)

        val image = Html2Bitmap.Builder(this@GetFromSnappActivity, WebViewContent.html(doc.html())).build().bitmap
        val dir = File(this.getExternalFilesDir(null), "/")
        val imgFile = File(dir, "image${MainActivity.uiList.size + 1}.png")
        val outputStream = FileOutputStream(imgFile.absolutePath)
        image?.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        this.imgAddress = imgFile.absolutePath
        this.specification = "سفر از " + from + " به " + to
        this.date = date
        this.price = price.toLong()


        openGoalDialog = true
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "Items",
            Gson().toJson(MainActivity.uiList).toString()
        )
        edit.apply()
    }

    private fun convertPersianNumbersToEnglish(input: String): String {
        var inputCopy = input
        inputCopy = inputCopy.replace("۰", "0")
        inputCopy = inputCopy.replace("۱", "1")
        inputCopy = inputCopy.replace("۲", "2")
        inputCopy = inputCopy.replace("۳", "3")
        inputCopy = inputCopy.replace("۴", "4")
        inputCopy = inputCopy.replace("۵", "5")
        inputCopy = inputCopy.replace("۶", "6")
        inputCopy = inputCopy.replace("۷", "7")
        inputCopy = inputCopy.replace("۸", "8")
        inputCopy = inputCopy.replace("۹", "9")
        inputCopy = inputCopy.replace("٫", ".")
        inputCopy = inputCopy.replace("-", "-")
        inputCopy = inputCopy.replace(",", "")
        inputCopy = inputCopy.replace("﷼", "")
        inputCopy = inputCopy.replace(" ", "")
        Log.e(
            "convert", inputCopy
        )
        return inputCopy
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == 4) {
            back()
        }
        return true
    }

    private fun back() {
        if (webView.url == "https://app.snapp.taxi/ride-history") {
            finish()
        } else {
            webView.goBack()
        }
    }

    @Composable
    fun Page() {
        TankhahPTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = { AppBar() },
                    content = { Content() },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun Content() {

        Column {

            if (openGoalDialog) {
                FileAlertDialog()
            }
            AndroidView(
                factory = {
                    webView
                })
        }
    }

    @Composable
    fun AppBar() {

        TopAppBar(

            navigationIcon = {
                IconButton(onClick = { back() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = ""
                    )
                }
            },

            actions = {
                IconButton(onClick = {
                    val url = webView.url.toString()
                    CoroutineScope(IO).launch {
                        scanBill(url)
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_barcode_scan),
                        contentDescription = ""
                    )
                }
            },

            title = {
                Text(
                    text = "اطلاعات سفر",
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Center,
                )
            }
        )
    }

    @Composable
    fun FileAlertDialog() {

        AlertDialog(

            buttons = {

                Column {

                    Text(
                        text = "لطفا دلیل سفر را بنویسید", modifier = Modifier
                            .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                    )

                    OutlinedTextField(
                        value = goal, onValueChange = {
                            goal = it
                        },
                        modifier = Modifier
                            .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Button(modifier = Modifier
                        .padding(bottom = 10.dp, top = 10.dp, start = 10.dp, end = 10.dp)
                        .align(Alignment.CenterHorizontally),
                        onClick = {
                            openGoalDialog = false
                            MainActivity.uiList.add(
                                Items(
                                    specification = specification + " بابت " + goal,
                                    date = date,
                                    price = price,
                                    payTo = "اسنپ",
                                    factorNumber = MainActivity.uiList.size + 1,
                                    imgAddress = imgAddress
                                )
                            )
                            saveToMemory()
                            webView.goBack()
                            webView.goBack()
                        }) {
                        Text(text = "ذخیره")
                    }
                }
            },

            onDismissRequest = {
                openGoalDialog = false
            }
        )
    }
}