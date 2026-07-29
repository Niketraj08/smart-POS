package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.repository.PosRepository
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
import com.example.domain.util.PdfInvoiceGenerator
import com.example.domain.util.ThermalPrinterSimulator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = PosRepository(db)

    init {
        DatabaseInitializer.populateIfEmpty(db)
    }

    // --- Active User & Authentication State ---
    private val _currentUser = MutableStateFlow<User?>(
        User("u1", "Sarah Jenkins (Manager)", UserRole.ADMIN, "1234", "sarah@smartpos.com", "+1 555-0101")
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun loginWithPin(pin: String): Boolean {
        var success = false
        viewModelScope.launch {
            val user = repository.getUserByPin(pin)
            if (user != null && com.example.domain.util.SecurePinStorage.verifyStaffPin(getApplication(), user.id, pin, defaultPin = user.pin)) {
                _currentUser.value = user
                _loginError.value = null
                success = true
            } else {
                _loginError.value = "Invalid Staff PIN. Verified by Encrypted Local Storage."
            }
        }
        return success
    }

    fun switchRole(role: UserRole) {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(role = role)
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- Active Navigation Screen ---
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Restaurant Configuration ---
    val restaurantConfig: StateFlow<RestaurantConfig> = repository.getRestaurantConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RestaurantConfig())

    fun updateConfig(config: RestaurantConfig) {
        viewModelScope.launch { repository.saveRestaurantConfig(config) }
    }

    // --- Tables ---
    val tables: StateFlow<List<TableInfo>> = repository.getAllTables()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTable = MutableStateFlow<TableInfo?>(null)
    val selectedTable: StateFlow<TableInfo?> = _selectedTable.asStateFlow()

    fun selectTable(table: TableInfo) {
        _selectedTable.value = table
    }

    fun updateTableStatus(tableId: Int, newStatus: TableStatus, activeOrderId: String? = null) {
        viewModelScope.launch { repository.updateTableStatus(tableId, newStatus, activeOrderId) }
    }

    fun saveTable(table: TableInfo) {
        viewModelScope.launch { repository.saveTable(table) }
    }

    // --- Menu & Categories ---
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val menuItems: StateFlow<List<MenuItemModel>> = repository.getAllMenuItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val filteredMenuItems: StateFlow<List<MenuItemModel>> = combine(
        menuItems, selectedCategoryId, searchQuery
    ) { items, catId, query ->
        items.filter { item ->
            (catId == null || item.categoryId == catId) &&
            (query.isBlank() || item.name.contains(query, ignoreCase = true) || item.description.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveMenuItem(item: MenuItemModel) {
        viewModelScope.launch { repository.saveMenuItem(item) }
    }

    fun deleteMenuItem(item: MenuItemModel) {
        viewModelScope.launch { repository.deleteMenuItem(item) }
    }

    // --- Active POS Cart Register ---
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _orderType = MutableStateFlow(OrderType.DINE_IN)
    val orderType: StateFlow<OrderType> = _orderType.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerModel?>(null)
    val selectedCustomer: StateFlow<CustomerModel?> = _selectedCustomer.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    fun setOrderType(type: OrderType) {
        _orderType.value = type
    }

    fun selectCustomer(customer: CustomerModel?) {
        _selectedCustomer.value = customer
    }

    fun setDiscount(discount: Double) {
        _discountAmount.value = discount
    }

    fun addToCart(item: MenuItemModel) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == item.id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(CartItem(menuItem = item, quantity = 1))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(item: CartItem, delta: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == item.menuItem.id }
        if (index >= 0) {
            val newQty = currentList[index].quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQty)
            }
            _cart.value = currentList
        }
    }

    fun updateCartItemNotes(item: CartItem, notes: String) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == item.menuItem.id }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(specialNotes = notes)
            _cart.value = currentList
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discountAmount.value = 0.0
    }

    // --- Orders & KDS ---
    val allOrders: StateFlow<List<OrderSummary>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kitchenOrders: StateFlow<List<OrderSummary>> = repository.getKitchenOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeBillingOrder = MutableStateFlow<OrderSummary?>(null)
    val activeBillingOrder: StateFlow<OrderSummary?> = _activeBillingOrder.asStateFlow()

    fun selectOrderForBilling(order: OrderSummary?) {
        _activeBillingOrder.value = order
    }

    fun sendOrderToKitchen(onComplete: (OrderSummary) -> Unit = {}) {
        val items = _cart.value
        if (items.isEmpty()) return

        val table = _selectedTable.value
        val customer = _selectedCustomer.value
        val discount = _discountAmount.value
        val type = _orderType.value

        val orderId = "ORD-${(1000..9999).random()}"
        val orderNum = orderId

        viewModelScope.launch {
            val createdOrder = repository.createOrder(
                orderId = orderId,
                orderNumber = orderNum,
                tableNumber = table?.tableNumber,
                orderType = type,
                customerName = customer?.name,
                customerPhone = customer?.phone,
                cartItems = items,
                discount = discount
            )

            // Update table status if dine in
            if (table != null && type == OrderType.DINE_IN) {
                repository.updateTableStatus(table.id, TableStatus.OCCUPIED, orderId)
            }

            clearCart()
            _activeBillingOrder.value = createdOrder
            onComplete(createdOrder)
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus, paymentStatus: PaymentStatus = PaymentStatus.UNPAID, method: PaymentMethod? = null) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, paymentStatus, method)
        }
    }

    fun updateOrderItemStatus(itemId: Int, status: OrderStatus) {
        viewModelScope.launch { repository.updateOrderItemStatus(itemId, status) }
    }

    // --- Printing & PDF Generation ---
    private val _printedReceiptText = MutableStateFlow<String?>(null)
    val printedReceiptText: StateFlow<String?> = _printedReceiptText.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    fun printThermalReceipt(order: OrderSummary, context: Context) {
        val text = ThermalPrinterSimulator.formatThermalReceiptText(order, restaurantConfig.value)
        _printedReceiptText.value = text
    }

    fun generatePdfInvoice(order: OrderSummary, context: Context) {
        val pdf = PdfInvoiceGenerator.generateInvoicePdf(context, order, restaurantConfig.value)
        _generatedPdfFile.value = pdf
    }

    fun generateAllOrdersPdf(orders: List<OrderSummary>, context: Context) {
        val pdf = PdfInvoiceGenerator.generateAllOrdersPdf(context, orders, restaurantConfig.value)
        _generatedPdfFile.value = pdf
    }

    fun clearPrintPreview() {
        _printedReceiptText.value = null
        _generatedPdfFile.value = null
    }

    // --- Customers ---
    val customers: StateFlow<List<CustomerModel>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCustomer(customer: CustomerModel) {
        viewModelScope.launch { repository.saveCustomer(customer) }
    }

    fun deleteCustomer(customer: CustomerModel) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }

    // --- Inventory ---
    val inventory: StateFlow<List<InventoryModel>> = repository.getAllInventory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveInventory(item: InventoryModel) {
        viewModelScope.launch { repository.saveInventory(item) }
    }

    fun deleteInventory(item: InventoryModel) {
        viewModelScope.launch { repository.deleteInventory(item) }
    }

    // --- Staff / Employee Management ---
    val users: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveUser(user: User) {
        viewModelScope.launch { repository.saveUser(user) }
    }
}
