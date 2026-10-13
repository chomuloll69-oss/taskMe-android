package com.bruh.taskMe

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

data class Todo(val id: Long, val title: String, val done: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TodoTheme { TodoScreen() } }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TodoTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= 31 ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TodoScreen() {
    val todos = remember { mutableStateListOf<Todo>() }
    var showDialog by remember { mutableStateOf(false) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("taskMe") },
                subtitle = { Text("${todos.count { !it.done }} left 😮‍💨") },
                scrollBehavior = scroll
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New task") }
            )
        }
    ) { padding ->
        if (todos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nothing here. Touch grass 🌱", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(todos, key = { it.id }) { todo ->
                    TodoRow(
                        todo = todo,
                        onToggle = {
                            val i = todos.indexOfFirst { it.id == todo.id }
                            if (i >= 0) todos[i] = todo.copy(done = !todo.done)
                        },
                        onDelete = { todos.removeAll { it.id == todo.id } },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (showDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New task") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text("What needs doing?") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (text.isNotBlank()) {
                        todos.add(Todo(System.currentTimeMillis(), text.trim()))
                        showDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun TodoRow(todo: Todo, onToggle: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val container by animateColorAsState(
        if (todo.done) MaterialTheme.colorScheme.surfaceContainerLow
        else MaterialTheme.colorScheme.secondaryContainer
    )
    ListItem(
        modifier = modifier.clip(RoundedCornerShape(24.dp)),
        colors = ListItemDefaults.colors(containerColor = container),
        leadingContent = { Checkbox(checked = todo.done, onCheckedChange = { onToggle() }) },
        headlineContent = {
            Text(
                todo.title,
                textDecoration = if (todo.done) TextDecoration.LineThrough else null
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Delete") }
        }
    )
}