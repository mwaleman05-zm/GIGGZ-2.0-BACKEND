package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JournalEntity
import com.example.ui.GiggzViewModel
import com.example.ui.components.showSafeToast
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalScreen(viewModel: GiggzViewModel) {
    val context = LocalContext.current
    val journals by viewModel.userJournals.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var editingNoteId by remember { mutableStateOf<Int?>(null) }

    // Colors matching Giggz primary style (warm green / slate / dark)
    val primaryGreen = Color(0xFF10B981)
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) Color.LightGray else Color.Gray
    val cardBg = if (isDark) Color(0xFF1E2228) else Color.White
    val cardBorder = if (isDark) Color(0xFF2D323A) else Color(0xFFE5E7EB)

    val filteredJournals = journals.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.content.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title Header Area
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "My Personal Journal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryGreen
                )
                Text(
                    text = "Write, save, and manage your private logs, tasks, or daily goals.",
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
        }

        // 2. Add / Edit Journal Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingNoteId == null) "New Entry" else "Modify Entry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Title", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryGreen,
                            unfocusedBorderColor = cardBorder,
                            focusedLabelColor = primaryGreen,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Content / Notes", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryGreen,
                            unfocusedBorderColor = cardBorder,
                            focusedLabelColor = primaryGreen,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (noteTitle.isBlank()) {
                                    context.showSafeToast("Please enter a title")
                                    return@Button
                                }
                                if (noteContent.isBlank()) {
                                    context.showSafeToast("Please write some content")
                                    return@Button
                                }
                                
                                if (editingNoteId == null) {
                                    viewModel.insertJournal(noteTitle, noteContent)
                                    context.showSafeToast("Note saved! 📝")
                                } else {
                                    viewModel.updateJournal(editingNoteId!!, noteTitle, noteContent)
                                    context.showSafeToast("Note updated! 📝")
                                }

                                // Reset state
                                noteTitle = ""
                                noteContent = ""
                                editingNoteId = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (editingNoteId == null) "Save Note" else "Update Note",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (editingNoteId != null) {
                            OutlinedButton(
                                onClick = {
                                    noteTitle = ""
                                    noteContent = ""
                                    editingNoteId = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, primaryGreen)
                            ) {
                                Text("Cancel", color = primaryGreen, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search through your journal...", fontSize = 12.sp, color = textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                )
            )
        }

        // 4. Notes List Items
        if (filteredJournals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "No journal entries yet." else "No matches found.",
                            color = textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(filteredJournals, key = { it.id }) { note ->
                val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(note.dateCreated))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            noteTitle = note.title
                            noteContent = note.content
                            editingNoteId = note.id
                        },
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        noteTitle = note.title
                                        noteContent = note.content
                                        editingNoteId = note.id
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Note",
                                        tint = primaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        viewModel.deleteJournal(note.id)
                                        context.showSafeToast("Entry deleted! 🗑️")
                                        if (editingNoteId == note.id) {
                                            noteTitle = ""
                                            noteContent = ""
                                            editingNoteId = null
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Note",
                                        tint = Color.Red.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note.content,
                            fontSize = 12.sp,
                            color = textSecondary,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
