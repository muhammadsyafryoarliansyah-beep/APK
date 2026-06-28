package com.example.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val type: String,
    val packageName: String,
    val packageDesc: String,
    val imageUris: String = "",
    val bahanBakuIds: String = "",
    val price: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bahan_baku")
data class BahanBaku(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val unit: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "product_bahan_baku_cross_ref",
    primaryKeys = ["productId", "bahanBakuId"],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["bahanBakuId"])
    ]
)
data class ProductBahanBakuCrossRef(
    val productId: Int,
    val bahanBakuId: Int
)

data class ProductWithBahanBaku(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProductBahanBakuCrossRef::class,
            parentColumn = "productId",
            entityColumn = "bahanBakuId"
        )
    )
    val bahanBakuList: List<BahanBaku>
)

data class ProductCost(
    val productId: Int,
    val productName: String,
    val totalCost: Double
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Delete
    suspend fun deleteProductOnly(product: Product)

    @Transaction
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getProductsWithBahanBaku(): Flow<List<ProductWithBahanBaku>>

    @Query("""
        SELECT p.id as productId, p.name as productName, COALESCE(SUM(b.price), 0.0) as totalCost
        FROM products p
        LEFT JOIN product_bahan_baku_cross_ref ref ON p.id = ref.productId
        LEFT JOIN bahan_baku b ON ref.bahanBakuId = b.id
        GROUP BY p.id
    """)
    fun getProductProductionCosts(): Flow<List<ProductCost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductBahanBakuCrossRefs(crossRefs: List<ProductBahanBakuCrossRef>)

    @Query("DELETE FROM product_bahan_baku_cross_ref WHERE productId = :productId")
    suspend fun deleteProductBahanBakuCrossRefs(productId: Int)

    @Query("DELETE FROM product_bahan_baku_cross_ref WHERE bahanBakuId = :bahanBakuId")
    suspend fun deleteCrossRefsByBahanBakuId(bahanBakuId: Int)

    @Transaction
    suspend fun insertProductWithBahanBaku(product: Product, bahanBakuIds: List<Int>) {
        val pid = insertProduct(product).toInt()
        val actualProductId = if (product.id != 0) product.id else pid
        
        deleteProductBahanBakuCrossRefs(actualProductId)
        val crossRefs = bahanBakuIds.map { ProductBahanBakuCrossRef(actualProductId, it) }
        if (crossRefs.isNotEmpty()) {
            insertProductBahanBakuCrossRefs(crossRefs)
        }
    }

    @Transaction
    suspend fun deleteProduct(product: Product) {
        deleteProductBahanBakuCrossRefs(product.id)
        deleteProductOnly(product)
    }
}

@Dao
interface BahanBakuDao {
    @Query("SELECT * FROM bahan_baku ORDER BY timestamp DESC")
    fun getAllBahanBaku(): Flow<List<BahanBaku>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBahanBaku(bahanBaku: BahanBaku)

    @Delete
    suspend fun deleteBahanBaku(bahanBaku: BahanBaku)
}

@Database(
    entities = [Product::class, BahanBaku::class, ProductBahanBakuCrossRef::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun bahanBakuDao(): BahanBakuDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create table cross reference
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `product_bahan_baku_cross_ref` (
                        `productId` INTEGER NOT NULL,
                        `bahanBakuId` INTEGER NOT NULL,
                        PRIMARY KEY(`productId`, `bahanBakuId`)
                    )
                """.trimIndent())
                
                // Create indices for optimization
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_product_bahan_baku_cross_ref_productId` ON `product_bahan_baku_cross_ref` (`productId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_product_bahan_baku_cross_ref_bahanBakuId` ON `product_bahan_baku_cross_ref` (`bahanBakuId`)")
                
                // Real-time data migration from CSV strings in existing products to the cross reference table
                val cursor = db.query("SELECT id, bahanBakuIds FROM products")
                if (cursor != null) {
                    try {
                        val idIndex = cursor.getColumnIndex("id")
                        val idsIndex = cursor.getColumnIndex("bahanBakuIds")
                        if (idIndex != -1 && idsIndex != -1) {
                            while (cursor.moveToNext()) {
                                val productId = cursor.getInt(idIndex)
                                val bahanBakuIdsStr = cursor.getString(idsIndex) ?: ""
                                if (bahanBakuIdsStr.isNotBlank()) {
                                    val ids = bahanBakuIdsStr.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        .mapNotNull { it.toIntOrNull() }
                                    for (bahanBakuId in ids) {
                                        db.execSQL(
                                            "INSERT OR IGNORE INTO product_bahan_baku_cross_ref (productId, bahanBakuId) VALUES (?, ?)",
                                            arrayOf(productId, bahanBakuId)
                                        )
                                    }
                                }
                            }
                        }
                    } finally {
                        cursor.close()
                    }
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN price TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

class ProductRepository(private val productDao: ProductDao, private val bahanBakuDao: BahanBakuDao) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allBahanBaku: Flow<List<BahanBaku>> = bahanBakuDao.getAllBahanBaku()
    
    val allProductsWithBahanBaku: Flow<List<ProductWithBahanBaku>> = productDao.getProductsWithBahanBaku()
    val productProductionCosts: Flow<List<ProductCost>> = productDao.getProductProductionCosts()

    suspend fun insert(product: Product) {
        val ids = product.bahanBakuIds.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
        productDao.insertProductWithBahanBaku(product, ids)
    }

    suspend fun delete(product: Product) = productDao.deleteProduct(product)
    
    suspend fun insertBahanBaku(bahanBaku: BahanBaku) = bahanBakuDao.insertBahanBaku(bahanBaku)
    
    suspend fun deleteBahanBaku(bahanBaku: BahanBaku) {
        productDao.deleteCrossRefsByBahanBakuId(bahanBaku.id)
        bahanBakuDao.deleteBahanBaku(bahanBaku)
    }
}
