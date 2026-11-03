package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.engine.AccountingEngine
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccountingEngineTest {

    private lateinit var database: TarazDatabase
    private lateinit var dao: TarazDao
    private lateinit var engine: AccountingEngine

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TarazDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.tarazDao()
        engine = AccountingEngine(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSaleInvoiceProcessingAndProfit() = runBlocking {
        val productId = dao.insertProduct(
            ProductEntity(
                name = "برنج",
                purchasePrice = 50000L,
                salePrice = 80000L,
                stock = 100.0
            )
        )

        val invoice = InvoiceEntity(
            number = "F-001",
            type = "SALE",
            date = System.currentTimeMillis(),
            paidAmount = 80000L,
            paymentMethod = "CASH"
        )

        val items = listOf(
            InvoiceItemEntity(
                invoiceId = 0,
                productId = productId,
                productNameSnapshot = "برنج",
                quantity = 1.0,
                unitPrice = 80000L,
                unitCost = 50000L
            )
        )

        val invoiceId = engine.processInvoice(invoice, items)
        val savedInvoice = dao.getInvoiceById(invoiceId)

        assertNotNull(savedInvoice)
        assertEquals(80000L, savedInvoice?.total)
        assertEquals(30000L, savedInvoice?.profit) // (80000 - 50000) * 1

        val product = dao.getProductById(productId)
        assertEquals(99.0, product?.stock ?: 0.0, 0.001)
    }

    @Test
    fun testVoidInvoiceRestoresStock() = runBlocking {
        val productId = dao.insertProduct(
            ProductEntity(
                name = "روغن",
                purchasePrice = 40000L,
                salePrice = 60000L,
                stock = 50.0
            )
        )

        val invoice = InvoiceEntity(
            number = "F-002",
            type = "SALE",
            date = System.currentTimeMillis(),
            paidAmount = 60000L
        )

        val items = listOf(
            InvoiceItemEntity(
                invoiceId = 0,
                productId = productId,
                productNameSnapshot = "روغن",
                quantity = 2.0,
                unitPrice = 60000L,
                unitCost = 40000L
            )
        )

        val invoiceId = engine.processInvoice(invoice, items)
        assertEquals(48.0, dao.getProductById(productId)?.stock ?: 0.0, 0.001)

        engine.voidInvoice(invoiceId, "تست ابطال")
        assertEquals(50.0, dao.getProductById(productId)?.stock ?: 0.0, 0.001)
        assertEquals("VOID", dao.getInvoiceById(invoiceId)?.status)
    }
}
