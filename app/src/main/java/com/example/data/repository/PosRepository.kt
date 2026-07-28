package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ConfigEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InventoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.TableEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.CartItem
import com.example.domain.model.Category
import com.example.domain.model.CustomerModel
import com.example.domain.model.InventoryModel
import com.example.domain.model.MenuItemModel
import com.example.domain.model.OrderSummary
import com.example.domain.model.OrderStatus
import com.example.domain.model.OrderType
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PaymentStatus
import com.example.domain.model.RestaurantConfig
import com.example.domain.model.TableInfo
import com.example.domain.model.TableStatus
import com.example.domain.model.User
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PosRepository(private val db: AppDatabase) {

    // --- Users ---
    fun getAllUsers(): Flow<List<User>> = db.userDao().getAllUsers().map { list ->
        list.map {
            User(
                id = it.id,
                name = it.name,
                role = UserRole.valueOf(it.role),
                pin = it.pin,
                email = it.email,
                phone = it.phone
            )
        }
    }

    suspend fun getUserByPin(pin: String): User? {
        val entity = db.userDao().getUserByPin(pin) ?: return null
        return User(
            id = entity.id,
            name = entity.name,
            role = UserRole.valueOf(entity.role),
            pin = entity.pin,
            email = entity.email,
            phone = entity.phone
        )
    }

    suspend fun saveUser(user: User) {
        db.userDao().insertUser(
            UserEntity(
                id = user.id,
                name = user.name,
                role = user.role.name,
                pin = user.pin,
                email = user.email,
                phone = user.phone
            )
        )
    }

    // --- Tables ---
    fun getAllTables(): Flow<List<TableInfo>> = db.tableDao().getAllTables().map { list ->
        list.map {
            TableInfo(
                id = it.id,
                tableNumber = it.tableNumber,
                capacity = it.capacity,
                status = TableStatus.valueOf(it.status),
                activeOrderId = it.activeOrderId,
                section = it.section
            )
        }
    }

    suspend fun updateTableStatus(id: Int, status: TableStatus, activeOrderId: String?) {
        db.tableDao().updateTableStatus(id, status.name, activeOrderId)
    }

    suspend fun saveTable(table: TableInfo) {
        db.tableDao().insertTable(
            TableEntity(
                id = if (table.id == 0) 0 else table.id,
                tableNumber = table.tableNumber,
                capacity = table.capacity,
                status = table.status.name,
                activeOrderId = table.activeOrderId,
                section = table.section
            )
        )
    }

    // --- Categories & Menu ---
    fun getAllCategories(): Flow<List<Category>> = db.categoryDao().getAllCategories().map { list ->
        list.map { Category(id = it.id, name = it.name, iconName = it.iconName) }
    }

    suspend fun saveCategory(category: Category) {
        db.categoryDao().insertCategory(CategoryEntity(name = category.name, iconName = category.iconName))
    }

    fun getAllMenuItems(): Flow<List<MenuItemModel>> = db.menuItemDao().getAllMenuItems().map { list ->
        list.map {
            MenuItemModel(
                id = it.id,
                categoryId = it.categoryId,
                name = it.name,
                description = it.description,
                price = it.price,
                gstPercentage = it.gstPercentage,
                isAvailable = it.isAvailable,
                isVeg = it.isVeg,
                imageUrl = it.imageUrl
            )
        }
    }

    suspend fun saveMenuItem(item: MenuItemModel) {
        db.menuItemDao().insertMenuItem(
            MenuItemEntity(
                id = item.id,
                categoryId = item.categoryId,
                name = item.name,
                description = item.description,
                price = item.price,
                gstPercentage = item.gstPercentage,
                isAvailable = item.isAvailable,
                isVeg = item.isVeg,
                imageUrl = item.imageUrl
            )
        )
    }

    suspend fun deleteMenuItem(item: MenuItemModel) {
        db.menuItemDao().deleteMenuItem(
            MenuItemEntity(
                id = item.id,
                categoryId = item.categoryId,
                name = item.name,
                description = item.description,
                price = item.price
            )
        )
    }

    // --- Orders & KDS ---
    fun getAllOrders(): Flow<List<OrderSummary>> = db.orderDao().getAllOrders().map { list ->
        list.map { order ->
            val items = db.orderDao().getOrderItemsDirect(order.id).map { item ->
                CartItem(
                    menuItem = MenuItemModel(
                        id = item.menuItemId,
                        categoryId = 1,
                        name = item.menuItemName,
                        description = "",
                        price = item.unitPrice,
                        gstPercentage = item.gstPercentage
                    ),
                    quantity = item.quantity,
                    specialNotes = item.specialNotes,
                    status = try { OrderStatus.valueOf(item.status) } catch (_: Exception) { OrderStatus.PENDING }
                )
            }
            OrderSummary(
                id = order.id,
                orderNumber = order.orderNumber,
                tableNumber = order.tableNumber,
                orderType = OrderType.valueOf(order.orderType),
                customerName = order.customerName,
                customerPhone = order.customerPhone,
                status = OrderStatus.valueOf(order.status),
                subtotal = order.subtotal,
                discount = order.discount,
                taxAmount = order.taxAmount,
                totalAmount = order.totalAmount,
                paymentMethod = order.paymentMethod?.let { PaymentMethod.valueOf(it) },
                paymentStatus = PaymentStatus.valueOf(order.paymentStatus),
                createdAt = order.createdAt,
                items = items
            )
        }
    }

    fun getKitchenOrders(): Flow<List<OrderSummary>> = db.orderDao().getActiveKitchenOrders().map { list ->
        list.map { order ->
            val items = db.orderDao().getOrderItemsDirect(order.id).map { item ->
                CartItem(
                    menuItem = MenuItemModel(
                        id = item.menuItemId,
                        categoryId = 1,
                        name = item.menuItemName,
                        description = "",
                        price = item.unitPrice,
                        gstPercentage = item.gstPercentage
                    ),
                    quantity = item.quantity,
                    specialNotes = item.specialNotes,
                    status = try { OrderStatus.valueOf(item.status) } catch (_: Exception) { OrderStatus.PENDING }
                )
            }
            OrderSummary(
                id = order.id,
                orderNumber = order.orderNumber,
                tableNumber = order.tableNumber,
                orderType = OrderType.valueOf(order.orderType),
                customerName = order.customerName,
                customerPhone = order.customerPhone,
                status = OrderStatus.valueOf(order.status),
                subtotal = order.subtotal,
                discount = order.discount,
                taxAmount = order.taxAmount,
                totalAmount = order.totalAmount,
                paymentMethod = order.paymentMethod?.let { PaymentMethod.valueOf(it) },
                paymentStatus = PaymentStatus.valueOf(order.paymentStatus),
                createdAt = order.createdAt,
                items = items
            )
        }
    }

    suspend fun createOrder(
        orderId: String,
        orderNumber: String,
        tableNumber: String?,
        orderType: OrderType,
        customerName: String?,
        customerPhone: String?,
        cartItems: List<CartItem>,
        discount: Double = 0.0
    ): OrderSummary {
        val subtotal = cartItems.sumOf { it.subtotal }
        val taxAmount = cartItems.sumOf { it.gstAmount }
        val totalAmount = (subtotal - discount) + taxAmount

        val orderEntity = OrderEntity(
            id = orderId,
            orderNumber = orderNumber,
            tableNumber = tableNumber,
            orderType = orderType.name,
            customerName = customerName,
            customerPhone = customerPhone,
            status = OrderStatus.PENDING.name,
            subtotal = subtotal,
            discount = discount,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paymentMethod = null,
            paymentStatus = PaymentStatus.UNPAID.name,
            createdAt = System.currentTimeMillis()
        )

        val itemEntities = cartItems.map {
            OrderItemEntity(
                orderId = orderId,
                menuItemId = it.menuItem.id,
                menuItemName = it.menuItem.name,
                unitPrice = it.menuItem.price,
                quantity = it.quantity,
                gstPercentage = it.menuItem.gstPercentage,
                specialNotes = it.specialNotes,
                status = OrderStatus.PENDING.name
            )
        }

        db.orderDao().insertOrder(orderEntity)
        db.orderDao().insertOrderItems(itemEntities)

        return OrderSummary(
            id = orderId,
            orderNumber = orderNumber,
            tableNumber = tableNumber,
            orderType = orderType,
            customerName = customerName,
            customerPhone = customerPhone,
            status = OrderStatus.PENDING,
            subtotal = subtotal,
            discount = discount,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paymentStatus = PaymentStatus.UNPAID,
            items = cartItems
        )
    }

    suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus,
        paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
        paymentMethod: PaymentMethod? = null
    ) {
        db.orderDao().updateOrderStatus(orderId, status.name, paymentStatus.name, paymentMethod?.name)
    }

    suspend fun updateOrderItemStatus(itemId: Int, status: OrderStatus) {
        db.orderDao().updateOrderItemStatus(itemId, status.name)
    }

    // --- Customers ---
    fun getAllCustomers(): Flow<List<CustomerModel>> = db.customerDao().getAllCustomers().map { list ->
        list.map {
            CustomerModel(
                id = it.id,
                name = it.name,
                phone = it.phone,
                email = it.email,
                loyaltyPoints = it.loyaltyPoints,
                totalSpent = it.totalSpent
            )
        }
    }

    suspend fun saveCustomer(customer: CustomerModel) {
        db.customerDao().insertCustomer(
            CustomerEntity(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                email = customer.email,
                loyaltyPoints = customer.loyaltyPoints,
                totalSpent = customer.totalSpent
            )
        )
    }

    suspend fun deleteCustomer(customer: CustomerModel) {
        db.customerDao().deleteCustomer(
            CustomerEntity(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                email = customer.email
            )
        )
    }

    // --- Inventory ---
    fun getAllInventory(): Flow<List<InventoryModel>> = db.inventoryDao().getAllInventory().map { list ->
        list.map {
            InventoryModel(
                id = it.id,
                itemName = it.itemName,
                category = it.category,
                currentStock = it.currentStock,
                minThreshold = it.minThreshold,
                unit = it.unit,
                costPerUnit = it.costPerUnit
            )
        }
    }

    suspend fun saveInventory(item: InventoryModel) {
        db.inventoryDao().insertInventory(
            InventoryEntity(
                id = item.id,
                itemName = item.itemName,
                category = item.category,
                currentStock = item.currentStock,
                minThreshold = item.minThreshold,
                unit = item.unit,
                costPerUnit = item.costPerUnit
            )
        )
    }

    suspend fun deleteInventory(item: InventoryModel) {
        db.inventoryDao().deleteInventory(
            InventoryEntity(
                id = item.id,
                itemName = item.itemName,
                category = item.category,
                currentStock = item.currentStock,
                minThreshold = item.minThreshold,
                unit = item.unit,
                costPerUnit = item.costPerUnit
            )
        )
    }

    // --- Config ---
    fun getRestaurantConfig(): Flow<RestaurantConfig> = db.configDao().getConfig().map { config ->
        config?.let {
            RestaurantConfig(
                restaurantName = it.restaurantName,
                address = it.address,
                phone = it.phone,
                gstin = it.gstin,
                currencySymbol = it.currencySymbol,
                defaultTaxRate = it.defaultTaxRate
            )
        } ?: RestaurantConfig()
    }

    suspend fun saveRestaurantConfig(config: RestaurantConfig) {
        db.configDao().saveConfig(
            ConfigEntity(
                id = 1,
                restaurantName = config.restaurantName,
                address = config.address,
                phone = config.phone,
                gstin = config.gstin,
                currencySymbol = config.currencySymbol,
                defaultTaxRate = config.defaultTaxRate
            )
        )
    }
}
