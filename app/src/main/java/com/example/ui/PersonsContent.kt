package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContactEntity
import com.example.viewmodel.TarazViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonsContent(viewModel: TarazViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<ContactEntity?>(null) }

    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("CUSTOMER") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("اشخاص (مشتریان و تأمین‌کنندگان)") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingContact = null
                    nameInput = ""
                    phoneInput = ""
                    typeInput = "CUSTOMER"
                    showDialog = true
                },
                modifier = Modifier.testTag("fab_new_contact")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "افزودن شخص")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "فهرست اشخاص (${contacts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هیچ شخصی ثبت نشده است", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(contacts) { contact ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(contact.phone ?: "بدون شماره تلفن", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            AssistChip(
                                onClick = { },
                                label = { Text(if (contact.type == "CUSTOMER") "مشتری" else "تأمین‌کننده") }
                            )
                            IconButton(onClick = {
                                editingContact = contact
                                nameInput = contact.name
                                phoneInput = contact.phone ?: ""
                                typeInput = contact.type
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "ویرایش شخص", tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingContact == null) "افزودن شخص جدید" else "ویرایش شخص") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("نام و نام خانوادگی") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("شماره تلفن") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nameInput.isNotBlank()) {
                        val contact = ContactEntity(
                            id = editingContact?.id ?: 0L,
                            name = nameInput,
                            phone = phoneInput,
                            type = typeInput
                        )
                        viewModel.saveContact(contact)
                        showDialog = false
                    }
                }) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("انصراف") }
            }
        )
    }
}
