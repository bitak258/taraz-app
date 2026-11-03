package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ContactEntity::class,
        ProductEntity::class,
        UnitConversionEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        TransactionEntity::class,
        ChequeEntity::class,
        CategoryEntity::class,
        ShopSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TarazDatabase : RoomDatabase() {

    abstract fun tarazDao(): TarazDao

    companion object {
        @Volatile
        private var INSTANCE: TarazDatabase? = null

        fun getDatabase(context: Context): TarazDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TarazDatabase::class.java,
                    "taraz_database.db"
                )
                    .addCallback(TarazDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class TarazDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateSampleData(database.tarazDao())
                    }
                }
            }
        }

        suspend fun populateSampleData(dao: TarazDao) {
            dao.saveShopSettings(
                ShopSettingsEntity(
                    shopName = "فروشگاه مرکزی تراز",
                    shopPhone = "۰۲۱۸۸۸۸۸۸۸۸",
                    shopAddress = "تهران، بازار بزرگ",
                    sellerName = "مدیر تراز",
                    defaultTaxPercent = 0.0
                )
            )

            dao.insertCategory(CategoryEntity(name = "فروش روزانه", kind = "INCOME", colorHex = "#4CAF50"))
            dao.insertCategory(CategoryEntity(name = "خرید کالا", kind = "EXPENSE", colorHex = "#F44336"))
            dao.insertCategory(CategoryEntity(name = "هزینه اجاره و آب‌برق", kind = "EXPENSE", colorHex = "#FF9800"))

            dao.insertContact(ContactEntity(name = "علی مهدوی", phone = "۰۹۱۲۳۴۵۶۷۸۹", type = "CUSTOMER"))
            dao.insertContact(ContactEntity(name = "پخش مواد غذایی البرز", phone = "۰۲۱۷۷۷۷۷۷۷۷", type = "SUPPLIER"))
            dao.insertContact(ContactEntity(name = "محمد رضایی", phone = "۰۹۱۹۸۷۶۵۴۳۲", type = "CUSTOMER"))

            dao.insertProduct(ProductEntity(name = "برنج طارم هاشمی", baseUnit = "کیلو", purchasePrice = 65000L, salePrice = 85000L, stock = 250.0, minStock = 20.0))
            dao.insertProduct(ProductEntity(name = "روغن مایع ۸۱۰ گرمی", baseUnit = "عدد", purchasePrice = 45000L, salePrice = 54000L, stock = 120.0, minStock = 15.0))
            dao.insertProduct(ProductEntity(name = "چای سیاه ممتاز ۵۰۰ گرمی", baseUnit = "بسته", purchasePrice = 110000L, salePrice = 135000L, stock = 45.0, minStock = 10.0))
            dao.insertProduct(ProductEntity(name = "قند شکسته ۵ کیلویی", baseUnit = "بسته", purchasePrice = 180000L, salePrice = 210000L, stock = 30.0, minStock = 5.0))
        }
    }
}
