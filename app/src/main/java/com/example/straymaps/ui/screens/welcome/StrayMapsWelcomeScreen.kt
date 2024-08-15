package com.example.straymaps.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.straymaps.R
import com.example.straymaps.ui.theme.dancingScriptFontFamily

val listOfBrushGradientColors = listOf(
    Color(0xFF44464F),
    Color(0xFFA7391C),
    Color(0xFF611300)
)

/** Composable function that displays the first options that a user has when opening StrayMaps app
 *  They can choose the following options:
 *  1. sign up, which will take them to another screen where they can create an account
 *  2. sign in, which will take them to another screen where they can sign in to to their
 *      already existing account
 *  3. continue as a "guest", which will create an anonymous account for them, which later on they
 *      can convert into an actual account
 */
@Composable
fun WelcomeScreen(
    showSignUpPage: () -> Unit,
    showSignInPage: () -> Unit,
    signInAnonymously: (String, String) -> Unit,
    viewModel: StrayMapsWelcomeScreenViewModel = hiltViewModel()
){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(color = MaterialTheme.colorScheme.onPrimary)
    ) {
        Box(
        ) {
            Image(
                painter = painterResource(R.drawable.initial_screen_photo),
                contentDescription = stringResource(R.string.initial_photo_description),
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_large))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape_medium)))
            )
        }
        Spacer(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.padding_medium))
        )
        Text(
            text = stringResource(id = R.string.welcome_sign),
            style = MaterialTheme.typography.displaySmall
        )
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
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
        ElevatedButton(
            onClick = {
                showSignUpPage() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Text(
                text = stringResource(id = R.string.sign_up),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black)
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        ElevatedButton(
            onClick = {
                showSignInPage() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Text(
                text = stringResource(id = R.string.sign_in),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black)
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        ElevatedButton(
            onClick = {
                viewModel.signInAnonymously(signInAnonymously)},
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Text(
                text = stringResource(id = R.string.continue_as_guest),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black)

            )
        }
    }
}


