package com.example.straymaps.ui.screens.stray_filed_reports

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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.straymaps.R
import com.example.straymaps.data.Converters
import com.example.straymaps.data.stray_animal.StrayAnimal
import com.example.straymaps.misc.StrayMapsScreen
import kotlinx.coroutines.launch

/** Sealed class representing the different criteria that the stray animal filed reports can be
 *  sorted by
 */
sealed class StraySortCriteria(val name: String){
    object TYPE: StraySortCriteria("Type")
    object COLOUR: StraySortCriteria("Colour")
    object SEX: StraySortCriteria("Sex")
    object DATE: StraySortCriteria("Date")
}

/** Composable function that displays already filed stray animal reports, combining different
 *  composable functions to achieve this
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrayAnimalFiledReportScreen(
    restartApp: (String) -> Unit,
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: StrayAnimalFiledReportsViewModel = hiltViewModel(),
){
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val allStrayAnimalReports by viewModel.allStrayAnimalFiledReportState.collectAsState()
    var sortByMenu by rememberSaveable { mutableStateOf(false) }
    var searchAlertDialog by rememberSaveable { mutableStateOf(false) }
    val microchipIdReportFound by viewModel.microchipIdReportFound.collectAsState()
    val listOfCriteria = listOf(
        StraySortCriteria.TYPE,
        StraySortCriteria.COLOUR,
        StraySortCriteria.SEX,
        StraySortCriteria.DATE
    )

    LaunchedEffect(Unit) {
        viewModel.initialize(restartApp)
    }

    HandlingSearchAlertDialog(
        searchAlertDialog = searchAlertDialog,
        context = context,
        viewModel = viewModel,
        onDismissRequest = {searchAlertDialog = false})

    microchipIdReportFound?.let {
        HandlingMicrochipIdSearch(
            microchipIdReportFound = it,
            context = context,
            viewModel = viewModel
            )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            StrayAnimalFiledReportScreenTopAppBar(
                onBackClick = onBackClick,
                sortByMenu = sortByMenu,
                onSearchClick = { searchAlertDialog = true },
                onSortClicked = { sortByMenu = sortByMenu.not() },
                onSortSelected = { sortCriteria ->
                    when (sortCriteria) {
                        StraySortCriteria.TYPE -> viewModel.getAllStrayReportsByType()
                        StraySortCriteria.COLOUR -> viewModel.getAllStrayReportsByColour()
                        StraySortCriteria.SEX -> viewModel.getAllStrayReportsBySex()
                        StraySortCriteria.DATE -> viewModel.getAllStrayReportsByDate()
                    }
                    sortByMenu = false
                },
                listOfCriteria = listOfCriteria,
                scrollBehavior =  scrollBehavior
            )
        },
    ) {innerPadding ->
        ReportsScreen(
            reportList = allStrayAnimalReports,
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
fun StrayAnimalFiledReportScreenTopAppBar(
    onBackClick: () -> Unit,
    sortByMenu: Boolean,
    onSearchClick: () -> Unit,
    onSortClicked: () -> Unit,
    onSortSelected: (StraySortCriteria) -> Unit,
    listOfCriteria: List<StraySortCriteria>,
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

/** Composable function that displays the list of stray animal reports made by ListOfReports function
 *  (or a notification that there are no filed reports otherwise)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportsScreen (
    reportList: List<StrayAnimal>,
    navController: NavController,
    viewModel: StrayAnimalFiledReportsViewModel,
    modifier: Modifier = Modifier
){
    val (reportToDelete, setReportToDelete) = remember { mutableStateOf<StrayAnimal?>(null) }
    var deleteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    )   {
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
                    .padding(dimensionResource(id = R.dimen.padding_small))
            )
        }
    }

    if (deleteDialog && reportToDelete != null) {
            DeleteDialog(
                onDismissRequest = {
                    setReportToDelete(null)
                    deleteDialog = false
                },
                strayAnimalReport = reportToDelete,
                onDeleteConfirmButton = {
                    coroutineScope.launch {
                        viewModel.deleteStrayAnimal(it)
                        setReportToDelete(null)
                        deleteDialog = false
                    }
                },
                onNotDeleteButton = {
                    setReportToDelete(null)
                    deleteDialog = false
                }
            )
    }
}

//Composable function that displays a list of cards made by StrayAnimalFiledReportCard function
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListOfReports(
    reportList: List<StrayAnimal>,
    onDeleteClick: (StrayAnimal) -> Unit,
    navController: NavController,
    viewModel: StrayAnimalFiledReportsViewModel,
    modifier: Modifier
){
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ){
        items(items = reportList){strayAnimal ->
            StrayAnimalFiledReportCard(
                animalReportCard = strayAnimal,
                onEditClick = { reportId ->
                    navController.navigate(StrayMapsScreen.EditStrayReportScreen.route + "/$reportId") },
                onDeleteClick = {onDeleteClick(strayAnimal)},
                viewModel = viewModel )
        }
    }
}


//Composable function that displays a card with all the stray animal information from the report
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StrayAnimalFiledReportCard(
    animalReportCard: StrayAnimal,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (StrayAnimal) -> Unit,
    viewModel: StrayAnimalFiledReportsViewModel
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
        if (currentUserId == animalReportCard.strayAnimalReportMadeByUserId)
            { Box {
                Row {
                    OutlinedButton(onClick = { animalReportCard.strayAnimalId?.let { onEditClick(it) } }) {
                        Text(text = stringResource(id = R.string.edit_report))
                    }
                    OutlinedButton(onClick = {
                        onDeleteClick(animalReportCard)
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
                        if (animalReportCard.strayAnimalPhotoPath == "none"){
                            ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.noimageavailable)
                                .crossfade(true)
                                .build()
                        }
                        else {
                            ImageRequest.Builder(LocalContext.current)
                                .data(animalReportCard.strayAnimalPhotoPath)
                                .crossfade(true)
                                .build()
                             },
                        placeholder = painterResource(id = R.drawable.noimageavailable),
                        contentDescription = stringResource(id = R.string.report_photo_placeholder),
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.padding_extra_extra_large))
                            .clickable(
                                onClick = {
                                    imageUri = animalReportCard.strayAnimalPhotoPath.toString()
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
                text = "Type of animal: ${animalReportCard.strayAnimalType}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Colour of the animal: ${animalReportCard.strayAnimalColour}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (animalReportCard.strayAnimalSex == "") {
                        "Sex of the animal: Not available"}
                        else {
                            "Sex of the animal: ${animalReportCard.strayAnimalSex}"},
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Description: ${animalReportCard.strayAnimalAppearanceDescription}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Last known location: ${animalReportCard.strayAnimalLocationDescription}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (animalReportCard.strayAnimalMicrochipID == "") {
                        "Microchip ID: Not available"}
                        else {
                            "Microchip ID: ${animalReportCard.strayAnimalMicrochipID}"},
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (animalReportCard.strayAnimalContactInformation == ""){
                        "Animal contact person: Not available"}
                        else {
                            "Animal contact person: ${animalReportCard.strayAnimalContactInformation}"},
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (animalReportCard.strayAnimalAdditionalInformation == "") {
                        "Additional information: Not available"}
                        else {
                            "Additional information: ${animalReportCard.strayAnimalAdditionalInformation}"},
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Report created: ${
                    animalReportCard.strayAnimalReportDateAndTime?.let {
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
    strayAnimalReport: StrayAnimal,
    onDeleteConfirmButton: (StrayAnimal) -> Unit,
    onNotDeleteButton:() -> Unit
) {
    AlertDialog(
        text = { Text (text = stringResource(id = R.string.confirm_delete))},
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = {
                    onDeleteConfirmButton(strayAnimalReport) },
                border = null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
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
                onClick = {
                    onNotDeleteButton()},
                border = null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
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
    viewModel: StrayAnimalFiledReportsViewModel,
    onDismissRequest: () -> Unit
){
    searchAlertDialog.takeIf {it}?.let {
        SearchAlertDialog(
            userInput = viewModel.userInputMicrochipID,
            onValueChange = viewModel::updateUserInputMicrochipId,
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                if (viewModel.userInputMicrochipID.isNotBlank()) {
                    viewModel.findStrayAnimalReportByMicrochipId(viewModel.userInputMicrochipID)
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
fun HandlingMicrochipIdSearch(
    microchipIdReportFound: Boolean,
    context: Context,
    viewModel: StrayAnimalFiledReportsViewModel
) {
    LaunchedEffect(microchipIdReportFound) {
        when (microchipIdReportFound) {
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
    listOfCriteria: List<StraySortCriteria>,
    onSortSelected: (StraySortCriteria) -> Unit,
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
