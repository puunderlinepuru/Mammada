package com.pupuru.mammada

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupuru.mammada.ui.theme.MammadaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Vector
import kotlin.math.roundToInt
import kotlin.text.toInt

var globalReputationChange = 0
var globalCommentContents = ""

val abrilFamily = FontFamily(
    Font(R.font.abril_fatface, FontWeight.Normal)
)
val abyssinicaFamily = FontFamily(
    Font(R.font.abyssinica_sil, weight = FontWeight.Normal)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MammadaTheme {
                MammadaTrackerApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MammadaTrackerApp() {
    MammadaTrackerStaticBase(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MammadaTrackerStaticBase(modifier: Modifier = Modifier) {

//    FIRST TIME READ
    val context = LocalContext.current
    var firstTimeRead = 0
    var readLinesVector = Vector<String>()
    try {
        context.openFileInput("myfile.txt").use { fis ->
//            var readString = fis.bufferedReader().use { it.readText() }
//            firstTimeRead = readString.toInt()
//            println("first time read: $firstTimeRead")

            fis.bufferedReader().use {
                bufferedReader ->
                bufferedReader.forEachLine { line ->
                    readLinesVector.add(line)
                    println("Added Line \" $line\" to vector")
                }
            }
        }

        firstTimeRead = readLinesVector.firstElement().toInt()

        readLinesVector.removeElementAt(0)
        println("removed final reputation, vecor is now: $readLinesVector")
    } catch (e: IOException) {
        Log.e("DiceApp", "Failed to read file", e)
    }



    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            modifier = Modifier,
            text = stringResource(R.string.app_name),
            fontFamily = abrilFamily,
            fontSize = 32.sp,
            textAlign = TextAlign.Left,
//            lineHeight = 10.sp,
//            maxLines = 1
        )
        Image(
            modifier = Modifier.size(300.dp),
            painter = painterResource(R.drawable.mammada),
            contentDescription = "that's me"
        )
        MovingElements(firstTimeRead, readLinesVector)
    }
}

@Composable
fun ReputationSlider() {
    var sliderPosition by remember { mutableFloatStateOf(50f) }
    Slider(
        modifier = Modifier.padding(horizontal = 25.dp),
        value = sliderPosition,
//            steps = 9,
//            onValueChange = {sliderPosition = it},
        onValueChange = {
            sliderPosition = it.roundToInt().toFloat() },
        onValueChangeFinished = {
            globalReputationChange = sliderPosition.toInt() - 50
            println("slider stopped moving, reputation changed to: $globalReputationChange")
        },
        valueRange = 0f..100f
    )
    Text(
        modifier = Modifier,
        text = "${sliderPosition.toInt()-50}%"
    )
}

@Composable
fun ReputationComments() {
    var commentContents by remember { mutableStateOf("") }
    globalCommentContents = commentContents

    OutlinedTextField(
        value = commentContents,
        onValueChange =  {
            commentContents = it
            println("new commentContents = $commentContents")
                         },
        label = { Text("Комментарий") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .wrapContentHeight(),
        textStyle = TextStyle(color = Color.Black)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(reputationHistory: Vector<String>) {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    var skipPartiallyExpanded by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    // App content

    Button(
        onClick = { openBottomSheet = !openBottomSheet }
    ) {
        Text(
            stringResource(R.string.ShowHistoryButton),

        )
    }
    // Sheet content
    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            sheetState = bottomSheetState,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    // Note: If you provide logic outside of onDismissRequest to remove the sheet,
                    // you must additionally handle intended state cleanup, if any.
                    onClick = {
                        scope
                            .launch { bottomSheetState.hide() }
                            .invokeOnCompletion {
                                if (!bottomSheetState.isVisible) {
                                    openBottomSheet = false
                                }
                            }
                    }
                ) {
                    Text(stringResource(R.string.HideHistoryButton))
                }
            }
            LazyColumn (reverseLayout = true) {
                items(reputationHistory.size) {
                    ListItem(
                        headlineContent = { Text("${reputationHistory[it]}") },
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                    )
                }
            }
        }
    }

}


@Composable
fun MovingElements(finalReputation: Int, reputationHistory: Vector<String>) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var reputationInt by remember { mutableIntStateOf(finalReputation) }

    Text(
        modifier = Modifier,
        text = "Rep: $reputationInt%\n",
        fontFamily = abyssinicaFamily,
        fontSize = 20.sp
    )

    Spacer(modifier = Modifier.height(80.dp))

    ReputationSlider()
    ReputationComments()

    Button(onClick = {

        val filename = "myfile.txt"
        val reputationString = "${reputationInt + globalReputationChange}"

        if (reputationHistory.size >= 10) {
            reputationHistory.removeAt(0)
            println("Vector has max size, removing the first index")
        }
        reputationHistory.add("$globalReputationChange $globalCommentContents")
        println("combined \\n+$globalReputationChange+$globalCommentContents")

        scope.launch(Dispatchers.IO) {
//            WRITE
            try {
                context.openFileOutput(filename, Context.MODE_PRIVATE).use { fos ->

                    fos.write("".toByteArray())
//                    current reputation
                    fos.bufferedWriter().use {
                        bufferedWriter ->
                        bufferedWriter.write(reputationString)
                        for (string in reputationHistory) {
                            bufferedWriter.write("\n" + string)
                        }
                    }

                }
            } catch (e: IOException) {
                Log.e("DiceApp", "Failed to write file", e)
            }
            reputationHistory.clear()

//            READ
            try {
                context.openFileInput(filename).use { fis ->
//                    val readString = fis.bufferedReader().use { it.readText() }
//                    affinity = readString.toInt()
//                    println("affinity is now: $affinity")

                    fis.bufferedReader().use {
                            bufferedReader ->
                        bufferedReader.forEachLine { line ->
                            reputationHistory.add(line)
                            println("Added Line \"$line\" to reputationHistory. It is now: $reputationHistory")
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("DiceApp", "Failed to read file", e)
            }
            reputationInt = reputationHistory.firstElement().toInt()
            reputationHistory.removeElementAt(0)
            println("removed final reputation, vector is now: $reputationHistory")
        }
    }) {
        Text(stringResource(R.string.SaveButton))
    }

    BottomSheet(reputationHistory)
}
