package com.example.straymaps.ui.screens.sign_up

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.straymaps.R
import com.example.straymaps.data.User
import com.example.straymaps.ui.screens.welcome.listOfBrushGradientColors
import com.example.straymaps.ui.theme.dancingScriptFontFamily

//Composable function that displays the sign up screen of StrayMaps app, with the top app bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreenWithTopAppBar(
    onBackClick: () -> Unit,
    openAndPopUp: (String, String) -> Unit,
    viewModel: StrayMapsSignUpScreenViewModel = hiltViewModel()
    ){

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val currentUser by viewModel.currentUserProfile.collectAsState()
    val email by viewModel.signUpEmail.collectAsState()
    val password by viewModel.signUpPassword.collectAsState()
    val confirmPassword by viewModel.signUpConfirmPassword.collectAsState()
    val isNotMatching by remember {
        derivedStateOf {
            password != confirmPassword
        }
    }

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
                        text = stringResource(id = R.string.sign_up),
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
            SignUpScreen(
                currentUser,
                email,
                password,
                confirmPassword,
                isNotMatching,
                openAndPopUp,
                viewModel,
            )
        }
    }
}

//Composable function that displays the sign up screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    currentUser: User?,
    email: String,
    password: String,
    confirmPassword: String,
    isNotMatching: Boolean,
    openAndPopUp: (String, String) -> Unit,
    viewModel: StrayMapsSignUpScreenViewModel = hiltViewModel(),
){
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))) {
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
            value = email,
            onValueChange = { email ->
                viewModel.updateEmail(email)
            },
            label = {
                Text(stringResource(id = R.string.email))
            },
            isError = !email.isEmailValid()
        )


        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedTextField(
            value = password,
            onValueChange = {password -> viewModel.updatePassword(password)},
            label = {
                Text(stringResource(id = R.string.password))
            },
            isError = !password.isPasswordValid(),
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

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {confirmPassword -> viewModel.updateConfirmPassword(confirmPassword)},
            label = {
                Text(stringResource(id = R.string.confirm_password))
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

        if (isNotMatching) {
            Text(text = stringResource(id = R.string.passwords_not_matching),
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedButton(
            onClick = {
                if (currentUser?.isAnonymous == true) { viewModel.onSignUpClick(openAndPopUp) }
                else if (currentUser?.isAnonymous == false) { viewModel.onSignUpAnonymousLinkClick(openAndPopUp) }
            },
            border = null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
            Text(
                text = stringResource(id = R.string.sign_up),
                style = TextStyle(
                    color = Color.Black
                )
            )
        }
    }
}




















