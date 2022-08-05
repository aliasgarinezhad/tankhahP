package com.domil.tankhahp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.domil.tankhahp.ui.theme.ErrorSnackBar
import com.domil.tankhahp.ui.theme.TankhahPTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File


class AddNewItemActivity : ComponentActivity() {

    private var date by mutableStateOf("")
    private var specification by mutableStateOf("")
    private var imgAddress by mutableStateOf("")
    private var payTo by mutableStateOf("")
    private var price by mutableStateOf("")
    private var state = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Page() }
    }

    private fun addToItems() {

        MainActivity.uiList.add(
            Items(
                date = date,
                specification = specification,
                payTo = payTo,
                price = price.toLong(),
                factorNumber = MainActivity.uiList.size + 1,
                imgAddress = imgAddress
            )
        )
        saveToMemory()
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

    @SuppressLint("QueryPermissionsNeeded")
    private fun createImageFile() {

        try {

            val dir = File(this.getExternalFilesDir(null), "/")
            val outputImageFile = File.createTempFile(
                "image${MainActivity.uiList.size + 1}",
                ".png",
                dir
            )

            val photoURI = FileProvider.getUriForFile(
                this,
                "com.domil.tankhahp.provider",
                outputImageFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, 1)

            imgAddress = outputImageFile.absolutePath

        } catch (e: Exception) {

            if (e is ActivityNotFoundException) {
                CoroutineScope(Main).launch {
                    state.showSnackbar(
                        "دسترسی به دوربین امکان پذیر نیست.",
                        null,
                        SnackbarDuration.Long
                    )
                }
            } else {
                CoroutineScope(Main).launch {
                    state.showSnackbar(
                        "مشکلی در ذخیره عکس پیش آمده است. لطفا دوباره امتحان کنید.",
                        null,
                        SnackbarDuration.Long
                    )
                }
            }
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
                    floatingActionButton = { OpenSnapp() },
                    floatingActionButtonPosition = FabPosition.Center,
                )
            }
        }
    }

    @Composable
    fun Content() {

        Column(modifier = Modifier.fillMaxSize()) {

            DateTextField()
            SpecificationField()
            PayToTextField()
            PriceTextField()

            Button(
                onClick = {
                    createImageFile()
                },
                modifier = Modifier
                    .padding(top = 20.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "اضافه کردن تصویر رسید")
            }

            Button(
                onClick = {
                    addToItems()
                    finish()
                },
                modifier = Modifier
                    .padding(top = 20.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "اضافه کردن تنخواه")
            }
        }
    }

    @Composable
    fun AppBar() {

        TopAppBar(

            title = {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "اضافه کردن تنخواه جدید", textAlign = TextAlign.Center,
                    )
                }
            },
        )
    }

    @Composable
    fun DateTextField() {

        OutlinedTextField(
            value = date, onValueChange = {
                date = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "تاریخ") }
        )
    }

    @Composable
    fun SpecificationField() {

        OutlinedTextField(
            value = specification, onValueChange = {
                specification = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "شرح هزینه (شامل مبدا، مقصد و دلیل سفر)") }
        )
    }

    @Composable
    fun PayToTextField() {

        OutlinedTextField(
            value = payTo, onValueChange = {
                payTo = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "مرکز هزینه (مانند اسنپ)") }
        )
    }

    @Composable
    fun PriceTextField() {

        OutlinedTextField(
            value = price, onValueChange = {
                price = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "مبلغ (ریال)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    @Composable
    fun OpenSnapp() {
        ExtendedFloatingActionButton(
            onClick = {
                Intent(this, GetFromSnappActivity::class.java).apply {
                    startActivity(this)
                }
            },
            text = { Text("ثبت تنخواه اسنپ") },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary
        )
    }
}