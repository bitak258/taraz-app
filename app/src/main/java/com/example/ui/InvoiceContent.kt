package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.*
import com.example.util.Fa
import com.example.viewmodel.TarazViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceContent(viewModel: TarazViewModel) {
    var invoiceType by remember { mutableStateOf("SALE") } // SALE, PURCHASE, ESTIMATE
    val products by viewModel.products.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()

    var contactNameInput by remember { mutableStateOf("") }
    var selectedContactId by remember { mutableStateOf<Long?>(null) }
    var showContactDialog by remember { mutableStateOf(false) }

    var sellerNameInput by remember { mutableStateOf("") }
    LaunchedEffect(settings) {
        if (sellerNameInput.isBlank() && settings?.sellerName != null) {
            sellerNameInput = settings?.sellerName ?: ""
        }
    }

    var cartItems = remember { mutableStateListOf<InvoiceItemEntity>() }
    var discountInput by remember { mutableStateOf("0") }
    var paidInput by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var showProductDialog by remember { mutableStateOf(false) }

    // Item editing state
    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var editQtyInput by remember { mutableStateOf("") }
    var editPriceInput by remember { mutableStateOf("") }
    var editDiscountInput by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.lineTotal }
    val discount = discountInput.toLongOrNull() ?: 0L
    val total = (subtotal - discount).coerceAtLeast(0L)
    val paid = paidInput.toLongOrNull() ?: 0L
    val estimatedProfit = cartItems.sumOf { item ->
        val cost = item.unitCost ?: 0L
        ((item.unitPrice - cost) * item.quantity).toLong() - item.discount
    }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("ثبت فاکتور جدید") },
                actions = {
                    TextButton(onClick = {
                        invoiceType = when (invoiceType) {
                            "SALE" -> "PURCHASE"
                            "PURCHASE" -> "ESTIMATE"
                            else -> "SALE"
                        }
                    }) {
                        Text(
                            when (invoiceType) {
                                "SALE" -> "نوع: فروش"
                                "PURCHASE" -> "نوع: خرید"
                                else -> "نوع: پیش‌فاکتور"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card: Contact & Seller
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("طرف حساب", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = { showContactDialog = true }) {
                                Text("انتخاب از دفترچه")
                            }
                        }
                        OutlinedTextField(
                            value = contactNameInput,
                            onValueChange = { contactNameInput = it; selectedContactId = null },
                            label = { Text("نام مشتری / تأمین‌کننده (قابل تایپ)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = sellerNameInput,
                            onValueChange = { sellerNameInput = it },
                            label = { Text("نام فروشنده") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Items header & Add button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("اقلام فاکتور (${cartItems.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { showProductDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن کالا")
                    }
                }
            }

            if (cartItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هنوز کالایی به فاکتور اضافه نشده است", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                itemsIndexed(cartItems) { index, item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productNameSnapshot, fontWeight = FontWeight.Bold)
                                Text("تعداد: ${Fa.digits(item.quantity.toString())} ${item.unit} × ${Fa.money(item.unitPrice)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (invoiceType == "SALE") {
                                    val lineProfit = ((item.unitPrice - (item.unitCost ?: 0L)) * item.quantity).toLong() - item.discount
                                    Text("سود این سطر: ${Fa.money(lineProfit)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(Fa.money(item.lineTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                            Row {
                                IconButton(onClick = {
                                    editingItemIndex = index
                                    editQtyInput = item.quantity.toString()
                                    editPriceInput = item.unitPrice.toString()
                                    editDiscountInput = item.discount.toString()
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { cartItems.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // Summary & Payment & Seller Profit display
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("جمع جزء:")
                            Text(Fa.money(subtotal), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مبلغ کل:")
                            Text(Fa.money(total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        if (invoiceType == "SALE") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("سود کل فاکتور (مخصوص فروشنده):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text(Fa.money(estimatedProfit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = paidInput,
                            onValueChange = { paidInput = it },
                            label = { Text("مبلغ پرداختی (تومان)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (cartItems.isEmpty()) {
                                    snackbarMessage = "لطفاً حداقل یک کالا اضافه کنید."
                                    return@Button
                                }
                                val cName = if (contactNameInput.isNotBlank()) contactNameInput else "مشتری گذری"
                                val invoice = InvoiceEntity(
                                    number = "F-1405-${(1000..9999).random()}",
                                    type = invoiceType,
                                    contactId = selectedContactId,
                                    contactNameSnapshot = cName,
                                    date = System.currentTimeMillis(),
                                    subtotal = subtotal,
                                    discount = discount,
                                    total = total,
                                    paidAmount = paid,
                                    paymentMethod = paymentMethod,
                                    status = "FINAL",
                                    profit = if (invoiceType == "SALE") estimatedProfit else null
                                )
                                scope.launch {
                                    viewModel.processInvoice(invoice, cartItems) { id ->
                                        snackbarMessage = "فاکتور با موفقیت ثبت شد."
                                        cartItems.clear()
                                        contactNameInput = ""
                                        selectedContactId = null
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_submit_invoice")
                        ) {
                            Text("ثبت نهایی فاکتور")
                        }
                    }
                }
            }
        }
    }

    // Product selection dialog
    if (showProductDialog) {
        AlertDialog(
            onDismissRequest = { showProductDialog = false },
            title = { Text("انتخاب کالا") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(products) { prod ->
                        ListItem(
                            headlineContent = { Text(prod.name) },
                            supportingContent = { Text("موجودی: ${Fa.digits(prod.stock.toString())} ${prod.baseUnit} — ${Fa.money(prod.salePrice)}") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingContent = {
                                Button(onClick = {
                                    val price = if (invoiceType == "SALE") prod.salePrice else prod.purchasePrice
                                    cartItems.add(
                                        InvoiceItemEntity(
                                            invoiceId = 0,
                                            productId = prod.id,
                                            productNameSnapshot = prod.name,
                                            unit = prod.baseUnit,
                                            quantity = 1.0,
                                            unitPrice = price,
                                            unitCost = prod.purchasePrice,
                                            lineTotal = price
                                        )
                                    )
                                    showProductDialog = false
                                }) {
                                    Text("انتخاب")
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductDialog = false }) { Text("بستن") }
            }
        )
    }

    // Contact selection dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("انتخاب طرف حساب") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(contacts) { contact ->
                        ListItem(
                            headlineContent = { Text(contact.name) },
                            supportingContent = { Text(contact.phone ?: "بدون تلفن") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingContent = {
                                Button(onClick = {
                                    contactNameInput = contact.name
                                    selectedContactId = contact.id
                                    showContactDialog = false
                                }) {
                                    Text("انتخاب")
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) { Text("بستن") }
            }
        )
    }

    // Edit Item Dialog
    if (editingItemIndex != null) {
        val item = cartItems[editingItemIndex!!]
        AlertDialog(
            onDismissRequest = { editingItemIndex = null },
            title = { Text("ویرایش کالای فاکتور") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.productNameSnapshot, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editQtyInput,
                        onValueChange = { editQtyInput = it },
                        label = { Text("تعداد / مقدار") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPriceInput,
                        onValueChange = { editPriceInput = it },
                        label = { Text("قیمت واحد (تومان)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDiscountInput,
                        onValueChange = { editDiscountInput = it },
                        label = { Text("تخفیف سطر (تومان)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val q = editQtyInput.toDoubleOrNull() ?: item.quantity
                    val p = editPriceInput.toLongOrNull() ?: item.unitPrice
                    val d = editDiscountInput.toLongOrNull() ?: item.discount
                    val lineTot = ((p * q).toLong() - d).coerceAtLeast(0L)
                    cartItems[editingItemIndex!!] = item.copy(
                        quantity = q,
                        unitPrice = p,
                        discount = d,
                        lineTotal = lineTot
                    )
                    editingItemIndex = null
                }) {
                    Text("تایید")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItemIndex = null }) { Text("انصراف") }
            }
        )
    }
}
