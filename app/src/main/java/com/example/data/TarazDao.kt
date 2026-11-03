package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TarazDao {

    // Contacts
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    // Products
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE isActive = 1 AND stock <= minStock ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = stock + :delta WHERE id = :productId")
    suspend fun updateStock(productId: Long, delta: Double)

    // Unit Conversions
    @Query("SELECT * FROM unit_conversions WHERE productId = :productId")
    suspend fun getUnitConversionsForProduct(productId: Long): List<UnitConversionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitConversion(conversion: UnitConversionEntity): Long

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY date DESC, id DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Transaction
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Long)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // Cheques
    @Query("SELECT * FROM cheques ORDER BY dueDate ASC")
    fun getAllCheques(): Flow<List<ChequeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheque(cheque: ChequeEntity): Long

    @Update
    suspend fun updateCheque(cheque: ChequeEntity)

    // Categories
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    // Shop Settings
    @Query("SELECT * FROM shop_settings WHERE id = 1")
    suspend fun getShopSettings(): ShopSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveShopSettings(settings: ShopSettingsEntity)
}
