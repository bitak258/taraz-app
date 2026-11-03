package com.example.engine

import com.example.data.*

class AccountingEngine(private val dao: TarazDao) {

    suspend fun processInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        // Calculate subtotal, costTotal, profit, and line totals
        var calculatedSubtotal = 0L
        var calculatedCostTotal = 0L
        var hasUnknownCost = false

        val processedItems = items.map { item ->
            val lineGross = (item.unitPrice * item.quantity).toLong()
            val lineTotal = lineGross - item.discount
            calculatedSubtotal += lineTotal

            if (invoice.type == "SALE") {
                if (item.unitCost != null) {
                    calculatedCostTotal += (item.unitCost * item.quantity).toLong()
                } else {
                    hasUnknownCost = true
                }
            }
            item.copy(lineTotal = lineTotal)
        }

        val total = calculatedSubtotal - invoice.discount
        val profit = if (invoice.type == "SALE") {
            if (hasUnknownCost && calculatedCostTotal == 0L) null
            else calculatedSubtotal - calculatedCostTotal - invoice.discount
        } else null

        val finalInvoice = invoice.copy(
            subtotal = calculatedSubtotal,
            total = total,
            costTotal = if (invoice.type == "SALE") calculatedCostTotal else null,
            profit = profit,
            updatedAt = System.currentTimeMillis()
        )

        // Insert invoice
        val invoiceId = dao.insertInvoice(finalInvoice)

        // Insert items and update stock if FINAL
        val itemsWithId = processedItems.map { it.copy(invoiceId = invoiceId) }
        dao.insertInvoiceItems(itemsWithId)

        if (finalInvoice.status == "FINAL" && finalInvoice.type != "ESTIMATE") {
            val stockDeltaMultiplier = if (finalInvoice.type == "SALE") -1.0 else 1.0
            for (item in itemsWithId) {
                if (item.productId != null) {
                    dao.updateStock(item.productId, item.quantity * stockDeltaMultiplier)
                }
            }

            // Create cash transaction if paid amount > 0 and cash/card
            if (finalInvoice.paidAmount > 0 && (finalInvoice.paymentMethod == "CASH" || finalInvoice.paymentMethod == "CARD")) {
                val txType = if (finalInvoice.type == "SALE") "INCOME" else "EXPENSE"
                dao.insertTransaction(
                    TransactionEntity(
                        type = txType,
                        amount = finalInvoice.paidAmount,
                        invoiceId = invoiceId,
                        contactId = finalInvoice.contactId,
                        date = finalInvoice.date,
                        note = "ثبت خودکار فاکتور ${finalInvoice.number}"
                    )
                )
            }
        }

        return invoiceId
    }

    suspend fun voidInvoice(invoiceId: Long, reason: String) {
        val invoice = dao.getInvoiceById(invoiceId) ?: return
        if (invoice.status == "VOID") return

        val items = dao.getItemsForInvoice(invoiceId)

        // Reverse stock if it was FINAL
        if (invoice.status == "FINAL" && invoice.type != "ESTIMATE") {
            val stockReverseMultiplier = if (invoice.type == "SALE") 1.0 else -1.0
            for (item in items) {
                if (item.productId != null) {
                    dao.updateStock(item.productId, item.quantity * stockReverseMultiplier)
                }
            }
        }

        val updated = invoice.copy(
            status = "VOID",
            voidReason = reason,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateInvoice(updated)
    }
}
