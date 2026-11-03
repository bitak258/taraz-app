package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.TarazViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.Dashboard)
    object Invoice : Screen("invoice", "فاکتور", Icons.Default.ReceiptLong)
    object Inventory : Screen("inventory", "انبار", Icons.Default.Inventory2)
    object Persons : Screen("persons", "اشخاص", Icons.Default.People)
    object Reports : Screen("reports", "گزارشات", Icons.Default.BarChart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TarazViewModel) {
    var currentTab by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    val items = listOf(
        Screen.Dashboard,
        Screen.Invoice,
        Screen.Inventory,
        Screen.Persons,
        Screen.Reports
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(72.dp)
                    .testTag("bottom_navigation"),
                tonalElevation = 8.dp
            ) {
                items.forEach { screen ->
                    val selected = currentTab == screen
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        selected = selected,
                        onClick = { currentTab = screen },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                is Screen.Dashboard -> DashboardContent(viewModel, onNavigateToInvoice = { currentTab = Screen.Invoice })
                is Screen.Invoice -> InvoiceContent(viewModel)
                is Screen.Inventory -> InventoryContent(viewModel)
                is Screen.Persons -> PersonsContent(viewModel)
                is Screen.Reports -> ReportsContent(viewModel)
            }
        }
    }
}
