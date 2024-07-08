package com.ntech.listadetarefas


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display.DEFAULT_DISPLAY
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ntech.listadetarefas.ui.theme.ListaDeTarefasTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.tooling.preview.Preview



class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedPreferences = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        setContent {
            ListaDeTarefasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    App(sharedPreferences, editor)
                    Spacer(modifier = Modifier.height(16.dp))

                    }
                }
            }
        }
    }



@Composable


fun gradientBackgroundBrush(
    isVerticalGradient: Boolean,
    colors: List<Color>
): Brush {
    val endOffset = if (isVerticalGradient) {
        Offset(0f, Float.POSITIVE_INFINITY)
    } else {
        Offset(Float.POSITIVE_INFINITY, 0f)
    }

    return Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = endOffset
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor) {
    var tasks by remember { mutableStateOf(loadTasks(sharedPreferences)) }
    var newTaskText by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    val deletedItems by remember { mutableStateOf(mutableSetOf<Int>()) }
    val gradientListColors = listOf(
        Color(0xFFDCE6EB),
        Color(0xFFA9C5D5),
        Color(0xFF44697D)
    )
    val myFontFamily = FontFamily(
        Font(resId = R.font.roboto_regular, weight = FontWeight.Normal),
        Font(resId = R.font.roboto_medium, weight = FontWeight.Medium)
    )

    fun saveTasks(tasks: List<Pair<String, Boolean>>) {
        editor.putInt("task_count", tasks.size)
        tasks.forEachIndexed { index, task ->
            editor.putString("task_text_$index", task.first)
            editor.putBoolean("task_checked_$index", task.second)
        }
        editor.apply()
    }

    fun addTask() {
        if (text.isNotEmpty()) {
            tasks = tasks + Pair(text, false)
            text = ""
            saveTasks(tasks)
        }
    }

    Column(
        Modifier
            .background(
                brush = gradientBackgroundBrush(
                    isVerticalGradient = true,
                    colors = gradientListColors
                )
            )
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(54.dp))
        Text(
            text = "Minhas Tarefas",
            style = TextStyle(
                color = Color(0xFF345465),
                fontSize = 22.sp,
                fontFamily = myFontFamily,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 40.dp)
                .align(Alignment.Start)
        )
        Spacer(modifier = Modifier.size(1.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f)
        ) {
            itemsIndexed(tasks) { index, task ->
                var isEditing by remember { mutableStateOf(false) }
                var editedText by remember { mutableStateOf(task.first) }
                var isDeleted by remember { mutableStateOf(false) }
                var isChecked by remember { mutableStateOf(task.second) }

                if (!isDeleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 5.dp,
                                shape = RoundedCornerShape(24.dp),
                                clip = false
                            )
                            .background(
                                color = Color.White,
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    if (dragAmount.dp < -45.dp) { // Verifica se o dragAmount excede 30dp
                                        isDeleted = true
                                        deletedItems.add(index)
                                        tasks = tasks
                                            .toMutableList()
                                            .apply {
                                                removeAt(index)
                                            }
                                        saveTasks(tasks)
                                    }
                                }
                            }
                    ) {
                        IconButton(
                            onClick = {
                                isChecked = !isChecked
                                tasks = tasks.toMutableList().apply {
                                    this[index] = Pair(this[index].first, isChecked)
                                }
                                saveTasks(tasks)
                            },
                            modifier = Modifier
                                .padding(10.dp)
                                .background(Color.White, shape = RoundedCornerShape(50.dp))
                                .size(40.dp)
                        ) {
                            val painter: Painter = if (isChecked) {
                                painterResource(id = R.drawable.check_circle_20dp)
                            } else {
                                painterResource(id = R.drawable.circle_20dp)
                            }
                            Icon(
                                painter = painter,
                                contentDescription = null,
                                tint = Color(0xFF44697D),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (isEditing) {
                            TextField(
                                value = editedText,
                                onValueChange = { editedText = it },
                                colors = TextFieldDefaults.textFieldColors(
                                    disabledTextColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Transparent)
                                    .onKeyEvent {
                                        if (it.key.nativeKeyCode == Key.Enter.nativeKeyCode) {
                                            tasks = tasks
                                                .toMutableList()
                                                .apply {
                                                    this[index] =
                                                        Pair(editedText, this[index].second)
                                                }
                                            isEditing = false
                                            saveTasks(tasks)
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        tasks = tasks.toMutableList().apply {
                                            this[index] = Pair(editedText, this[index].second)
                                        }
                                        isEditing = false
                                        saveTasks(tasks)
                                    }
                                )
                            )
                        } else {
                            Text(
                                text = task.first,
                                style = if (task.second) {
                                    TextStyle(
                                        fontFamily = myFontFamily,
                                        fontSize = 14.sp,
                                        textDecoration = LineThrough
                                    )
                                } else {
                                    TextStyle(
                                        fontFamily = myFontFamily,
                                        fontSize = 14.sp,
                                        textDecoration = TextDecoration.None
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Transparent)
                                    .clickable {
                                        isEditing = true
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    singleLine = true,
                    value = text,
                    onValueChange = { text = it },
                    shape = RoundedCornerShape(16.dp),
                    placeholder = {
                        Text(text = "Escrever nova Tarefa...")
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        disabledTextColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .background(
                            color = Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(24.dp),
                            clip = false
                        )
                        .border(width = 1.dp, color = Color.Transparent)
                        .onKeyEvent {
                            if (it.key.nativeKeyCode == Key.Enter.nativeKeyCode) {
                                addTask()
                                true
                            } else {
                                false
                            }
                        },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { addTask() }
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = {
                            tasks = tasks.filterNot { it.second }
                            saveTasks(tasks)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                    ) {
                        Text("Excluir Feitas")
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))

            }

        }

    }

}

private fun loadTasks(sharedPreferences: SharedPreferences): List<Pair<String, Boolean>> {
    val taskCount = sharedPreferences.getInt("task_count", 0)
    val loadedTasks = mutableListOf<Pair<String, Boolean>>()
    for (index in 0 until taskCount) {
        val taskText = sharedPreferences.getString("task_text_$index", "") ?: ""
        val isChecked = sharedPreferences.getBoolean("task_checked_$index", false)
        loadedTasks.add(Pair(taskText, isChecked))
    }
    return loadedTasks
}

@Preview
@Composable
fun AppPreview() {
    ListaDeTarefasTheme {
        App(
            sharedPreferences = LocalContext.current.getSharedPreferences(
                "task_preferences",
                Context.MODE_PRIVATE
            ),
            editor = LocalContext.current.getSharedPreferences(
                "task_preferences",
                Context.MODE_PRIVATE
            ).edit()
        )
    }
}
