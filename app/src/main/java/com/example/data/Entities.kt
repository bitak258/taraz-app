package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val type: String = "CUSTOMER", // CUSTOMER, SUPPLIER, BOTH
    val creditLimit: Long = 0, // 0 = unlimited
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val baseUnit: String = "عدد", // عدد، بسته، کیلو، متر، لیتر، شاخه، کارتن
    val unitsPerCarton: Double = 1.0,
    val purchasePrice: Long = 0,
    val salePrice: Long = 0,
    val stock: Double = 0.0,
    val minStock: Double = 0.0,
    val barcode: String? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "unit_conversions")
data class UnitConversionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val unit: String,
    val factorToBase: Double = 1.0
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val type: String = "SALE", // SALE, PURCHASE, ESTIMATE
    val contactId: Long? = null,
    val contactNameSnapshot: String? = null,
    val date: Long,
    val subtotal: Long = 0,
    val discount: Long = 0,
    val taxPercent: Double = 0.0,
    val total: Long = 0,
    val paidAmount: Long = 0,
    val paymentMethod: String = "CASH", // CASH, CREDIT, CARD, CHEQUE
    val status: String = "FINAL", // DRAFT, FINAL, VOID
    val voidReason: String? = null,
    val isEdited: Boolean = false,
    val note: String? = null,
    val costTotal: Long? = null,
    val profit: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long? = null,
    val productNameSnapshot: String,
    val unit: String = "عدد",
    val quantity: Double = 1.0,
    val cartons: Double = 0.0,
    val unitPrice: Long = 0,
    val unitCost: Long? = null,
    val discount: Long = 0,
    val lineTotal: Long = 0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = "INCOME", // INCOME, EXPENSE
    val amount: Long,
    val categoryId: Long? = null,
    val contactId: Long? = null,
    val invoiceId: Long? = null,
    val date: Long,
    val note: String? = null,
    val isOwnerCapital: Boolean = false // آورده/برداشت مالک
)

@Entity(tableName = "cheques")
data class ChequeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val direction: String = "RECEIVED", // RECEIVED, PAID
    val amount: Long,
    val dueDate: Long,
    val bank: String? = null,
    val serial: String? = null,
    val contactId: Long? = null,
    val status: String = "PENDING" // PENDING, CASHED, BOUNCED
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String = "INCOME", // INCOME, EXPENSE
    val colorHex: String = "#4CAF50"
)

@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val shopName: String = "فروشگاه من",
    val shopPhone: String? = null,
    val shopAddress: String? = null,
    val logoPath: String? = null,
    val defaultTaxPercent: Double = 0.0,
    val sellerName: String = "مدیر فروشگاه",
    val pinHash: String? = null
)
