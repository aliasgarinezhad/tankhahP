package com.domil.tankhahp.ui.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.domil.tankhahp.Items
import com.domil.tankhahp.R


@Composable
fun ErrorSnackBar(state: SnackbarHostState) {

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {

        SnackbarHost(hostState = state, snackbar = {
            Snackbar(
                shape = MaterialTheme.shapes.large,
                action = {
                    Text(
                        text = "باشه",
                        color = MaterialTheme.colors.secondary,
                        style = MaterialTheme.typography.h2,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable {
                                state.currentSnackbarData?.dismiss()
                            }
                    )
                }
            ) {
                Text(
                    text = state.currentSnackbarData?.message ?: "",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.h2,
                )
            }
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item(
    i: Int,
    uiList: MutableList<Items>,
    clickable: Boolean = false,
    text1: String,
    text2: String,
    colorFull: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {

    val topPadding = if (i == 0) 16.dp else 12.dp
    val bottomPadding = if (i == uiList.size - 1) 12.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = topPadding)
            .shadow(elevation = 5.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = if (colorFull) JeanswestSelected else MaterialTheme.colors.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onLongClick = { onLongClick() },
                onClick = { onClick() })
    ) {

        Row(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = text1,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.Start)
                )
                Text(
                    text = text2,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.Start)
                )
                Text(
                    text = "مرکز هزینه: " + uiList[i].payTo,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.Start)
                )
                Text(
                    text = "نوع فاکتور: " +
                            if (uiList[i].hasImageFile) "تصویری " else "کاغذی " + "با شماره فاکتور " + uiList[i].factorNumber,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.Start)
                )
                Text(
                    text = "شرح: " + uiList[i].specification,
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

@Composable
fun ProductCodeTextField(
    modifier: Modifier,
    onSearch: () -> Unit,
    hint: String,
    onValueChange: (it: String) -> Unit,
    value: String
) {

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        textStyle = MaterialTheme.typography.body2,

        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = ""
            )
        },
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        modifier = modifier
            .testTag("SearchProductCodeTextField")
            .background(
                color = MaterialTheme.colors.secondary,
                shape = MaterialTheme.shapes.small
            ),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            onSearch()
        }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = MaterialTheme.colors.secondary
        ),
        placeholder = { Text(text = hint) }
    )
}

@Composable
fun FilterDropDownList(
    modifier: Modifier,
    text: @Composable () -> Unit,
    values: MutableList<String>,
    onClick: (item: String) -> Unit
) {

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colors.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .border(
                BorderStroke(1.dp, if (expanded) Jeanswest else borderColor),
                shape = MaterialTheme.shapes.small
            )
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .testTag("FilterDropDownList")
                .fillMaxHeight(),
        ) {

            text()
            Icon(
                painter = painterResource(
                    id = if (expanded) {
                        R.drawable.ic_baseline_arrow_drop_up_24
                    } else {
                        R.drawable.ic_baseline_arrow_drop_down_24
                    }
                ),
                "",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 4.dp, end = 4.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentWidth()
                .background(color = BottomBar, shape = Shapes.small)
        ) {
            values.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onClick(it)
                }) {
                    Text(text = it)
                }
            }
        }
    }
}

