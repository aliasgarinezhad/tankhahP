package com.domil.tankhahp

import android.content.Intent
import android.os.Bundle
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
import androidx.preference.PreferenceManager
import com.domil.tankhahp.ui.theme.ErrorSnackBar
import com.domil.tankhahp.ui.theme.TankhahPTheme

class UserDataActivity : ComponentActivity() {

    private var fullName by mutableStateOf("")
    private var globalCardNumber by mutableStateOf("")
    private var cardNumber by mutableStateOf("")
    private var state = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Page() }
    }

    private fun saveMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = memory.edit()
        editor.putString("fullName", fullName)
        editor.putString("globalCardNumber", "IR$globalCardNumber")
        editor.putString("cardNumber", cardNumber)
        editor.apply()
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

            FullNameTextField()
            CardNumberField()
            GlobalCardNumberField()

            Button(
                onClick = {
                    saveMemory()
                    val intent =
                        Intent(this@UserDataActivity, MainActivity::class.java)
                    intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                },
                modifier = Modifier
                    .padding(top = 20.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "ثبت مشخصات")
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
                        "ثبت مشخصات", textAlign = TextAlign.Center,
                    )
                }
            },
        )
    }

    @Composable
    fun FullNameTextField() {

        OutlinedTextField(
            value = fullName, onValueChange = {
                fullName = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "نام و نام خانوادگی خود را وارد کنید") }
        )
    }

    @Composable
    fun CardNumberField() {

        OutlinedTextField(
            value = cardNumber, onValueChange = {
                cardNumber = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "شماره کارت خود را وارد کنید") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    @Composable
    fun GlobalCardNumberField() {

        OutlinedTextField(
            value = globalCardNumber, onValueChange = {
                globalCardNumber = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "شماره شبا خود را بدون IR وارد کنید") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

}