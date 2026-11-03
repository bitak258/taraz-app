package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.Fa
import com.example.viewmodel.TarazViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsContent(viewModel: TarazViewModel) {
    val invoices by viewModel.invoices.collectAsState()
    val products by viewModel.products.collectAsState()

    val totalSales = invoices.filter { it.status == "FINAL" && it.type == "SALE" }.sumOf { it.total }
    val totalProfit = invoices.filter { it.status == "FINAL" && it.type == "SALE" }.mapNotNull { it.profit }.sum()
    val inventoryValuation = products.sumOf { it.stock * it.purchasePrice }.toLong()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("گزارشات مالی و انبار") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "خلاصه وضعیت مالی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportRow(title = "کل فروش ثبت‌شده", value = Fa.money(totalSales))
                        Divider()
                        ReportRow(title = "مجموع سود ناخالص", value = Fa.money(totalProfit))
                        Divider()
                        ReportRow(title = "ارزش موجودی انبار (به قیمت خرید)", value = Fa.money(inventoryValuation))
                    }
                }
            }

            item {
                Text(
                    text = "گزارشات تکمیلی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedCard(
                    onClick = { /* PDF Export action */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("دریافت گزارش PDF", fontWeight = FontWeight.Bold)
                            Text("نسخه مشتری و نسخه فروشنده با تفکیک سود", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
