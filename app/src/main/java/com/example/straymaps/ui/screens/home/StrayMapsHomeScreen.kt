package com.example.straymaps.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.straymaps.R
import com.example.straymaps.data.User
import com.example.straymaps.misc.DefaultImageProvider
import com.example.straymaps.ui.screens.welcome.listOfBrushGradientColors
import com.example.straymaps.ui.theme.dancingScriptFontFamily

/** Composable function that represents the Home screen of StrayMaps app
 *  Using a LazyColumn to represent different choices (i.e. destinations) that user can go to
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrayMapsHomeScreen(
    onStraySpotterButtonClicked: () -> Unit,
    onLostPetsButtonClicked: () -> Unit,
    onStraySelection: () -> Unit,
    onPetSelection: () -> Unit,
    onWhereToGoButtonClicked: () -> Unit,
    onFeedAStrayButtonClicked: () -> Unit,
    restartApp: (String) -> Unit,
    navigate:(String) -> Unit,
    openAndPopUp: (String, String) -> Unit,
    viewModel: StrayMapsHomeScreenViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val defaultImageProvider = DefaultImageProvider.getDefaultImagePath(context)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val currentUserProfileInfo by viewModel.currentUserProfile.collectAsState()
    var showUserAccountDialog by rememberSaveable {mutableStateOf(false)}
    var showLogoutAppDialog by rememberSaveable {mutableStateOf(false)}
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showFiledReportsSelectionDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit){
        viewModel.initialize(restartApp)
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
                    Text(
                        text = stringResource(R.string.home_screen_top_app_bar),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontFamily = dancingScriptFontFamily,
                            fontWeight = FontWeight.Medium,
                            brush = Brush.linearGradient(
                                colors = listOfBrushGradientColors
                            ),
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { showUserAccountDialog = true },
                        ) {
                        Icon(
                            Icons.Filled.AccountBox,
                            "Account information",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        ) { innerPadding ->
        Row(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.onPrimary)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(top = dimensionResource(id = R.dimen.padding_medium))
                    .weight(2f)
            ) {
                //StraySpotter
                item {
                    HomeScreenChoices(
                        painter = painterResource(id = R.drawable.strayspotter),
                        textAndDescription = stringResource(id = R.string.stray_spotter),
                        onClick = { onStraySpotterButtonClicked()  }
                    )
                }
                //LostPets
                item {
                    HomeScreenChoices(
                        painter = painterResource(id = R.drawable.lostandfound),
                        textAndDescription = stringResource(id = R.string.lost_and_found),
                        onClick = { onLostPetsButtonClicked() }
                    )
                }
                //SeeFiledReports
                item {
                    HomeScreenChoices(
                        painter = painterResource(id = R.drawable.filedreports),
                        textAndDescription = stringResource(id = R.string.already_filed_reports),
                        onClick = { showFiledReportsSelectionDialog = true }
                    )
                }
                //WhereToGo
                item {
                    HomeScreenChoices(
                        painter = painterResource(id = R.drawable.wheretogo),
                        textAndDescription = stringResource(id = R.string.where_to_go),
                        onClick = { onWhereToGoButtonClicked() }
                    )
                }
                //FeedAStray
                item {
                    HomeScreenChoices(
                        painter = painterResource(id = R.drawable.feedastray),
                        textAndDescription = stringResource(id = R.string.feed_a_stray),
                        onClick = { onFeedAStrayButtonClicked() }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }

    /** If the value is true, brings up a composable function that lets the user choose
     *  which group of filed reports they would like to see
     */
    if (showFiledReportsSelectionDialog){
        FiledReportSelection(
            onDismissRequest = { showFiledReportsSelectionDialog = false },
            onStraySelection = { onStraySelection() },
            onPetSelection = { onPetSelection() },
            painter = painterResource(id = R.drawable.bebe)
        )
    }

    //Dialog that checks whether the user would like to sign out
    if (showLogoutAppDialog) {
        ConfirmationDialog(
            titleRes = R.string.sign_out,
            textRes = R.string.sign_out_question,
            confirmationButtonRes = R.string.yes,
            dismissButtonRes = R.string.no,
            onConfirm = {
                viewModel.onSignOutClick()
                showLogoutAppDialog = false
                showUserAccountDialog = false
            },
            onDismiss = {
                showLogoutAppDialog = false
            }
            )
    }

    //If the value is true, brings up a composable function that shows the user their account information
    if (showUserAccountDialog){
        currentUserProfileInfo?.let {
            UserAccountInformation(
                onDismissRequest = {showUserAccountDialog = false},
                currentUser = it,
                onSignInClick = {viewModel.onSignInClick(navigate)},
                onSignUpClick = {viewModel.onSignUpClick(openAndPopUp)},
                onLogoutClick = {showLogoutAppDialog = true},
                onDeleteAccountClick = {showDeleteAccountDialog = true}
            )
        }
    }

    //Dialog that lets the user delete their account
    if (showDeleteAccountDialog) {
        ConfirmationDialog(
            titleRes = R.string.delete_account,
            textRes = R.string.delete_account_question,
            confirmationButtonRes = R.string.yes,
            dismissButtonRes = R.string.no,
            onConfirm = {
                viewModel.onDeleteAccountClick()
                showDeleteAccountDialog = false
                showUserAccountDialog = false
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

//Composable function used as a blueprint for different confirmation dialogs
@Composable
fun ConfirmationDialog(
    titleRes: Int,
    textRes: Int,
    confirmationButtonRes: Int,
    dismissButtonRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = titleRes)
            )
        },
        text = {
            Text(
                text = stringResource(id = textRes)
            )
        },
        confirmButton = { 
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(id = confirmationButtonRes)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = dismissButtonRes)
                )
            }
        }
    )
}

