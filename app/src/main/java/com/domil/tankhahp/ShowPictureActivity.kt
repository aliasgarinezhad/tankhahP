package com.domil.tankhahp

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.domil.tankhahp.ui.theme.ErrorSnackBar
import com.domil.tankhahp.ui.theme.TankhahPTheme


class ShowPictureActivity : ComponentActivity() {

    private var state = SnackbarHostState()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { Page() }
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

        val img = BitmapFactory.decodeFile(intent.getStringExtra("imgAddress"))
        img?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "", modifier = Modifier.fillMaxSize())
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
                    text = "تصویر صورت حساب",
                    modifier = Modifier
                        .padding(end = 60.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Center,
                )
            }
        )
    }
}