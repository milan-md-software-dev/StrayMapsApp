package com.example.straymaps.ui.screens.sign_in

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.straymaps.R
import com.example.straymaps.ui.screens.welcome.listOfBrushGradientColors
import com.example.straymaps.ui.theme.dancingScriptFontFamily

//Screen where users can sign in to the app if they already have an account
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreenWithTopAppBar(
    onBackClick: () -> Unit,
    openAndPopUp: (String, String) -> Unit,
    skipSignIn: (String) -> Unit,
    viewModel: StrayMapsSignInScreenViewModel = hiltViewModel()
){

    //Lets user skip signing in if they already already signed in
    LaunchedEffect(Unit){
        viewModel.initialize(skipSignIn)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = Color.Black
                ),
                title = {
                    Text (
                        text = stringResource(id = R.string.sign_in),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {onBackClick()}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.arrow_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) {innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        SignInScreen(
            openAndPopUp,
            viewModel
        )
        Row(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_medium))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        )
            {
                Text(
                    text = stringResource(id = R.string.make_an_account),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer (modifier = Modifier.width(2.dp))
                Text (
                    text = stringResource(id = R.string.make_an_account_sign_up),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable { viewModel.onSignUpClick(openAndPopUp) }
                )
            }
        }
    }
}


//Composable function with elements related to signing in
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: StrayMapsSignInScreenViewModel = hiltViewModel()
    ){

    val email = viewModel.email.collectAsState()
    val password = viewModel.password.collectAsState()
    var isPasswordVisible by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = TextStyle(
                fontSize = 64.sp,
                fontFamily = dancingScriptFontFamily,
                fontWeight = FontWeight.Medium,
                brush = Brush.linearGradient(
                    colors = listOfBrushGradientColors
                ),
            ),
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_extra_large)))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email -> viewModel.getEmail(email) },
            label = {
                Text(stringResource(id = R.string.email))
            }
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password -> viewModel.getPassword(password) },
            label = {
                Text(stringResource(id = R.string.password))
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val (icon, iconColor) = if (isPasswordVisible) {
                    Pair (
                        Icons.Filled.Visibility,
                        colorResource(id = R.color.teal_200)
                    )
                } else {
                    Pair (
                        Icons.Filled.VisibilityOff,
                        colorResource(id = R.color.black)
                    )
                }
                IconButton(
                    onClick = {isPasswordVisible = !isPasswordVisible}
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedButton(
            onClick = { viewModel.onSignInClick(openAndPopUp)},
            border = null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(id = R.string.sign_in),
                style = TextStyle(
                    color = Color.Black
                )
            )
        }
    }
}