//Composable function used as a blueprint for showing the navigation choices on Home screen
@Composable
fun HomeScreenChoices(
    painter: Painter,
    textAndDescription : String,
    onClick: () -> Unit,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = textAndDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable(onClick = { onClick() })
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)) )
        Text(
            text = textAndDescription,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

/** Composable function that lets the user choose which filed reports group they would like to see,
 *  i.e. stray animal reports or lost pet reports
 */
@Composable
fun FiledReportSelection(
    onDismissRequest: () -> Unit,
    onStraySelection: () -> Unit,
    onPetSelection: () -> Unit,
    painter: Painter
){
    Dialog(
        onDismissRequest = {onDismissRequest()}
    ){
        Card(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large)),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
               Image(
                   painter = painter,
                   contentDescription = null,
                   contentScale = ContentScale.Fit
               )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Text (
                    text = stringResource(id = R.string.filed_report_selection),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ElevatedButton(
                        onClick = {onStraySelection()},
                        border = null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.padding_small))
                    ){
                        Text("Stray animals",
                            maxLines = 2,
                            style = TextStyle(
                                color = Color.Black
                            )
                        )
                    }
                    ElevatedButton(
                        onClick = {onPetSelection()},
                        border = null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.padding_small))
                    ){
                        Text("Lost pets",
                            style = TextStyle(
                                color = Color.Black
                            )
                            )
                    }
                }
            }
        }
    }
}

//Composable function that shows the user account information
@Composable
fun UserAccountInformation(
    onDismissRequest: () -> Unit,
    currentUser: User,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
){
    Dialog(
        onDismissRequest = {onDismissRequest()}
    ) {
        Card(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large)),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium))) {
            Column(
                Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.account_centre),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)))
                if (currentUser.isAnonymous == true) {
                    Text(
                        text = stringResource(id = R.string.anonymous_user),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)))
                    OutlinedButton(
                        onClick = { onSignInClick() },
                        border = null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.fillMaxWidth(),
                        shape = IconButtonDefaults.outlinedShape
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_in),
                            style = TextStyle(
                                color = Color.Black
                            )
                        )
                    }
                    OutlinedButton(
                        onClick = { onSignUpClick() },
                        border = null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.fillMaxWidth(),
                        shape = IconButtonDefaults.outlinedShape
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_up),
                            style = TextStyle(
                                color = Color.Black
                            )
                        )
                    }
                }
                else if (currentUser.isAnonymous == false) {
                    Text (
                        text = "Email: ${currentUser.email}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)))
                    OutlinedButton(
                        onClick = { onLogoutClick() },
                        border = null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.fillMaxWidth(),
                        shape = IconButtonDefaults.outlinedShape
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_out),
                            style = TextStyle(
                                color = Color.Black
                            )
                        )
                    }
                    OutlinedButton(
                        onClick = { onDeleteAccountClick() },
                        border = null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.fillMaxWidth(),
                        shape = IconButtonDefaults.outlinedShape
                    ) {
                        Text(
                            text = stringResource(id = R.string.delete_account),
                            style = TextStyle(
                                color = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun StrayMapsHomeScreenPreview(){
    StrayMapsHomeScreen(
        onStraySpotterButtonClicked = {},
        onLostPetsButtonClicked = {},
        onStraySelection = {},
        onPetSelection = {},
        onWhereToGoButtonClicked = {},
        onFeedAStrayButtonClicked = {},
        restartApp = {},
        navigate = {},
        openAndPopUp = {_,_ ->}
    )
}

