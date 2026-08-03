package com.example.domain.model

enum class UserRole(val displayName: String) {
    ADMIN("Admin / Manager"),
    CASHIER("Cashier"),
    WAITER("Waiter / Captain"),
    KITCHEN_STAFF("Kitchen Staff"),
    CUSTOMER("Customer / Guest")
}

data class TableServiceRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tableNumber: String,
    val customerName: String,
    val serviceType: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TableStatus(val label: String) {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    RESERVED("Reserved"),
    BILLED("Billed")
}

enum class OrderStatus(val label: String) {
    PENDING("New Order"),
    PREPARING("In Kitchen"),
    READY("Ready to Serve"),
    SERVED("Served"),
    BILLED("Billed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class OrderType(val label: String) {
    DINE_IN("Dine-In"),
    TAKEAWAY("Takeaway"),
    DELIVERY("Delivery")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CARD("Credit/Debit Card"),
    UPI_QR("UPI / QR Code"),
    SPLIT("Split Payment")
}

enum class PaymentStatus {
    UNPAID,
    PAID,
    PARTIAL
}

data class User(
    val id: String,
    val name: String,
    val role: UserRole,
    val pin: String,
    val email: String,
    val phone: String
)

data class TableInfo(
    val id: Int,
    val tableNumber: String,
    val capacity: Int,
    val status: TableStatus,
    val activeOrderId: String? = null,
    val section: String = "Main Dining"
)

data class Category(
    val id: Int,
    val name: String,
    val iconName: String = "restaurant"
)

data class MenuItemModel(
    val id: Int,
    val categoryId: Int,
    val categoryName: String = "",
    val name: String,
    val description: String,
    val price: Double,
    val gstPercentage: Double = 5.0,
    val isAvailable: Boolean = true,
    val isVeg: Boolean = true,
    val imageUrl: String = ""
)

data class CartItem(
    val menuItem: MenuItemModel,
    var quantity: Int = 1,
    var specialNotes: String = "",
    var status: OrderStatus = OrderStatus.PENDING
) {
    val subtotal: Double
        get() = menuItem.price * quantity

    val gstAmount: Double
        get() = (subtotal * menuItem.gstPercentage) / 100.0

    val totalWithGst: Double
        get() = subtotal + gstAmount
}

data class OrderSummary(
    val id: String,
    val orderNumber: String,
    val tableNumber: String? = null,
    val orderType: OrderType,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val status: OrderStatus,
    val subtotal: Double,
    val discount: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<CartItem> = emptyList()
)

data class CustomerModel(
    val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val loyaltyPoints: Int = 0,
    val totalSpent: Double = 0.0
)

data class InventoryModel(
    val id: Int = 0,
    val itemName: String,
    val category: String,
    val currentStock: Double,
    val minThreshold: Double,
    val unit: String,
    val costPerUnit: Double
)

data class RestaurantConfig(
    val restaurantName: String = "Swad Sutra Fine Dining",
    val address: String = "123 Gourmet Street, Foodville, NY 10001",
    val phone: String = "+1 (555) 019-2831",
    val gstin: String = "27AABCU9603R1ZM",
    val currencySymbol: String = "₹",
    val defaultTaxRate: Double = 5.0
)
