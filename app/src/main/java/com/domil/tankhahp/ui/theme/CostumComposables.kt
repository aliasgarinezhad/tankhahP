package com.domil.tankhahp.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.domil.tankhahp.Items

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
    text1 : String,
    text2 : String,
    colorFull : Boolean = false,
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
                color = if(colorFull) JeanswestSelected else MaterialTheme.colors.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("items")
            .combinedClickable(enabled = clickable, onLongClick = { onLongClick() }, onClick = { onClick() })
    ) {

        /*Box {

            Image(
                painter = rememberImagePainter(
                    uiList[i].imageUrl,
                ),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .shadow(0.dp, shape = Shapes.large)
                    .background(
                        color = MaterialTheme.colors.onPrimary,
                        shape = Shapes.large
                    )
                    .border(
                        BorderStroke(2.dp, color = BorderLight),
                        shape = Shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            /*if (uiList[i].requestedNum > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                ) {
                    Text(
                        text = uiList[i].requestedNum.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }*/
        }*/

        Row(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = text1,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = text2,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "مرکز هزینه: " + uiList[i].payTo,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "شرح: " + uiList[i].specification,
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Right,
                )
            }
        }
    }
}

@Composable
fun ProductCodeTextField(
    modifier: Modifier, onSearch : () -> Unit, hint : String, onValueChange : (it : String) -> Unit, value : String
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
