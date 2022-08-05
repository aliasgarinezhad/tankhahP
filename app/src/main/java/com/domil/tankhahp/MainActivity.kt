package com.domil.tankhahp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.domil.tankhahp.JalaliDate.JalaliDate
import com.domil.tankhahp.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {

    private var sumOfPrices by mutableStateOf(0L)
    private var fullName by mutableStateOf("")
    private var globalCardNumber by mutableStateOf("")
    private var cardNumber by mutableStateOf("")
    private var state = SnackbarHostState()
    private var fileName by mutableStateOf("تنخواه تاریخ ")
    private var openFileDialog by mutableStateOf(false)

    companion object {
        var uiList = mutableStateListOf<Items>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Page()
        }

        val util = JalaliDate()
        fileName += util.currentShamsidate

        loadMemory()
        checkPermission()
    }

    private fun checkPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                0
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setContent {
            Page()
        }

        if (fullName == "") {
            val intent =
                Intent(this@MainActivity, UserDataActivity::class.java)
            intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        sumOfPrices = 0
        uiList.forEach {
            sumOfPrices += it.price
        }
    }

    private fun loadMemory() {

        val type = object : TypeToken<SnapshotStateList<Items>>() {}.type

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        fullName = memory.getString("fullName", "") ?: ""
        globalCardNumber = memory.getString("globalCardNumber", "") ?: ""
        cardNumber = memory.getString("cardNumber", "") ?: ""

        uiList = Gson().fromJson(
            memory.getString("Items", ""),
            type
        ) ?: mutableStateListOf()
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

    private fun exportImages(excelUri: Uri) {

        var images = mutableListOf<Bitmap>()
        val outputImages = mutableListOf<Bitmap>()
        val outputFiles = mutableListOf<File>()
        val uris = ArrayList<Uri>()

        uiList.forEach {
            if (it.imgAddress != "") {
                images.add(
                    writeFactorNumber(
                        BitmapFactory.decodeFile(it.imgAddress),
                        it.factorNumber
                    )
                )
            }
        }

        for (i in 0 until images.size / 4) {
            outputImages.add(
                combineFourBitmaps(
                    images[0 + (4 * i)],
                    images[1 + (4 * i)],
                    images[2 + (4 * i)],
                    images[3 + (4 * i)]
                )!!
            )
        }

        if (images.size % 4 == 1) {
            outputImages.add(images[images.size - 1])
        } else if (images.size % 4 == 2) {
            outputImages.add(
                combineFourBitmaps(
                    images[images.size - 2],
                    images[images.size - 1],
                    null,
                    null
                )!!
            )
        } else if (images.size % 4 == 3) {
            outputImages.add(
                combineFourBitmaps(
                    images[images.size - 3],
                    images[images.size - 2],
                    images[images.size - 1],
                    null
                )!!
            )
        }

        val dir = File(getExternalFilesDir(null), "/")

        for (i in 0 until outputImages.size) {
            val imageFile = File(dir, "outputFile$i.png")
            val outputStream = FileOutputStream(imageFile.absolutePath)
            outputImages[i].compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputFiles.add(imageFile)
        }

        outputFiles.forEach {
            uris.add(
                FileProvider.getUriForFile(
                    this,
                    this.applicationContext.packageName + ".provider",
                    it
                )
            )
        }

        uris.add(excelUri)

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        shareIntent.type = "application/octet-stream"
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(shareIntent)
    }

    private fun writeFactorNumber(originalBitmap: Bitmap, factorNumber: Int): Bitmap {

        val mutableBitmap: Bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)
        val paint = Paint()
        paint.color = Color.BLACK // Text Color
        paint.textSize = 18f // Text Size
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // Text Overlapping Pattern

        canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
        canvas.drawText(factorNumber.toString(), 50f, 70f, paint)
        return mutableBitmap
    }

    private fun combineFourBitmaps(
        topLeft: Bitmap,
        topRight: Bitmap,
        bottomLeft: Bitmap?,
        bottomRight: Bitmap?
    ): Bitmap? {

        val width = topLeft.width * 2
        val height = topLeft.height * 2

        val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        canvas.drawBitmap(topLeft, 0f, 0f, null)
        canvas.drawBitmap(topRight, topLeft.width.toFloat(), 0f, null)
        if (bottomLeft != null) {
            canvas.drawBitmap(bottomLeft, 0f, topLeft.height.toFloat(), null)
            if (bottomRight != null) {
                canvas.drawBitmap(
                    bottomRight,
                    topLeft.width.toFloat(),
                    topRight.height.toFloat(),
                    null
                )
            }
        }
        return combined
    }

    private fun exportFile() {

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("تنخواه")

        sheet.isRightToLeft = true

        sheet.createRow(sheet.physicalNumberOfRows)
        val nameRow = sheet.createRow(sheet.physicalNumberOfRows)
        createCell(nameRow, 1, "نام و نام خانوادگی: ")
        createCell(nameRow, 2, fullName)

        val cardNumberRow = sheet.createRow(sheet.physicalNumberOfRows)
        createCell(cardNumberRow, 1, "شماره کارت: ")
        createCell(cardNumberRow, 2, cardNumber)

        val globalCardNumberRow = sheet.createRow(sheet.physicalNumberOfRows)
        createCell(globalCardNumberRow, 1, "شماره شبا: ")
        createCell(globalCardNumberRow, 2, globalCardNumber)

        sheet.createRow(sheet.physicalNumberOfRows)

        val headerRow = sheet.createRow(sheet.physicalNumberOfRows)
        headerRow.createCell(0)
        createCell(headerRow, 1, "تاریخ")
        createCell(headerRow, 2, "شرح")
        createCell(headerRow, 3, "مرکز هزینه")
        createCell(headerRow, 4, "فاکتور")
        createCell(headerRow, 5, "مبلغ (ریال)")

        uiList.forEach {
            val row = sheet.createRow(sheet.physicalNumberOfRows)
            headerRow.createCell(0)
            createCell(row, 1, it.date)
            createCell(row, 2, it.specification)
            createCell(row, 3, it.payTo)
            createCell(row, 4, it.factorNumber.toDouble())
            createCell(row, 5, it.price.toDouble())
        }

        val sumOfPriceRow = sheet.createRow(sheet.physicalNumberOfRows)
        sumOfPriceRow.createCell(4).setCellValue("جمع: ")
        sumOfPriceRow.createCell(5).setCellValue(sumOfPrices.toDouble())

        sheet.setColumnWidth(1, 5000)
        sheet.setColumnWidth(2, 10000)
        sheet.setColumnWidth(3, 5000)
        sheet.setColumnWidth(4, 5000)
        sheet.setColumnWidth(5, 5000)

        for (i in 1 until 4) {
            for (j in 1 until 3) {
                setRegionBorder(CellRangeAddress(i, i, j, j), sheet)
            }
        }

        for (i in 5 until sheet.physicalNumberOfRows - 1) {
            for (j in 1 until 6) {
                setRegionBorder(CellRangeAddress(i, i, j, j), sheet)
            }
        }

        for (j in 4 until 6) {
            setRegionBorder(
                CellRangeAddress(
                    sheet.physicalNumberOfRows - 1,
                    sheet.physicalNumberOfRows - 1,
                    j,
                    j
                ), sheet
            )
        }

        val dir = File(this.getExternalFilesDir(null), "/")

        val outFile = File(dir, "$fileName.xlsx")

        val outputStream = FileOutputStream(outFile.absolutePath)
        workbook.write(outputStream)
        outputStream.flush()
        outputStream.close()

        val uri = FileProvider.getUriForFile(
            this,
            this.applicationContext.packageName + ".provider",
            outFile
        )
        exportImages(uri)
    }

    private fun createCell(row: Row, columnIndex: Int, value: String) {

        val cell = row.createCell(columnIndex)
        val cellStyle = cell.cellStyle
        cellStyle.alignment = CellStyle.ALIGN_CENTER
        cell.cellStyle = cellStyle
        cell.setCellValue(value)
    }

    private fun createCell(row: Row, columnIndex: Int, value: Double) {

        val cell = row.createCell(columnIndex)
        val cellStyle = cell.cellStyle
        cellStyle.alignment = CellStyle.ALIGN_CENTER
        cell.cellStyle = cellStyle
        cell.setCellValue(value)
    }

    private fun setRegionBorder(region: CellRangeAddress, sheet: Sheet) {
        val wb = sheet.workbook
        RegionUtil.setBorderBottom(CellStyle.BORDER_THIN.toInt(), region, sheet, wb)
        RegionUtil.setBorderLeft(CellStyle.BORDER_THIN.toInt(), region, sheet, wb)
        RegionUtil.setBorderRight(CellStyle.BORDER_THIN.toInt(), region, sheet, wb)
        RegionUtil.setBorderTop(CellStyle.BORDER_THIN.toInt(), region, sheet, wb)
    }

    private fun clear(items: Items) {
        uiList.remove(items)
        sumOfPrices = 0
        uiList.forEach {
            sumOfPrices += it.price
        }
        saveToMemory()
    }

    @Composable
    fun Page() {
        TankhahPTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = { AppBar() },
                    content = { Content() },
                    bottomBar = { BottomAppBar() },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @Composable
    fun BottomAppBar() {
        BottomAppBar(backgroundColor = JeanswestBottomBar) {

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "مجموع هزینه ها: $sumOfPrices",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )

                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(this@MainActivity, AddNewItemActivity::class.java)
                            startActivity(intent)
                        },
                    ) {
                        Text(text = "اضافه کردن تنخواه جدید")
                    }
                }
            }
        }
    }

    @Composable
    fun AppBar() {

        TopAppBar(

            actions = {
                IconButton(onClick = { openFileDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_share_24),
                        contentDescription = ""
                    )
                }
            },

            title = {
                Text(
                    text = "لیست تنخواه ها",
                    modifier = Modifier
                        .padding(start = 35.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Center,
                )
            }
        )
    }

    @Composable
    fun Content() {

        Column {

            if (openFileDialog) {
                FileAlertDialog()
            }

            LazyColumn(modifier = Modifier.padding(top = 8.dp, bottom = 56.dp)) {

                items(uiList.size) { i ->
                    LazyColumnItem(i)
                }
            }
        }
    }

    @Composable
    fun LazyColumnItem(i: Int) {

        val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

        Box {

            Item(
                i, uiList,
                text1 = "تاریخ: " + uiList[i].date,
                text2 = "مبلغ (ریال): " + uiList[i].price.toString(),
                clickable = true
            ) {
                Intent(this@MainActivity, ShowPictureActivity::class.java).apply {
                    putExtra("imgAddress", uiList[i].imgAddress)
                    startActivity(this)
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = topPaddingClearButton, end = 8.dp)
                    .background(
                        shape = RoundedCornerShape(36.dp),
                        color = deleteCircleColor
                    )
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .clickable {
                        clear(uiList[i])
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_clear_24),
                    contentDescription = "",
                    tint = deleteColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp)
                )
            }
        }
    }

    @Composable
    fun FileAlertDialog() {

        AlertDialog(

            buttons = {

                Column {

                    Text(
                        text = "نام فایل خروجی را وارد کنید", modifier = Modifier
                            .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                    )

                    OutlinedTextField(
                        value = fileName, onValueChange = {
                            fileName = it
                        },
                        modifier = Modifier
                            .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Button(modifier = Modifier
                        .padding(bottom = 10.dp, top = 10.dp, start = 10.dp, end = 10.dp)
                        .align(Alignment.CenterHorizontally),
                        onClick = {
                            openFileDialog = false
                            exportFile()
                        }) {
                        Text(text = "ذخیره")
                    }
                }
            },

            onDismissRequest = {
                openFileDialog = false
            }
        )
    }
}