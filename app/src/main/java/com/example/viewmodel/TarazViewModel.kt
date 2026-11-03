package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.engine.AccountingEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TarazViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TarazDatabase.getDatabase(application)
    private val dao = database.tarazDao()
    private val engine = AccountingEngine(dao)

    val contacts: StateFlow<List<ContactEntity>> = dao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val products: StateFlow<List<ProductEntity>> = dao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = dao.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cheques: StateFlow<List<ChequeEntity>> = dao.getAllCheques()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<CategoryEntity>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = dao.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _settings = MutableStateFlow<ShopSettingsEntity?>(null)
    val settings: StateFlow<ShopSettingsEntity?> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            _settings.value = dao.getShopSettings()
        }
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            dao.insertProduct(product)
        }
    }

    fun archiveProduct(product: ProductEntity) {
        viewModelScope.launch {
            dao.updateProduct(product.copy(isActive = false))
        }
    }

    fun saveContact(contact: ContactEntity) {
        viewModelScope.launch {
            dao.insertContact(contact)
        }
    }

    fun processInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = engine.processInvoice(invoice, items)
            onComplete(id)
        }
    }

    fun voidInvoice(invoiceId: Long, reason: String) {
        viewModelScope.launch {
            engine.voidInvoice(invoiceId, reason)
        }
    }

    fun addTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            dao.insertTransaction(tx)
        }
    }

    fun saveSettings(settings: ShopSettingsEntity) {
        viewModelScope.launch {
            dao.saveShopSettings(settings)
            _settings.value = settings
        }
    }

    suspend fun getInvoiceItems(invoiceId: Long): List<InvoiceItemEntity> {
        return dao.getItemsForInvoice(invoiceId)
    }
}
