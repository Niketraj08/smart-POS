package com.example.data.local

import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ConfigEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InventoryEntity
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.TableEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object DatabaseInitializer {

    fun populateIfEmpty(db: AppDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            val existingUsers = db.userDao().getAllUsers().firstOrNull()
            if (!existingUsers.isNullOrEmpty()) return@launch

            // 1. Initial Users
            val users = listOf(
                UserEntity("u1", "Sarah Jenkins (Manager)", "ADMIN", "1234", "sarah@smartpos.com", "+1 555-0101"),
                UserEntity("u2", "Alex Rivera (Cashier)", "CASHIER", "0000", "alex@smartpos.com", "+1 555-0102"),
                UserEntity("u3", "David Kim (Captain Waiter)", "WAITER", "1111", "david@smartpos.com", "+1 555-0103"),
                UserEntity("u4", "Chef Marco (Kitchen Head)", "KITCHEN_STAFF", "2222", "marco@smartpos.com", "+1 555-0104")
            )
            users.forEach { db.userDao().insertUser(it) }

            // 2. Tables
            val tables = listOf(
                TableEntity(1, "T-01", 2, "OCCUPIED", "ORD-1001", "Main Dining"),
                TableEntity(2, "T-02", 4, "AVAILABLE", null, "Main Dining"),
                TableEntity(3, "T-03", 4, "OCCUPIED", "ORD-1002", "Main Dining"),
                TableEntity(4, "T-04", 6, "RESERVED", null, "Main Dining"),
                TableEntity(5, "T-05", 2, "BILLED", "ORD-1003", "Patio"),
                TableEntity(6, "T-06", 8, "AVAILABLE", null, "Patio"),
                TableEntity(7, "VIP-1", 10, "AVAILABLE", null, "VIP Lounge"),
                TableEntity(8, "VIP-2", 6, "AVAILABLE", null, "VIP Lounge")
            )
            tables.forEach { db.tableDao().insertTable(it) }

            // 3. Categories
            val categories = listOf(
                CategoryEntity(1, "Starters & Appetizers", "starter"),
                CategoryEntity(2, "Main Course", "main_course"),
                CategoryEntity(3, "Gourmet Pizzas", "pizza"),
                CategoryEntity(4, "Artisanal Burgers", "burger"),
                CategoryEntity(5, "Desserts & Pastries", "dessert"),
                CategoryEntity(6, "Beverages & Cocktails", "beverage")
            )
            categories.forEach { db.categoryDao().insertCategory(it) }

            // 4. Menu Items
            val menuItems = listOf(
                MenuItemEntity(1, 1, "Truffle Garlic Bread", "Crispy baguette topped with truffle butter & mozzarella", 8.99, 5.0, true, true),
                MenuItemEntity(2, 1, "Crispy Calamari Rings", "Served with smoked paprika aioli and lemon wedges", 12.50, 5.0, true, false),
                MenuItemEntity(3, 1, "Buffalo Chicken Wings", "Tossed in spicy cayenne glaze with blue cheese dip", 11.00, 5.0, true, false),
                MenuItemEntity(4, 2, "Grilled Salmon Fillet", "Pan-seared Atlantic salmon with wild rice & asparagus", 24.99, 5.0, true, false),
                MenuItemEntity(5, 2, "Creamy Fettuccine Alfredo", "Handmade pasta in rich Parmesan cream sauce with herbs", 16.50, 5.0, true, true),
                MenuItemEntity(6, 2, "Prime Ribeye Steak 10oz", "USDA Prime beef served with truffle fries & garlic butter", 32.00, 5.0, true, false),
                MenuItemEntity(7, 3, "Margherita Supreme Pizza", "San Marzano tomato, fresh buffalo mozzarella & basil", 14.99, 5.0, true, true),
                MenuItemEntity(8, 3, "Smoky Pepperoni Pizza", "Double pepperoni with hot honey glaze and fresh oregano", 17.50, 5.0, true, false),
                MenuItemEntity(9, 4, "Smokey Bacon Cheeseburger", "Angus patty, sharp cheddar, crispy bacon & caramelized onion", 15.00, 5.0, true, false),
                MenuItemEntity(10, 4, "Plant-Based Beyond Burger", "Vegan patty, avocado, arugula & vegan chipotle mayo", 15.50, 5.0, true, true),
                MenuItemEntity(11, 5, "Classic Tiramisu", "Espresso-soaked ladyfingers with creamy mascarpone", 7.50, 5.0, true, true),
                MenuItemEntity(12, 5, "Warm Chocolate Lava Cake", "Molten chocolate center served with vanilla bean gelato", 8.50, 5.0, true, true),
                MenuItemEntity(13, 6, "Iced Berry Hibiscus Tea", "Fresh brewed hibiscus tea with muddled berries", 4.50, 5.0, true, true),
                MenuItemEntity(14, 6, "Signature Espresso Martini", "Vodka, fresh espresso, & Kahlúa coffee liqueur", 11.50, 5.0, true, true)
            )
            menuItems.forEach { db.menuItemDao().insertMenuItem(it) }

            // 5. Customers
            val customers = listOf(
                CustomerEntity(1, "Emily Watson", "+1 555-8821", "emily.w@example.com", 240, 320.50),
                CustomerEntity(2, "Michael Brown", "+1 555-3490", "m.brown@example.com", 110, 185.00),
                CustomerEntity(3, "Sophia Rodriguez", "+1 555-9012", "sophia.r@example.com", 450, 680.00)
            )
            customers.forEach { db.customerDao().insertCustomer(it) }

            // 6. Inventory Items
            val inventory = listOf(
                InventoryEntity(1, "Atlantic Salmon Fillets", "Seafood", 14.5, 5.0, "kg", 18.50),
                InventoryEntity(2, "USDA Prime Ribeye", "Meat", 22.0, 8.0, "kg", 24.00),
                InventoryEntity(3, "Mozzarella Cheese", "Dairy", 8.0, 10.0, "kg", 6.50),
                InventoryEntity(4, "San Marzano Tomatoes", "Produce", 35.0, 15.0, "can", 3.20),
                InventoryEntity(5, "Espresso Coffee Beans", "Beverage", 12.0, 4.0, "kg", 14.00)
            )
            inventory.forEach { db.inventoryDao().insertInventory(it) }

            // 7. Restaurant Configuration
            db.configDao().saveConfig(
                ConfigEntity(
                    id = 1,
                    restaurantName = "SmartPOS Bistro & Bar",
                    address = "123 Gourmet Street, Foodville, NY 10001",
                    phone = "+1 (555) 019-2831",
                    gstin = "27AABCU9603R1ZM",
                    currencySymbol = "$",
                    defaultTaxRate = 5.0
                )
            )

            // 8. Sample Active Orders for KDS & Billing
            val sampleOrder1 = OrderEntity(
                id = "ORD-1001",
                orderNumber = "ORD-1001",
                tableNumber = "T-01",
                orderType = "DINE_IN",
                customerName = "Emily Watson",
                customerPhone = "+1 555-8821",
                status = "PREPARING",
                subtotal = 33.98,
                discount = 0.0,
                taxAmount = 1.70,
                totalAmount = 35.68,
                paymentMethod = null,
                paymentStatus = "UNPAID",
                createdAt = System.currentTimeMillis() - 12 * 60 * 1000
            )
            val sampleItems1 = listOf(
                OrderItemEntity(0, "ORD-1001", 1, "Truffle Garlic Bread", 8.99, 1, 5.0, "Extra crispy", "READY"),
                OrderItemEntity(0, "ORD-1001", 4, "Grilled Salmon Fillet", 24.99, 1, 5.0, "Medium well", "PREPARING")
            )

            val sampleOrder2 = OrderEntity(
                id = "ORD-1002",
                orderNumber = "ORD-1002",
                tableNumber = "T-03",
                orderType = "DINE_IN",
                customerName = "Michael Brown",
                customerPhone = "+1 555-3490",
                status = "PENDING",
                subtotal = 49.50,
                discount = 2.0,
                taxAmount = 2.38,
                totalAmount = 49.88,
                paymentMethod = null,
                paymentStatus = "UNPAID",
                createdAt = System.currentTimeMillis() - 4 * 60 * 1000
            )
            val sampleItems2 = listOf(
                OrderItemEntity(0, "ORD-1002", 8, "Smoky Pepperoni Pizza", 17.50, 2, 5.0, "Less cheese", "PENDING"),
                OrderItemEntity(0, "ORD-1002", 14, "Signature Espresso Martini", 11.50, 1, 5.0, "", "PENDING")
            )

            val sampleOrder3 = OrderEntity(
                id = "ORD-1003",
                orderNumber = "ORD-1003",
                tableNumber = "T-05",
                orderType = "DINE_IN",
                customerName = "Sophia Rodriguez",
                customerPhone = "+1 555-9012",
                status = "BILLED",
                subtotal = 30.50,
                discount = 0.0,
                taxAmount = 1.53,
                totalAmount = 32.03,
                paymentMethod = "CASH",
                paymentStatus = "PAID",
                createdAt = System.currentTimeMillis() - 45 * 60 * 1000
            )
            val sampleItems3 = listOf(
                OrderItemEntity(0, "ORD-1003", 9, "Smokey Bacon Cheeseburger", 15.00, 1, 5.0, "", "SERVED"),
                OrderItemEntity(0, "ORD-1003", 10, "Plant-Based Beyond Burger", 15.50, 1, 5.0, "", "SERVED")
            )

            db.orderDao().insertOrder(sampleOrder1)
            db.orderDao().insertOrderItems(sampleItems1)

            db.orderDao().insertOrder(sampleOrder2)
            db.orderDao().insertOrderItems(sampleItems2)

            db.orderDao().insertOrder(sampleOrder3)
            db.orderDao().insertOrderItems(sampleItems3)
        }
    }
}
