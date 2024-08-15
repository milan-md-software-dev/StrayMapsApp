package com.example.straymaps.ui.screens.pets_filed_reports

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.straymaps.R
import com.example.straymaps.data.Converters
import com.example.straymaps.data.lost_pet.LostPet
import com.example.straymaps.misc.StrayMapsScreen
import kotlinx.coroutines.launch

/** Sealed class representing the different criteria that the lost pet filed reports can be
 *  sorted by
 */
sealed class PetSortCriteria(val name: String){
    object TYPE: PetSortCriteria("Type")
    object COLOUR: PetSortCriteria("Colour")
    object SEX: PetSortCriteria("Sex")
    object DATE: PetSortCriteria("Date")
}

/** Composable function that displays already filed lost pet reports, combining different
 *  composable functions to achieve this
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostPetFiledReportScreen(
    restartApp:(String) -> Unit,
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: LostPetFiledReportsScreenViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val allLostPetReports by viewModel.allLostPetFiledReportState.collectAsState()
    var sortByMenu by rememberSaveable { mutableStateOf(false) }
    var searchAlertDialog by rememberSaveable { mutableStateOf(false) }
    val microchipIdReportFound by viewModel.microchipIdReportFound.collectAsState()
    val listOfCriteria = listOf(
        PetSortCriteria.TYPE,
        PetSortCriteria.COLOUR,
        PetSortCriteria.SEX,
        PetSortCriteria.DATE
    )

    LaunchedEffect(Unit){
        viewModel.initialize(restartApp)
    }

    HandlingSearchAlertDialog(
        searchAlertDialog = searchAlertDialog,
        context = context,
        viewModel = viewModel,
        onDismissRequest = { searchAlertDialog = false }
        )

    microchipIdReportFound?.let {
        HandlingMicrochipIdSearch(
        microchipIdReportFound = it,
            context = context,
            viewModel = viewModel)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            LostPetFiledReportScreenTopAppBar(
                onBackClick = onBackClick,
                sortByMenu = sortByMenu,
                onSearchClick = { searchAlertDialog = true },
                onSortClicked = { sortByMenu = sortByMenu.not() },
                onSortSelected = { sortCriteria ->
                    when (sortCriteria) {
                        PetSortCriteria.TYPE -> viewModel.getAllLostPetReportsByType()
                        PetSortCriteria.COLOUR -> viewModel.getAllLostPetReportsByColour()
                        PetSortCriteria.SEX -> viewModel.getAllLostPetReportsBySex()
                        PetSortCriteria.DATE -> viewModel.getAllLostPetReportsByDate()
                    }
                    sortByMenu = false
                },
                listOfCriteria = listOfCriteria,
                scrollBehavior = scrollBehavior
            )
        },
    ) {innerPadding ->
        ReportsScreen(
            reportList = allLostPetReports,
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        )
    }
}

//Composable function for the Top app bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostPetFiledReportScreenTopAppBar(
    onBackClick: () -> Unit,
    sortByMenu: Boolean,
    onSearchClick: () -> Unit,
    onSortClicked: () -> Unit,
    onSortSelected: (PetSortCriteria) -> Unit,
    listOfCriteria: List<PetSortCriteria>,
    scrollBehavior: TopAppBarScrollBehavior
){
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            titleContentColor = Color.Black
        ),
        title = {
            Text(
                text = stringResource(id = R.string.filed_reports_screen_top_app_bar),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackClick()}) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.arrow_back)
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onSearchClick() }
            ){
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.search_icon)
                )
            }
            IconButton(
                onClick = { onSortClicked() }
            ){
                Icon(
                    imageVector = Icons.Filled.Sort,
                    contentDescription = stringResource(id = R.string.sort_by)
                )
            }
            if (sortByMenu) {
                SortDropdownMenu(
                    listOfCriteria = listOfCriteria,
                    onSortSelected = onSortSelected,
                    onDismissRequest = { onSortClicked() }
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

/** Composable function that displays the list of lost pet reports made by ListOfReports function
 *  (or a notification that there are no filed reports otherwise)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportsScreen(
    reportList: List<LostPet>,
    navController: NavController,
    viewModel: LostPetFiledReportsScreenViewModel,
    modifier: Modifier = Modifier
){
    val (reportToDelete, setReportToDelete) = remember { mutableStateOf<LostPet?>(null) }
    var deleteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (reportList.isEmpty()){
            Text(
                text = stringResource(id = R.string.empty_list),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
        else {
            ListOfReports(
                reportList = reportList,
                onDeleteClick = {report ->
                                setReportToDelete(report)
                                deleteDialog = true
                },
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
                    .padding(dimensionResource(id = R.dimen.padding_small)))
        }
    }

    if (deleteDialog && reportToDelete != null) {
        DeleteDialog(
            onDismissRequest = {
                setReportToDelete(null)
                deleteDialog = false
            },
            lostPetReport = reportToDelete,
            onDeleteConfirmButton = {
                coroutineScope.launch {
                    viewModel.deleteLostPet(reportToDelete)
                    setReportToDelete(null)
                    deleteDialog = false
                }
            }
        ) {

        }
    }
}

//Composable function that displays a list of cards made by LostPetFiledReportCard function
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListOfReports(
    reportList: List<LostPet>,
    onDeleteClick: (LostPet) -> Unit,
    navController: NavController,
    viewModel: LostPetFiledReportsScreenViewModel,
    modifier: Modifier
){
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ){
        items(items = reportList){lostPet ->
            LostPetFiledReportCard(
                lostPetReportCard = lostPet,
                onEditClick = { reportId ->
                              navController.navigate(StrayMapsScreen.EditPetReportScreen.route + "/$reportId")
                },
                onDeleteClick = {onDeleteClick(lostPet)},
                viewModel = viewModel)
        }
    }
}

//Composable function that displays a card with all the lost pet information from the report
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LostPetFiledReportCard(
    lostPetReportCard: LostPet,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (LostPet) -> Unit,
    viewModel: LostPetFiledReportsScreenViewModel
){
    var showDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf("") }
    val currentUserId by viewModel.currentUserId.collectAsState()


    Card(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
        elevation = CardDefaults.cardElevation(dimensionResource(id = R.dimen.padding_very_small))
    ){
        if (currentUserId == lostPetReportCard.lostPetReportMadeByUserId)
        { Box {
            Row {
                OutlinedButton(onClick = { lostPetReportCard.lostPetId?.let {onEditClick(it)} }) {
                    Text(text = stringResource(id = R.string.edit_report))
                }
                OutlinedButton(onClick = {
                    onDeleteClick(lostPetReportCard)
                }) {
                    Text(text = stringResource(id = R.string.delete_report))
                }
            }
        }

        }
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(
                    modifier = Modifier.weight(0.3f)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(2f)
                ) {
                    AsyncImage(
                        model =
                        if (lostPetReportCard.lostPetPhoto == "none"){
                            ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.noimageavailable)
                                .crossfade(true)
                                .build()
                        }
                        else {
                            ImageRequest.Builder(LocalContext.current)
                                .data(lostPetReportCard.lostPetPhoto)
                                .crossfade(true)
                                .build()
                        },
                        placeholder = painterResource(id = R.drawable.noimageavailable),
                        contentDescription = "Photo of the animal in question.",
                        modifier = Modifier
                            .size(128.dp)
                            .clickable(
                                onClick = {
                                    imageUri = lostPetReportCard.lostPetPhoto.toString()
                                    showDialog = true
                                }
                            )
                    )
                }
                Spacer(
                    modifier = Modifier.weight(0.3f)
                )
            }
            Text(
                text = "Type of animal: ${lostPetReportCard.lostPetType}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Animal name: ${lostPetReportCard.lostPetName}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Colour of the animal: ${lostPetReportCard.lostPetColour}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (lostPetReportCard.lostPetSex == "") {
                    "Sex of the animal: Not available"}
                else {
                    "Sex of the animal: ${lostPetReportCard.lostPetSex}"},
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Description: ${lostPetReportCard.lostPetAppearanceDescription}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Last known location: ${lostPetReportCard.lostPetLastKnownLocation}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (lostPetReportCard.lostPetMicrochipId == "") {
                    "Microchip ID: Not available"}
                else {
                    "Microchip ID: ${lostPetReportCard.lostPetMicrochipId}"},
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (lostPetReportCard.lostPetContactInformation == ""){
                    "Animal contact person: Not available"}
                else {
                    "Animal contact person: ${lostPetReportCard.lostPetContactInformation}"},
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (lostPetReportCard.lostPetAdditionalInformation == "") {
                    "Additional information: Not available"}
                else {
                    "Additional information: ${lostPetReportCard.lostPetAdditionalInformation}"},
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Report created: ${
                    lostPetReportCard.lostPetReportDateAndTime?.let {
                        viewModel.formatLocalDateTime(
                            Converters().toLocalDateTime(it)
                        )
                    }
                }",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showDialog && imageUri.isNotEmpty()){
        Dialog(onDismissRequest = {showDialog = false}) {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(id = R.string.expanded_image),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

//Composable function that allows the user to delete the report made by them
@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    lostPetReport: LostPet,
    onDeleteConfirmButton: (LostPet) -> Unit,
    onNotDeleteButton:() -> Unit
) {
    AlertDialog(
        text = { Text (text = stringResource(id = R.string.confirm_delete))},
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onDeleteConfirmButton(lostPetReport) },
                border = null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Text(text = stringResource(id = R.string.confirm),
                    style = TextStyle(
                        color = Color.Black
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onNotDeleteButton() },
                border = null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Text(text = stringResource(id = R.string.cancel),
                    style = TextStyle(
                        color = Color.Black
                    ))
            }
        }
    )
}

/** Composable function that allows the user to enter a microchip id, which is then
 *  used to look for a report with the matching microchip id
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAlertDialog(
    userInput: String,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    confirmButton: (String) -> Unit,
){
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null
            )
        },
        text = {
            TextField(
                value = userInput,
                onValueChange = onValueChange,
                label = {
                    Text(
                        text = stringResource(id = R.string.microchip_id_text_field),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = { confirmButton(userInput) },
                border = null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Text(text = stringResource(id = R.string.confirm),
                    style = TextStyle(
                        color = Color.Black
                    ))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {onDismissRequest()},
                border = null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ){
                Text(text = stringResource(id = R.string.cancel),
                    style = TextStyle(
                        color = Color.Black
                    ))
            }
        }
    )
}

//Function that handles the microchip ID search
@Composable
private fun HandlingSearchAlertDialog(
    searchAlertDialog: Boolean,
    context: Context,
    viewModel: LostPetFiledReportsScreenViewModel,
    onDismissRequest: () -> Unit
    ){
    searchAlertDialog.takeIf {it}?.let {
        SearchAlertDialog(
            userInput = viewModel.userInputMicrochipId,
            onValueChange = viewModel::updateUserInputMicrochipId,
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                if (viewModel.userInputMicrochipId.isNotBlank()) {
                    viewModel.findLostPetReportByMicrochipId(viewModel.userInputMicrochipId)
                    onDismissRequest()
                } else {
                    Toast.makeText(context, "Please enter proper microchip id.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

// Composable function that displays whether the microchip ID search was a success or not
@Composable
private fun HandlingMicrochipIdSearch(
    microchipIdReportFound: Boolean,
    context: Context,
    viewModel: LostPetFiledReportsScreenViewModel
){
    LaunchedEffect(microchipIdReportFound) {
        when (microchipIdReportFound){
            true -> Toast.makeText(context, "Report found!", Toast.LENGTH_SHORT).show()
            false -> Toast.makeText(context, "Report not found!", Toast.LENGTH_SHORT).show()
        }
        viewModel.resetMicrochipIdReportFoundValueToNull()
    }
}

/** Composable that displays the criteria by which the reports can be sorted, allowing the user
 *  to choose
 */
@Composable
fun SortDropdownMenu (
    listOfCriteria: List<PetSortCriteria>,
    onSortSelected: (PetSortCriteria) -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest) {
        listOfCriteria.forEach { sortCriteria ->
            DropdownMenuItem(
                text = { Text(text = sortCriteria.name)},
                onClick = {
                    onSortSelected(sortCriteria)
                    onDismissRequest()
                })
        }
    }
}