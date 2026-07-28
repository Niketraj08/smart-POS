package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val pin: String,
    val email: String,
    val phone: String
)

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableNumber: String,
    val capacity: Int,
    val status: String,
    val activeOrderId: String? = null,
    val section: String = "Main Floor"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String = "restaurant"
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val gstPercentage: Double = 5.0,
    val isAvailable: Boolean = true,
    val isVeg: Boolean = true,
    val imageUrl: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val tableNumber: String? = null,
    val orderType: String,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val status: String,
    val subtotal: Double,
    val discount: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val paymentMethod: String? = null,
    val paymentStatus: String,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "Staff"
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: String,
    val menuItemId: Int,
    val menuItemName: String,
    val unitPrice: Double,
    val quantity: Int,
    val gstPercentage: Double,
    val specialNotes: String = "",
    val status: String = "PENDING"
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val loyaltyPoints: Int = 0,
    val totalSpent: Double = 0.0
)

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val category: String,
    val currentStock: Double,
    val minThreshold: Double,
    val unit: String,
    val costPerUnit: Double
)

@Entity(tableName = "restaurant_config")
data class ConfigEntity(
    @PrimaryKey val id: Int = 1,
    val restaurantName: String,
    val address: String,
    val phone: String,
    val gstin: String,
    val currencySymbol: String,
    val defaultTaxRate: Double
)
