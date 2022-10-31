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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import com.domil.tankhahp.ui.theme.ErrorSnackBar
import com.domil.tankhahp.ui.theme.TankhahPTheme
import com.domil.tankhahp.ui.theme.Typography
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private var isScanningBill by mutableStateOf(false)
    private var openHelpDialog by mutableStateOf(true)
    private var savePressed by mutableStateOf(false)
    private var date = ""
    private var specification = ""
    private var price = 0L
    private val url = "https://app.snapp.taxi/ride-history"
    private var imgAddress = ""
    private var from = ""
    private var to = ""
    private var uiList = mutableStateListOf<Items>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = object : WebViewClient() {
                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    CoroutineScope(IO).launch {
                        if (url.toString().contains("https://snapp.taxi/receipt/")) {
                            scanBill(url.toString())
                        }
                    }
                    super.doUpdateVisitedHistory(view, url, isReload)
                }
            }
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            loadUrl(this@GetFromSnappActivity.url)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        }

        setContent { Page() }

        loadMemory()
    }

    private fun loadMemory() {

        val type = object : TypeToken<SnapshotStateList<Items>>() {}.type

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        uiList = Gson().fromJson(
            memory.getString("Items", ""),
            type
        ) ?: mutableStateListOf()
    }

    private fun scanBill(url: String) {

        isScanningBill = true

        try {
            val doc = Jsoup.connect(url).get()
            //get from and to
            val fromToHtml = doc.getElementsByClass("col-md-9 col-xs-7").html()

            if (fromToHtml.isEmpty()) {
                CoroutineScope(Dispatchers.Default).launch {
                    state.showSnackbar(
                        "مشکلی در دریافت رسید پیش آمده است. لطفا بعد از مدتی دوباره امتحان کنید.",
                        null,
                        SnackbarDuration.Long
                    )
                }
                isScanningBill = false
                return
            }

            val fromStartString = "مبدا:</span><span>"
            val fromIndexStart = fromToHtml.indexOf(fromStartString, 0)
            val fromIndexEnd =
                fromToHtml.indexOf("</span>", fromIndexStart + fromStartString.length)
            from = fromToHtml.substring(fromIndexStart + fromStartString.length, fromIndexEnd)

            while (from.count {
                    it == '،'
                } > 1) {
                from = from.substring(from.indexOfFirst {
                    it == '،'
                } + 2)
            }

            val toStartString = "مقصد:</span><span>"
            val toIndexStart = fromToHtml.indexOf(toStartString, fromIndexEnd)
            val toIndexEnd = fromToHtml.indexOf("</span>", toIndexStart + toStartString.length)
            to = fromToHtml.substring(toIndexStart + toStartString.length, toIndexEnd)

            while (to.count {
                    it == '،'
                } > 1) {
                to = to.substring(to.indexOfFirst {
                    it == '،'
                } + 2)
            }
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
            val date =
                datePriceHtml.substring(dateIndexStart + dateStartString.length, dateIndexEnd)

            price = convertPersianNumbersToEnglish(price)

            val image =
                Html2Bitmap.Builder(this@GetFromSnappActivity, WebViewContent.html(doc.html()))
                    .build().bitmap
            val dir = File(this.getExternalFilesDir(null), "/")
            val imgFile = File(dir, "image${uiList.size + 1}.png")
            val outputStream = FileOutputStream(imgFile.absolutePath)
            image?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            this.imgAddress = imgFile.absolutePath
            this.specification = "سفر از " + from + " به " + to
            this.date = date
            this.price = price.toLong() / 10

            isScanningBill = false
            openGoalDialog = true
        } catch (e: Exception) {
            isScanningBill = false
            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "مشکلی در دریافت رسید پیش آمده است. لطفا بعد از مدتی دوباره امتحان کنید.",
                    null,
                    SnackbarDuration.Long
                )
            }
        }
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "Items",
            Gson().toJson(uiList).toString()
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
        if (webView.url == "https://app.snapp.taxi/ride-history" || webView.url?.indexOf("https://app.snapp.taxi/login") != -1) {
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
            if (isScanningBill) {
                ShowLoadingAlertDialog()
            }
            if (openHelpDialog && uiList.isEmpty()) {
                ShowHelpAlertDialog()
            }
            AndroidView(
                factory = {
                    webView
                })
        }
    }

    @Composable
    fun FileAlertDialog() {

        AlertDialog(

            buttons = {

                Column(modifier = Modifier.wrapContentHeight()) {

                    OutlinedTextField(
                        value = goal, onValueChange = {
                            goal = it
                        },
                        label = { Text(text = "دلیل سفر") },
                        modifier = Modifier
                            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                            .align(Alignment.CenterHorizontally),
                        isError = if (savePressed) goal.isEmpty() else false,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = from, onValueChange = {
                            from = it
                        },
                        label = { Text(text = "مبدا") },
                        modifier = Modifier
                            .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                            .align(Alignment.CenterHorizontally),
                        isError = if (savePressed) from.isEmpty() else false,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = to, onValueChange = {
                            to = it
                        },
                        label = { Text(text = "مقصد") },
                        modifier = Modifier
                            .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                            .align(Alignment.CenterHorizontally),
                        isError = if (savePressed) to.isEmpty() else false,
                        singleLine = true
                    )

                    Button(modifier = Modifier
                        .padding(bottom = 24.dp, top = 24.dp, end = 24.dp, start = 24.dp)
                        .align(Alignment.CenterHorizontally)
                        .height(52.dp)
                        .fillMaxWidth(),
                        onClick = {
                            savePressed = true
                            if (goal == "" || from == "" || to == "") {
                                return@Button
                            }
                            specification = "سفر از " + from + " به " + to
                            openGoalDialog = false
                            uiList.add(
                                Items(
                                    specification = specification + " بابت " + goal,
                                    date = date,
                                    price = price,
                                    payTo = "اسنپ",
                                    factorNumber = uiList.size + 1,
                                    imgAddress = imgAddress,
                                    hasImageFile = true,
                                )
                            )
                            saveToMemory()
                            webView.goBack()
                            webView.goBack()
                        }) {
                        Text(text = "ذخیره", style = Typography.h5)
                    }
                }
            },

            onDismissRequest = {
                openGoalDialog = false
            }
        )
    }

    @Composable
    fun ShowLoadingAlertDialog() {
        AlertDialog(
            onDismissRequest = { },
            buttons = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(top = 24.dp),
                        strokeWidth = 6.dp
                    )

                    Text(
                        text = "در حال اسکن رسید سفر",
                        modifier = Modifier.padding(top = 48.dp, bottom = 24.dp),
                        style = Typography.h5
                    )
                }
            },
        )
    }

    @Composable
    fun ShowHelpAlertDialog() {
        AlertDialog(
            onDismissRequest = { openHelpDialog = false },
            buttons = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "ابتدا سفر مورد نظر خود را انتخاب کنید. سپس دکمه (رسید سفر) در پایین صفحه را لمس کنید.",
                        modifier = Modifier.padding(
                            top = 24.dp,
                            bottom = 0.dp,
                            end = 24.dp,
                            start = 24.dp
                        ),
                        style = Typography.h5
                    )
                    Button(
                        onClick = {
                            openHelpDialog = false
                        },
                        modifier = Modifier
                            .padding(bottom = 24.dp, top = 24.dp, end = 24.dp, start = 24.dp)
                            .align(Alignment.CenterHorizontally)
                            .height(52.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "متوجه شدم", style = MaterialTheme.typography.h5)
                    }
                }
            },
        )
    }
}