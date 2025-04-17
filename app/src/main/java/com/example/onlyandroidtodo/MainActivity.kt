// MainActivity.kt
package com.example.onlyandroidtodo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoApp()
        }
    }
}

// Data Class
@Serializable
data class TodoItem(
    val id: Int,
    val task: String,
    var isChecked: Boolean
)

// Save tsk showing some KTX extension error
fun saveTodoItems(context: Context, items: List<TodoItem>) {
    val sharedPref = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    val json = Json.encodeToString(items)
    editor.putString("todo_list", json)
    editor.apply()
}

// Load
fun loadTodoItems(context: Context): MutableList<TodoItem> {
    val sharedPref = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    val json = sharedPref.getString("todo_list", null)
    return if (json != null) {
        Json.decodeFromString(json)
    } else {
        mutableListOf()
    }
}

@Composable
fun TodoApp() {
    var isDarkTheme by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            TodoScreen(isDarkTheme) { isDarkTheme = !isDarkTheme }
        }
    }
}

@Composable
fun TodoScreen(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    val context = LocalContext.current
    var newTaskText by remember { mutableStateOf("") }
    val todoItems = remember { mutableStateListOf<TodoItem>().apply { addAll(loadTodoItems(context)) } }

    var isEditing by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<TodoItem?>(null) }

    // Automatically save items when changed
    LaunchedEffect(todoItems) {
        snapshotFlow { todoItems.toList() }.collect {
            saveTodoItems(context, it)
        }
    }

    // Editing Dialog
    if (isEditing && editingItem != null) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            title = { Text("Edit Task") },
            text = {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = { Text("Edit task...") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingItem?.let {
                            val index = todoItems.indexOfFirst { item -> item.id == it.id }
                            todoItems[index] = it.copy(task = newTaskText)
                        }
                        newTaskText = ""
                        isEditing = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { isEditing = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Todo List",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            IconButton(onClick = onThemeToggle) {
                Icon(
                    painter = painterResource(id = if (isDarkTheme) R.drawable.sun else R.drawable.moon),
                    contentDescription = "Toggle Theme"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter new task...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    if (newTaskText.isNotBlank()) {
                        val newItem = TodoItem(
                            id = (todoItems.maxOfOrNull { it.id } ?: 0) + 1,
                            task = newTaskText,
                            isChecked = false
                        )
                        todoItems.add(newItem)
                        newTaskText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add Task")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (todoItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks added yet!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(todoItems) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        TodoItemRow(
                            item = item,
                            onCheckedChange = { checked ->
                                val index = todoItems.indexOfFirst { it.id == item.id }
                                todoItems[index] = item.copy(isChecked = checked)
                            },
                            onDelete = {
                                todoItems.removeAll { it.id == item.id }
                            },
                            onEdit = {
                                editingItem = item
                                newTaskText = item.task
                                isEditing = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItemRow(
    item: TodoItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.task,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = if (item.isChecked) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (item.isChecked) {
                TextDecoration.LineThrough
            } else {
                TextDecoration.None
            }
        )

        IconButton(onClick = onEdit, modifier = Modifier.padding(start = 8.dp)) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit task", tint = MaterialTheme.colorScheme.primary)
        }

        IconButton(onClick = onDelete, modifier = Modifier.padding(start = 8.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete task", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TodoApp()
}

