package com.domil.tankhahp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.domil.tankhahp.ui.theme.ErrorSnackBar
import com.domil.tankhahp.ui.theme.FilterDropDownList
import com.domil.tankhahp.ui.theme.TankhahPTheme
import com.domil.tankhahp.ui.theme.Typography
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class AddNewItemActivity : ComponentActivity() {

    private var date by mutableStateOf("")
    private var specification by mutableStateOf("")
    private var imgAddress by mutableStateOf("")
    private var payTo by mutableStateOf("")
    private var price by mutableStateOf("")
    private var state = SnackbarHostState()
    private var factorImageFilters =
        mutableStateListOf("کاغذ فاکتور را دارم", "تصویر فاکتور را دارم")
    private var factorImageFilter by mutableStateOf("کاغذ فاکتور را دارم")
    private var openSelectImageSourceDialog by mutableStateOf(false)
    private var openImageNotFoundDialog by mutableStateOf(false)
    private var savePressed by mutableStateOf(false)
    private var uiList = mutableStateListOf<Items>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun addToItems() {

        uiList.add(
            Items(
                date = date,
                specification = specification,
                payTo = payTo,
                price = price.toLong(),
                factorNumber = uiList.size + 1,
                imgAddress = imgAddress,
                hasImageFile = factorImageFilter == "تصویر فاکتور را دارم",
            )
        )
        saveToMemory()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 0) {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
            val dir = File(this.getExternalFilesDir(null), "/")
            val imgFile = File(dir, "image${uiList.size + 1}.png")
            val outputStream = FileOutputStream(imgFile.absolutePath)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            imgAddress = imgFile.absolutePath
        }
    }

    private fun getImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, 0)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takeImageByCamera() {

        try {

            val dir = File(this.getExternalFilesDir(null), "/")
            val outputImageFile = File.createTempFile(
                "image${uiList.size + 1}",
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
                )
            }
        }
    }

    @Composable
    fun Content() {

        Column(modifier = Modifier.fillMaxSize()) {

            if (openSelectImageSourceDialog) {
                SelectImageSourceAlertDialog()
            }

            if (openImageNotFoundDialog) {
                OpenImageNotFoundAlertDialog()
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterDropDownList(
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp),
                    text = {
                        Text(
                            style = MaterialTheme.typography.body2,
                            text = factorImageFilter,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp)
                        )
                    },
                    values = factorImageFilters,
                    onClick = {
                        factorImageFilter = it
                    })

                if (factorImageFilter == "تصویر فاکتور را دارم") {
                    Button(
                        onClick = {
                            openSelectImageSourceDialog = true
                        },
                        modifier = Modifier
                            .padding(top = 24.dp, start = 8.dp, end = 24.dp)
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text(text = "اضافه کردن تصویر")
                    }
                } else {
                    FactorTextField()
                }
            }

            DateTextField()
            SpecificationField()
            PayToTextField()
            PriceTextField()

            Button(
                onClick = {
                    savePressed = true
                    if (date == "" || specification == "" || payTo == "" || price.toLongOrNull() == null) {
                        return@Button
                    }
                    if(factorImageFilter == "تصویر فاکتور را دارم" && imgAddress == "") {
                        openImageNotFoundDialog = true
                        return@Button
                    }
                    addToItems()
                    finish()
                },
                modifier = Modifier
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                    .align(Alignment.CenterHorizontally)
                    .height(52.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "ثبت فاکتور", style = MaterialTheme.typography.h5)
            }
        }
    }

    @Composable
    fun AppBar() {

        TopAppBar(

            navigationIcon = {
                IconButton(onClick = { finish() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = ""
                    )
                }
            },

            title = {
                Text(
                    text = "ثبت فاکتور جدید",
                    modifier = Modifier
                        .padding(end = 70.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Center,
                )
            }
        )
    }

    @Composable
    fun DateTextField() {

        OutlinedTextField(
            value = date, onValueChange = {
                date = it
            },
            modifier = Modifier
                .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            label = { Text(text = "تاریخ") },
            isError = if (savePressed) date.isEmpty() else false,
            singleLine = true
        )
    }

    @Composable
    fun SpecificationField() {

        OutlinedTextField(
            value = specification, onValueChange = {
                specification = it
            },
            modifier = Modifier
                .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            label = { Text(text = "شرح هزینه (علت هزینه و جزئیات دیگر)") },
            isError = if (savePressed) specification.isEmpty() else false,
            singleLine = true
        )
    }

    @Composable
    fun PayToTextField() {

        OutlinedTextField(
            value = payTo, onValueChange = {
                payTo = it
            },
            modifier = Modifier
                .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            label = { Text(text = "محل هزینه (مانند هایپر)") },
            isError = if (savePressed) payTo.isEmpty() else false,
            singleLine = true
        )
    }

    @Composable
    fun PriceTextField() {

        OutlinedTextField(
            value = price, onValueChange = {
                price = it
            },
            modifier = Modifier
                .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            label = { Text(text = "مبلغ (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = if (savePressed) price.toLongOrNull() == null else false,
            singleLine = true
        )
    }

    @Composable
    fun FactorTextField() {

        OutlinedTextField(
            value = (uiList.size + 1).toString(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .padding(top = 16.dp, start = 8.dp, end = 24.dp)
                .fillMaxWidth(),
            label = { Text(text = "شماره (روی فاکتور بنویسید)") },
        )
    }

    @Composable
    fun SelectImageSourceAlertDialog() {

        AlertDialog(
            onDismissRequest = {
                openSelectImageSourceDialog = false
            },
            buttons = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        onClick = {
                            openSelectImageSourceDialog = false
                            takeImageByCamera()
                        }, modifier = Modifier
                            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                            .align(Alignment.CenterHorizontally)
                            .height(52.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "باز کردن دوربین", style = MaterialTheme.typography.h5)
                    }
                    Button(
                        onClick = {
                            openSelectImageSourceDialog = false
                            getImageFromGallery()
                        },
                        modifier = Modifier
                            .padding(bottom = 24.dp, start = 24.dp, end = 24.dp, top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                            .height(52.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "باز کردن گالری", style = MaterialTheme.typography.h5)
                    }
                }
            }
        )
    }
    @Composable
    fun OpenImageNotFoundAlertDialog() {
        AlertDialog(
            onDismissRequest = { openImageNotFoundDialog = false },
            buttons = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "لطفا تصویر فاکتور را اضافه کنید.",
                        modifier = Modifier.padding(top = 24.dp, bottom = 0.dp, end = 24.dp, start = 24.dp),
                        style = Typography.h5
                    )
                    Button(
                        onClick = {
                            openImageNotFoundDialog = false
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