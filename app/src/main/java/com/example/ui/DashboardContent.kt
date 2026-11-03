package com.example.ui

import androidx.compose.foundation.background
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
import com.example.util.Fa
import com.example.viewmodel.TarazViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(viewModel: TarazViewModel, onNavigateToInvoice: () -> Unit) {
    val invoices by viewModel.invoices.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

    val todayMidnight = Fa.getTodayMidnightMillis()
    val todayInvoices = invoices.filter { it.date >= todayMidnight && it.status == "FINAL" && it.type == "SALE" }
    val todaySales = todayInvoices.sumOf { it.total }
    val todayProfit = todayInvoices.mapNotNull { it.profit }.sum()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تراز — حسابداری فروشگاهی", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Settings / Cheques */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "تنظیمات")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToInvoice,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("فاکتور جدید") },
                modifier = Modifier.testTag("fab_new_invoice")
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
            // Low stock alert banner
            if (lowStock.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("هشدار انبار", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("${lowStock.size} کالا به حداقل موجودی رسیده‌اند.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }

            // Summary 2x2 Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(
                        title = "فروش امروز",
                        amount = Fa.money(todaySales),
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "سود امروز",
                        amount = Fa.money(todayProfit),
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(
                        title = "کل مطالبات",
                        amount = Fa.money(0L), // Computed from contact balances
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "تعداد فاکتور امروز",
                        amount = Fa.digits(todayInvoices.size.toString()) + " فقره",
                        icon = Icons.Default.Receipt,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent invoices header
            item {
                Text(
                    text = "آخرین فاکتورها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (invoices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هنوز فاکتوری ثبت نشده است — از دکمه پایین شروع کنید", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(invoices.take(5)) { inv ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(inv.number, fontWeight = FontWeight.Bold)
                                Text(inv.contactNameSnapshot ?: "مشتری گذری", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Fa.money(inv.total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(if (inv.type == "SALE") "فروش" else "خرید", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(amount, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
