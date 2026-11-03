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
import com.example.data.ProductEntity
import com.example.util.Fa
import com.example.viewmodel.TarazViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryContent(viewModel: TarazViewModel) {
    val products by viewModel.products.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    var nameInput by remember { mutableStateOf("") }
    var baseUnitInput by remember { mutableStateOf("عدد") }
    var purchasePriceInput by remember { mutableStateOf("") }
    var salePriceInput by remember { mutableStateOf("") }
    var stockInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("انبار و کالاها") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    nameInput = ""
                    baseUnitInput = "عدد"
                    purchasePriceInput = ""
                    salePriceInput = ""
                    stockInput = ""
                    showDialog = true
                },
                modifier = Modifier.testTag("fab_new_product")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن کالا")
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
                    text = "فهرست کالاهای انبار (${products.size} کالا)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هیچ کالایی در انبار ثبت نشده است", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(products) { product ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("موجودی: ${Fa.digits(product.stock.toString())} ${product.baseUnit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Fa.money(product.salePrice), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("خرید: ${Fa.money(product.purchasePrice)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                editingProduct = product
                                nameInput = product.name
                                baseUnitInput = product.baseUnit
                                purchasePriceInput = product.purchasePrice.toString()
                                salePriceInput = product.salePrice.toString()
                                stockInput = product.stock.toString()
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "ویرایش کالا", tint = MaterialTheme.colorScheme.secondary)
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
            title = { Text(if (editingProduct == null) "افزودن کالای جدید" else "ویرایش کالا") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("نام کالا") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = baseUnitInput,
                        onValueChange = { baseUnitInput = it },
                        label = { Text("واحد (عدد، کیلو، بسته...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = purchasePriceInput,
                        onValueChange = { purchasePriceInput = it },
                        label = { Text("قیمت خرید (تومان)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = salePriceInput,
                        onValueChange = { salePriceInput = it },
                        label = { Text("قیمت فروش (تومان)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it },
                        label = { Text("موجودی") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nameInput.isNotBlank()) {
                        val product = ProductEntity(
                            id = editingProduct?.id ?: 0L,
                            name = nameInput,
                            baseUnit = baseUnitInput,
                            purchasePrice = purchasePriceInput.toLongOrNull() ?: 0L,
                            salePrice = salePriceInput.toLongOrNull() ?: 0L,
                            stock = stockInput.toDoubleOrNull() ?: 0.0,
                            isActive = true
                        )
                        viewModel.saveProduct(product)
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
