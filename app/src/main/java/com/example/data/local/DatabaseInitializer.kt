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
            if (existingUsers.isNullOrEmpty()) {
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
            }

            val existingMenuItems = db.menuItemDao().getAllMenuItems().firstOrNull()
            if (existingMenuItems.isNullOrEmpty() || existingMenuItems.size < 50) {
                // 3. Categories
                val categories = listOf(
                CategoryEntity(1, "Starters & Appetizers", "starter"),
                CategoryEntity(2, "Soups & Salads", "soup"),
                CategoryEntity(3, "Main Course & Steaks", "main_course"),
                CategoryEntity(4, "Gourmet Pizzas", "pizza"),
                CategoryEntity(5, "Burgers, Sandwiches & Wraps", "burger"),
                CategoryEntity(6, "Pasta, Noodles & Risotto", "pasta"),
                CategoryEntity(7, "Indian Delights & Biryani", "indian"),
                CategoryEntity(8, "Asian & Sushi Specialities", "asian"),
                CategoryEntity(9, "Tacos & Mexican Feasts", "mexican"),
                CategoryEntity(10, "Desserts & Pastries", "dessert"),
                CategoryEntity(11, "Beverages, Coffee & Shakes", "beverage"),
                CategoryEntity(12, "Cocktails & Mocktails", "cocktail")
            )
            categories.forEach { db.categoryDao().insertCategory(it) }

            // 4. Menu Items (176 items)
            val menuItems = listOf(
                // Cat 1: Starters & Appetizers
                MenuItemEntity(1, 1, "Truffle Garlic Bread", "Crispy baguette topped with truffle butter & mozzarella", 8.99, 5.0, true, true),
                MenuItemEntity(2, 1, "Crispy Calamari Rings", "Served with smoked paprika aioli and lemon wedges", 12.50, 5.0, true, false),
                MenuItemEntity(3, 1, "Buffalo Chicken Wings", "Tossed in spicy cayenne glaze with blue cheese dip", 11.00, 5.0, true, false),
                MenuItemEntity(4, 1, "Loaded Jalapeño Poppers", "Stuffed with sharp cheddar & cream cheese, crispy breaded", 9.50, 5.0, true, true),
                MenuItemEntity(5, 1, "Spinach Artichoke Dip", "Warm cheesy spinach dip served with toasted pita chips", 10.50, 5.0, true, true),
                MenuItemEntity(6, 1, "Cheesy Garlic Sticks", "Pull-apart garlic bread with melted mozzarella & marinara", 7.99, 5.0, true, true),
                MenuItemEntity(7, 1, "BBQ Pulled Pork Sliders", "Three mini brioche buns with slow-cooked pork & slaw", 13.50, 5.0, true, false),
                MenuItemEntity(8, 1, "Crispy Mozzarella Bites", "Golden fried mozzarella balls with herb marinara sauce", 8.50, 5.0, true, true),
                MenuItemEntity(9, 1, "Paneer Tikka Skewers", "Char-grilled cottage cheese marinated in spiced yogurt", 11.99, 5.0, true, true),
                MenuItemEntity(10, 1, "Vegetable Spring Rolls", "Crispy golden rolls filled with glass noodles & veggies", 7.50, 5.0, true, true),
                MenuItemEntity(11, 1, "Honey Chilli Cauliflower", "Crispy florets tossed in sweet spicy Indo-Chinese glaze", 9.99, 5.0, true, true),
                MenuItemEntity(12, 1, "Loaded Nacho Supreme", "Tortilla chips with queso, jalapeños, salsa & guacamole", 12.99, 5.0, true, true),
                MenuItemEntity(13, 1, "Bruschetta Pomodoro", "Toasted sourdough with vine tomatoes, garlic & fresh basil", 8.25, 5.0, true, true),
                MenuItemEntity(14, 1, "Shrimp Tempura", "Light crispy batter fried jumbo prawns with sweet chili dip", 14.50, 5.0, true, false),
                MenuItemEntity(15, 1, "Chicken Satay Skewers", "Grilled marinated chicken skewers with peanut dipping sauce", 11.50, 5.0, true, false),

                // Cat 2: Soups & Salads
                MenuItemEntity(16, 2, "Classic Caesar Salad", "Crisp romaine, parmesan, garlic croutons & Caesar dressing", 9.99, 5.0, true, true),
                MenuItemEntity(17, 2, "Roasted Tomato Basil Soup", "Rich vine-ripened tomato soup with pesto drizzle", 7.50, 5.0, true, true),
                MenuItemEntity(18, 2, "Creamy Wild Mushroom Soup", "Earthy truffle infused mushroom soup with croutons", 8.50, 5.0, true, true),
                MenuItemEntity(19, 2, "Greek Feta & Olive Salad", "Cucumber, kalamata olives, feta cheese & lemon oregano vinaigrette", 10.50, 5.0, true, true),
                MenuItemEntity(20, 2, "Cobb Salad Supreme", "Grilled chicken, avocado, bacon, boiled egg & blue cheese", 13.99, 5.0, true, false),
                MenuItemEntity(21, 2, "Cream of Broccoli Soup", "Velvety broccoli soup topped with aged cheddar", 7.99, 5.0, true, true),
                MenuItemEntity(22, 2, "Asian Sesame Chicken Salad", "Shredded cabbage, mandarin oranges, crispy wontons & sesame glaze", 11.99, 5.0, true, false),
                MenuItemEntity(23, 2, "Minestrone Vegetable Soup", "Hearty Italian vegetable broth with ditalini pasta", 7.25, 5.0, true, true),
                MenuItemEntity(24, 2, "Quinoa Avocado Super Bowl", "Mixed greens, quinoa, roasted sweet potato, edamame & tahini", 12.50, 5.0, true, true),
                MenuItemEntity(25, 2, "French Onion Soup", "Rich caramelized onion broth topped with melted gruyère toast", 8.99, 5.0, true, true),
                MenuItemEntity(26, 2, "Spicy Tom Yum Goong", "Thai hot & sour broth with prawns, lemongrass & mushrooms", 11.50, 5.0, true, false),
                MenuItemEntity(27, 2, "Beetroot & Goat Cheese Salad", "Roasted beets, arugula, candied walnuts & balsamic glaze", 10.99, 5.0, true, true),
                MenuItemEntity(28, 2, "Thai Papaya Salad (Som Tum)", "Shredded green papaya, peanuts, chili & lime dressing", 9.50, 5.0, true, true),

                // Cat 3: Main Course & Steaks
                MenuItemEntity(29, 3, "Grilled Atlantic Salmon Fillet", "Pan-seared Atlantic salmon with wild rice & asparagus", 24.99, 5.0, true, false),
                MenuItemEntity(30, 3, "USDA Prime Ribeye Steak 10oz", "USDA Prime beef served with truffle fries & garlic butter", 32.00, 5.0, true, false),
                MenuItemEntity(31, 3, "Herb Crusted Lamb Chops", "Grilled lamb rack served with roasted garlic mashed potatoes", 29.50, 5.0, true, false),
                MenuItemEntity(32, 3, "Lemon Herb Roasted Chicken", "Half chicken slow-roasted with herbs, Served with pan gravy", 18.99, 5.0, true, false),
                MenuItemEntity(33, 3, "Braised Beef Short Ribs", "Tender slow-cooked ribs in red wine reduction over polenta", 27.50, 5.0, true, false),
                MenuItemEntity(34, 3, "Pork Ribs with Bourbon BBQ", "Fall-off-the-bone tender rack of ribs with smoky glaze & fries", 25.00, 5.0, true, false),
                MenuItemEntity(35, 3, "Pan-Seared Sea Bass", "Chilean sea bass over saffron risotto with lemon caper butter", 31.00, 5.0, true, false),
                MenuItemEntity(36, 3, "Grilled Chicken Mushroom Sauce", "Juicy breast with wild mushroom cream sauce & veggies", 17.50, 5.0, true, false),
                MenuItemEntity(37, 3, "New York Strip Steak 12oz", "Center-cut strip loin cooked to perfection with peppercorn sauce", 28.99, 5.0, true, false),
                MenuItemEntity(38, 3, "Stuffed Chicken Parmigiana", "Crispy breaded chicken topped with marinara & melted mozzarella", 19.50, 5.0, true, false),
                MenuItemEntity(39, 3, "Slow-Cooked Beef Stew", "Hearty chunks of beef, carrots, potatoes in rich onion gravy", 16.99, 5.0, true, false),
                MenuItemEntity(40, 3, "Roasted Pork Tenderloin", "Herb rub pork tenderloin served with apple cider reduction", 21.00, 5.0, true, false),
                MenuItemEntity(41, 3, "Grilled Mahi Mahi", "Pacific fish fillet with mango salsa and coconut rice", 22.50, 5.0, true, false),
                MenuItemEntity(42, 3, "Eggplant Parmesan Bake", "Layered roasted eggplant, marinara sauce & parmesan cheese", 15.99, 5.0, true, true),
                MenuItemEntity(43, 3, "Filet Mignon 8oz", "Tenderloin steak served with red wine demi-glace & asparagus", 34.99, 5.0, true, false),

                // Cat 4: Gourmet Pizzas
                MenuItemEntity(44, 4, "Margherita Supreme Pizza", "San Marzano tomato, fresh buffalo mozzarella & basil", 14.99, 5.0, true, true),
                MenuItemEntity(45, 4, "Smoky Pepperoni Pizza", "Double pepperoni with hot honey glaze and fresh oregano", 17.50, 5.0, true, false),
                MenuItemEntity(46, 4, "BBQ Chicken & Red Onion Pizza", "Grilled chicken, smoky BBQ sauce, red onions & cilantro", 16.99, 5.0, true, false),
                MenuItemEntity(47, 4, "Quattro Formaggi (4 Cheese)", "Mozzarella, gorgonzola, parmesan & ricotta cheese base", 16.50, 5.0, true, true),
                MenuItemEntity(48, 4, "Spicy Sausage & Jalapeño Pizza", "Italian fennel sausage, pickled jalapeños & chili flakes", 17.00, 5.0, true, false),
                MenuItemEntity(49, 4, "Truffle Wild Mushroom Pizza", "Garlic cream base, mixed mushrooms, truffle oil & arugula", 18.50, 5.0, true, true),
                MenuItemEntity(50, 4, "Hawaiian Ham & Pineapple Pizza", "Smoked ham, juicy pineapple chunks & mozzarella", 15.99, 5.0, true, false),
                MenuItemEntity(51, 4, "Veggie Lovers Feast Pizza", "Bell peppers, onions, mushrooms, olives & sweet corn", 14.50, 5.0, true, true),
                MenuItemEntity(52, 4, "Pesto Chicken & Sun-dried Tomato", "Basil pesto base, grilled chicken, sun-dried tomatoes & feta", 17.25, 5.0, true, false),
                MenuItemEntity(53, 4, "Buffalo Chicken Ranch Pizza", "Spicy buffalo chicken, creamy ranch drizzle & green onions", 16.99, 5.0, true, false),
                MenuItemEntity(54, 4, "Prosciutto & Arugula Pizza", "Crispy prosciutto di Parma, fresh arugula & shaved parmesan", 19.00, 5.0, true, false),
                MenuItemEntity(55, 4, "Supreme Meat Lovers Pizza", "Pepperoni, bacon, sausage, ham & ground beef", 18.99, 5.0, true, false),
                MenuItemEntity(56, 4, "Mediterranean Feta & Olive", "Garlic oil base, kalamata olives, spinach & feta cheese", 15.50, 5.0, true, true),
                MenuItemEntity(57, 4, "Spicy Paneer Tikka Pizza", "Tandoori paneer, onions, capsicum & spicy mint drizzle", 15.99, 5.0, true, true),
                MenuItemEntity(58, 4, "Garlic Shrimp Pizza", "Marinated prawns, garlic butter, fresh parsley & mozzarella", 19.50, 5.0, true, false),

                // Cat 5: Burgers, Sandwiches & Wraps
                MenuItemEntity(59, 5, "Smokey Bacon Cheeseburger", "Angus patty, sharp cheddar, crispy bacon & caramelized onion", 15.00, 5.0, true, false),
                MenuItemEntity(60, 5, "Plant-Based Beyond Burger", "Vegan patty, avocado, arugula & vegan chipotle mayo", 15.50, 5.0, true, true),
                MenuItemEntity(61, 5, "Crispy Fried Chicken Sandwich", "Buttermilk fried chicken, pickles, spicy slaw on brioche", 13.99, 5.0, true, false),
                MenuItemEntity(62, 5, "Double Smoked Smash Burger", "Two smash patties, double American cheese & secret house sauce", 14.50, 5.0, true, false),
                MenuItemEntity(63, 5, "Classic Club Sandwich", "Triple-decker with turkey, bacon, lettuce, tomato & mayo", 12.99, 5.0, true, false),
                MenuItemEntity(64, 5, "Philly Cheesesteak Sandwich", "Sliced ribeye, sautéed peppers, onions & melted provolone", 14.99, 5.0, true, false),
                MenuItemEntity(65, 5, "Grilled Chicken Caesar Wrap", "Grilled chicken, romaine, parmesan & Caesar dressing in tortilla", 11.50, 5.0, true, false),
                MenuItemEntity(66, 5, "Spicy Chipotle Veggie Wrap", "Black beans, corn, avocado, pepper jack & chipotle crema", 10.99, 5.0, true, true),
                MenuItemEntity(67, 5, "Falafel & Hummus Wrap", "Crispy falafel, creamy hummus, cucumbers & tahini drizzle", 10.50, 5.0, true, true),
                MenuItemEntity(68, 5, "Pulled Pork Sandwich", "Slow smoked pork tossed in tangy BBQ sauce on toasted bun", 13.50, 5.0, true, false),
                MenuItemEntity(69, 5, "Fish Fillet Burger tartar", "Crispy beer-battered cod, lettuce & tangy house tartar sauce", 12.99, 5.0, true, false),
                MenuItemEntity(70, 5, "Mushroom Swiss Burger", "Angus beef patty topped with sautéed wild mushrooms & Swiss", 14.25, 5.0, true, false),
                MenuItemEntity(71, 5, "Avocado Turkey BLT", "Sliced turkey, crispy bacon, lettuce, tomato & avocado on sourdough", 13.25, 5.0, true, false),
                MenuItemEntity(72, 5, "Buffalo Chicken Wrap", "Crispy chicken tenders tossed in buffalo sauce with blue cheese", 11.99, 5.0, true, false),
                MenuItemEntity(73, 5, "Teriyaki Chicken Burger", "Grilled chicken breast, grilled pineapple & teriyaki glaze", 13.50, 5.0, true, false),

                // Cat 6: Pasta, Noodles & Risotto
                MenuItemEntity(74, 6, "Creamy Fettuccine Alfredo", "Handmade pasta in rich Parmesan cream sauce with herbs", 16.50, 5.0, true, true),
                MenuItemEntity(75, 6, "Classic Spaghetti Bolognese", "Traditional slow-simmered beef ragù over spaghetti", 15.99, 5.0, true, false),
                MenuItemEntity(76, 6, "Penne Arrabbiata", "Penne tossed in spicy garlic tomato sauce with fresh parsley", 13.50, 5.0, true, true),
                MenuItemEntity(77, 6, "Seafood Marinara Pasta", "Shrimp, mussels & calamari in garlic white wine tomato sauce", 21.99, 5.0, true, false),
                MenuItemEntity(78, 6, "Truffle Wild Mushroom Risotto", "Arborio rice cooked in mushroom broth with truffle oil", 18.50, 5.0, true, true),
                MenuItemEntity(79, 6, "Lobster Ravioli Pink Sauce", "House ravioli filled with Maine lobster in creamy tomato sauce", 23.50, 5.0, true, false),
                MenuItemEntity(80, 6, "Creamy Pesto Penne", "Penne pasta with basil pesto, toasted pine nuts & cream", 14.99, 5.0, true, true),
                MenuItemEntity(81, 6, "Lasagna Bolognese", "Layered pasta with beef ragù, béchamel & melted mozzarella", 17.00, 5.0, true, false),
                MenuItemEntity(82, 6, "Carbonara with Crispy Bacon", "Spaghetti with egg yolk, pecorino cheese & pancetta", 16.25, 5.0, true, false),
                MenuItemEntity(83, 6, "Saffron Seafood Risotto", "Arborio rice infused with saffron, clams & king prawns", 22.00, 5.0, true, false),
                MenuItemEntity(84, 6, "Pad Thai Noodles with Shrimp", "Stir-fried rice noodles with bean sprouts, peanuts & prawns", 15.50, 5.0, true, false),
                MenuItemEntity(85, 6, "Hakka Garlic Noodles", "Wok-tossed noodles with garlic, bell peppers & soy sauce", 12.50, 5.0, true, true),
                MenuItemEntity(86, 6, "Spinach & Ricotta Tortellini", "Stuffed pasta pockets served in sage brown butter sauce", 15.99, 5.0, true, true),
                MenuItemEntity(87, 6, "Spicy Dan Dan Noodles", "Szechuan chili oil noodles with minced chicken & scallions", 14.50, 5.0, true, false),
                MenuItemEntity(88, 6, "Gnocchi Gorgonzola", "Pillow soft potato gnocchi in rich gorgonzola cream sauce", 16.00, 5.0, true, true),

                // Cat 7: Indian Delights & Biryani
                MenuItemEntity(89, 7, "Hyderabadi Chicken Biryani", "Fragrant basmati rice cooked with spiced chicken & aromatic herbs", 15.99, 5.0, true, false),
                MenuItemEntity(90, 7, "Mutton Dum Biryani", "Slow-cooked succulent mutton layered with saffron basmati rice", 18.50, 5.0, true, false),
                MenuItemEntity(91, 7, "Butter Chicken (Murgh Makhani)", "Tender chicken cooked in rich buttery tomato cream sauce", 16.50, 5.0, true, false),
                MenuItemEntity(92, 7, "Paneer Butter Masala", "Cottage cheese cubes simmered in creamy tomato gravy", 14.50, 5.0, true, true),
                MenuItemEntity(93, 7, "Dal Makhani Classic", "Slow-cooked black lentils enriched with butter & cream", 12.99, 5.0, true, true),
                MenuItemEntity(94, 7, "Chicken Tikka Masala", "Char-grilled chicken tikka in spicy onion tomato gravy", 16.00, 5.0, true, false),
                MenuItemEntity(95, 7, "Palak Paneer", "Fresh spinach puree with cottage cheese & aromatic spices", 13.99, 5.0, true, true),
                MenuItemEntity(96, 7, "Kadhai Paneer", "Paneer tossed with bell peppers & freshly ground kadhai spices", 14.25, 5.0, true, true),
                MenuItemEntity(97, 7, "Malai Kofta Curry", "Deep-fried potato & paneer dumplings in rich cashew gravy", 14.99, 5.0, true, true),
                MenuItemEntity(98, 7, "Vegetable Dum Biryani", "Basmati rice dum-cooked with mixed vegetables & mint", 13.50, 5.0, true, true),
                MenuItemEntity(99, 7, "Garlic Butter Naan", "Freshly baked Indian flatbread brushed with garlic butter", 3.50, 5.0, true, true),
                MenuItemEntity(100, 7, "Tandoori Roti Basket", "Assortment of whole wheat roti, naan & laccha paratha", 6.50, 5.0, true, true),
                MenuItemEntity(101, 7, "Chicken Chettinad", "Spicy South Indian chicken curry with toasted coconut spices", 15.50, 5.0, true, false),
                MenuItemEntity(102, 7, "Goan Fish Curry", "Mahi mahi cooked in tangy spicy coconut tamarind gravy", 17.50, 5.0, true, false),
                MenuItemEntity(103, 7, "Kashmiri Rogan Josh", "Tender lamb braised in yogurt, Kashmiri chilies & spices", 18.99, 5.0, true, false),

                // Cat 8: Asian & Sushi Specialities
                MenuItemEntity(104, 8, "Salmon Nigiri (4 pcs)", "Fresh Atlantic salmon over seasoned sushi rice", 12.00, 5.0, true, false),
                MenuItemEntity(105, 8, "Spicy Tuna Roll (8 pcs)", "Fresh tuna, spicy mayo, cucumber & sesame seeds", 13.50, 5.0, true, false),
                MenuItemEntity(106, 8, "California Crab Roll (8 pcs)", "Crab meat, avocado, cucumber & tobiko flying fish roe", 12.50, 5.0, true, false),
                MenuItemEntity(107, 8, "Dragon Roll Supreme (8 pcs)", "Eel, cucumber topped with sliced avocado & unagi sauce", 16.50, 5.0, true, false),
                MenuItemEntity(108, 8, "Veggie Avocado Roll (8 pcs)", "Avocado, cucumber, pickled radish & asparagus", 10.50, 5.0, true, true),
                MenuItemEntity(109, 8, "Chicken Teriyaki Bowl", "Grilled chicken breast over steamed rice with teriyaki glaze", 13.99, 5.0, true, false),
                MenuItemEntity(110, 8, "Beef Broccoli Stir Fry", "Sliced flank steak sautéed with broccoli in savory garlic sauce", 15.50, 5.0, true, false),
                MenuItemEntity(111, 8, "Kung Pao Chicken", "Diced chicken, peanuts, chili peppers & zesty sauce", 14.50, 5.0, true, false),
                MenuItemEntity(112, 8, "Sweet & Sour Pork", "Crispy pork bites with pineapple & bell peppers in sweet sauce", 14.00, 5.0, true, false),
                MenuItemEntity(113, 8, "Vegetable Dim Sum (6 pcs)", "Steamed dumplings filled with spinach, corn & mushrooms", 9.99, 5.0, true, true),
                MenuItemEntity(114, 8, "Chicken Momos Steamed (8 pcs)", "Nepalese style chicken dumplings served with spicy tomato dip", 11.50, 5.0, true, false),
                MenuItemEntity(115, 8, "Peking Duck Wrap", "Roasted duck, cucumber, scallions & hoisin sauce in thin pancake", 17.00, 5.0, true, false),
                MenuItemEntity(116, 8, "Korean Fried Chicken Bowl", "Crispy gochujang glazed chicken over steamed jasmine rice", 14.99, 5.0, true, false),
                MenuItemEntity(117, 8, "Tonkotsu Pork Ramen", "Rich pork bone broth, chashu pork belly, ajitama egg & bamboo", 16.00, 5.0, true, false),
                MenuItemEntity(118, 8, "Miso Tofu Ramen Bowl", "Savory miso broth with silken tofu, edamame & scallions", 13.50, 5.0, true, true),

                // Cat 9: Tacos & Mexican Feasts
                MenuItemEntity(119, 9, "Carne Asada Beef Tacos (3 pcs)", "Grilled sirloin, cilantro, chopped onion & salsa verde", 13.99, 5.0, true, false),
                MenuItemEntity(120, 9, "Baja Crispy Fish Tacos (3 pcs)", "Beer battered white fish, chipotle slaw & lime cream", 13.50, 5.0, true, false),
                MenuItemEntity(121, 9, "Chicken Tinga Burrito", "Shredded spiced chicken, rice, beans, cheese & salsa", 12.99, 5.0, true, false),
                MenuItemEntity(122, 9, "Cheesy Beef Quesadilla", "Flour tortilla loaded with seasoned ground beef & melted cheese", 11.99, 5.0, true, false),
                MenuItemEntity(123, 9, "Veggie Beans & Rice Bowl", "Pinto beans, cilantro lime rice, guacamole & pico de gallo", 10.50, 5.0, true, true),
                MenuItemEntity(124, 9, "Spicy Shrimp Tacos (3 pcs)", "Grilled prawns, pineapple salsa & spicy mayo in corn tortilla", 14.50, 5.0, true, false),
                MenuItemEntity(125, 9, "Pork Carnitas Burrito Bowl", "Slow cooked pulled pork, roasted corn, beans & salsa", 13.25, 5.0, true, false),
                MenuItemEntity(126, 9, "Loaded Chicken Chimichanga", "Deep-fried flour burrito stuffed with chicken & topped with queso", 13.99, 5.0, true, false),
                MenuItemEntity(127, 9, "Guacamole & Tortilla Chips", "Freshly smashed avocado dip served with warm corn chips", 7.99, 5.0, true, true),
                MenuItemEntity(128, 9, "Enchiladas Suizas", "Three chicken enchiladas baked in creamy tomatillo sauce", 14.00, 5.0, true, false),
                MenuItemEntity(129, 9, "Churros with Chocolate Dip", "Crispy cinnamon sugar churros served with warm chocolate sauce", 6.99, 5.0, true, true),
                MenuItemEntity(130, 9, "Elote Mexican Street Corn", "Grilled corn cob coated in cotija cheese, mayo & chili powder", 5.99, 5.0, true, true),
                MenuItemEntity(131, 9, "Cheesy Nacho Supreme Bowl", "Tortilla chips loaded with melted cheese, jalapenos & sour cream", 11.50, 5.0, true, true),

                // Cat 10: Desserts & Pastries
                MenuItemEntity(132, 10, "Classic Tiramisu", "Espresso-soaked ladyfingers with creamy mascarpone", 7.50, 5.0, true, true),
                MenuItemEntity(133, 10, "Warm Chocolate Lava Cake", "Molten chocolate center served with vanilla bean gelato", 8.50, 5.0, true, true),
                MenuItemEntity(134, 10, "New York Baked Cheesecake", "Rich creamy cheesecake with berry compote drizzle", 8.00, 5.0, true, true),
                MenuItemEntity(135, 10, "Apple Pie Vanilla Gelato", "Warm cinnamon spiced apple pie served with vanilla ice cream", 7.99, 5.0, true, true),
                MenuItemEntity(136, 10, "Crème Brûlée", "Classic French vanilla custard with caramelized sugar crust", 8.25, 5.0, true, true),
                MenuItemEntity(137, 10, "Molten Nutella Brownie", "Fudgy chocolate brownie stuffed with Nutella & walnuts", 7.50, 5.0, true, true),
                MenuItemEntity(138, 10, "Red Velvet Cake Slice", "Layered red velvet sponge with cream cheese frosting", 6.99, 5.0, true, true),
                MenuItemEntity(139, 10, "Salted Caramel Macarons (4 pcs)", "Crispy French almond meringue shells with caramel ganache", 8.50, 5.0, true, true),
                MenuItemEntity(140, 10, "Mango Passionfruit Panna Cotta", "Silky Italian cream dessert topped with tropical fruit glaze", 7.25, 5.0, true, true),
                MenuItemEntity(141, 10, "French Chocolate Mousse", "Dark Belgian chocolate mousse with whipped cream", 7.50, 5.0, true, true),
                MenuItemEntity(142, 10, "Churros Sundae", "Warm churro bites over chocolate & vanilla ice cream", 8.99, 5.0, true, true),
                MenuItemEntity(143, 10, "Pistachio Baklava (2 pcs)", "Flaky phyllo pastry stuffed with crushed pistachios & honey", 6.50, 5.0, true, true),
                MenuItemEntity(144, 10, "Gulab Jamun with Rabri", "Warm fried milk solids soaked in cardamom syrup with condensed milk", 5.99, 5.0, true, true),
                MenuItemEntity(145, 10, "Gelato Trio Bowl", "Choice of 3 scoops: Vanilla, Chocolate, Pistachio or Mango", 7.00, 5.0, true, true),
                MenuItemEntity(146, 10, "Chocolate Fudge Sundae", "Warm fudge, vanilla gelato, nuts & maraschino cherry", 7.99, 5.0, true, true),

                // Cat 11: Beverages, Coffee & Shakes
                MenuItemEntity(147, 11, "Iced Berry Hibiscus Tea", "Fresh brewed hibiscus tea with muddled berries", 4.50, 5.0, true, true),
                MenuItemEntity(148, 11, "Fresh Espresso Double Shot", "Rich dark roast double shot espresso coffee", 3.50, 5.0, true, true),
                MenuItemEntity(149, 11, "Cappuccino Italian Style", "Espresso topped with steamed milk & velvety foam", 4.99, 5.0, true, true),
                MenuItemEntity(150, 11, "Vanilla Iced Latte", "Espresso, cold milk, sweet vanilla syrup over ice", 5.25, 5.0, true, true),
                MenuItemEntity(151, 11, "Caramel Macchiato", "Layered espresso, steamed milk & buttery caramel drizzle", 5.50, 5.0, true, true),
                MenuItemEntity(152, 11, "Chocolate Fudge Milkshake", "Creamy chocolate ice cream blended with dark cocoa syrup", 6.50, 5.0, true, true),
                MenuItemEntity(153, 11, "Strawberry Banana Smoothie", "Fresh strawberries, banana, yogurt & honey blend", 6.25, 5.0, true, true),
                MenuItemEntity(154, 11, "Mango Passionfruit Smoothie", "Alphonso mango pulp blended with passionfruit juice & ice", 6.50, 5.0, true, true),
                MenuItemEntity(155, 11, "Matcha Green Tea Latte", "Japanese ceremonial matcha blended with warm almond milk", 5.75, 5.0, true, true),
                MenuItemEntity(156, 11, "Fresh Cold Brew Coffee", "Steeped 18 hours cold brew over ice", 4.75, 5.0, true, true),
                MenuItemEntity(157, 11, "Hot Chocolate Supreme", "Rich Belgian hot cocoa topped with marshmallows", 4.99, 5.0, true, true),
                MenuItemEntity(158, 11, "Fresh Squeezed Orange Juice", "100% natural cold pressed orange juice", 5.00, 5.0, true, true),
                MenuItemEntity(159, 11, "Sparkling Mineral Water (750ml)", "San Pellegrino chilled sparkling water", 4.50, 5.0, true, true),
                MenuItemEntity(160, 11, "Fresh Mint Lemonade", "Zesty freshly squeezed lemons with crushed mint ice", 4.25, 5.0, true, true),
                MenuItemEntity(161, 11, "Oreo Cookie Shake", "Blended crushed Oreos, vanilla ice cream & whipped cream", 6.99, 5.0, true, true),

                // Cat 12: Cocktails & Mocktails
                MenuItemEntity(162, 12, "Signature Espresso Martini", "Vodka, fresh espresso, & Kahlúa coffee liqueur", 11.50, 5.0, true, true),
                MenuItemEntity(163, 12, "Classic Mojito", "White rum, fresh lime juice, muddled mint & sparkling soda", 10.50, 5.0, true, true),
                MenuItemEntity(164, 12, "Smoked Old Fashioned", "Bourbon whiskey, bitters, orange peel & smoked wood chips", 13.00, 5.0, true, true),
                MenuItemEntity(165, 12, "Aperol Spritz", "Aperol, prosecco sparkling wine & soda water", 11.00, 5.0, true, true),
                MenuItemEntity(166, 12, "Margarita on the Rocks", "Tequila Blanco, triple sec & freshly squeezed lime juice", 10.99, 5.0, true, true),
                MenuItemEntity(167, 12, "Piña Colada Tropical", "White rum, coconut cream & pineapple juice blended with ice", 11.25, 5.0, true, true),
                MenuItemEntity(168, 12, "Virgin Mojito (Mocktail)", "Non-alcoholic fresh lime, muddled mint & ginger ale", 6.50, 5.0, true, true),
                MenuItemEntity(169, 12, "Passionfruit Sparkler (Mocktail)", "Passionfruit puree, lime juice & sparkling soda water", 6.99, 5.0, true, true),
                MenuItemEntity(170, 12, "Blue Lagoon Mocktail", "Blue curacao syrup, lemonade & lemon slice", 6.25, 5.0, true, true),
                MenuItemEntity(171, 12, "Cucumber Mint Cooler (Mocktail)", "Fresh cucumber juice, mint leaves & lemon tonic", 6.50, 5.0, true, true),
                MenuItemEntity(172, 12, "Cosmopolitan", "Vodka, triple sec, cranberry juice & fresh lime", 11.00, 5.0, true, true),
                MenuItemEntity(173, 12, "Whiskey Sour", "Bourbon, lemon juice, simple syrup & egg white foam", 12.00, 5.0, true, true),
                MenuItemEntity(174, 12, "Long Island Iced Tea", "Vodka, rum, gin, tequila, triple sec & cola splash", 13.50, 5.0, true, true),
                MenuItemEntity(175, 12, "Sangria Red Wine Jug", "Spanish red wine, brandy, chopped fresh apples & oranges", 22.00, 5.0, true, true),
                MenuItemEntity(176, 12, "Sunrise Citrus Breeze (Mocktail)", "Orange juice, grenadine & sparkling lemonade layer", 6.75, 5.0, true, true)
            )
            menuItems.forEach { db.menuItemDao().insertMenuItem(it) }
            }

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
                    restaurantName = "Swad Sutra Fine Dining",
                    address = "123 Gourmet Street, Foodville, NY 10001",
                    phone = "+1 (555) 019-2831",
                    gstin = "27AABCU9603R1ZM",
                    currencySymbol = "₹",
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

            val sampleOrder4 = OrderEntity(
                id = "ORD-1004",
                orderNumber = "ORD-1004",
                tableNumber = "T-02",
                orderType = "DINE_IN",
                customerName = "David Miller",
                customerPhone = "+1 555-7744",
                status = "SERVED",
                subtotal = 42.00,
                discount = 5.00,
                taxAmount = 1.85,
                totalAmount = 38.85,
                paymentMethod = "CARD",
                paymentStatus = "PAID",
                createdAt = System.currentTimeMillis() - 90 * 60 * 1000
            )
            val sampleItems4 = listOf(
                OrderItemEntity(0, "ORD-1004", 4, "Grilled Salmon Fillet", 24.99, 1, 5.0, "Extra lemon", "SERVED"),
                OrderItemEntity(0, "ORD-1004", 11, "Classic Molten Lava Cake", 9.99, 1, 5.0, "", "SERVED"),
                OrderItemEntity(0, "ORD-1004", 13, "Artisanal Mango Smoothie", 7.02, 1, 5.0, "", "SERVED")
            )

            val sampleOrder5 = OrderEntity(
                id = "ORD-1005",
                orderNumber = "ORD-1005",
                tableNumber = "T-04",
                orderType = "TAKEAWAY",
                customerName = "Ananya Sharma",
                customerPhone = "+1 555-2233",
                status = "CANCELLED",
                subtotal = 22.50,
                discount = 0.0,
                taxAmount = 1.13,
                totalAmount = 23.63,
                paymentMethod = null,
                paymentStatus = "UNPAID",
                createdAt = System.currentTimeMillis() - 150 * 60 * 1000
            )
            val sampleItems5 = listOf(
                OrderItemEntity(0, "ORD-1005", 2, "Creamy Wild Mushroom Soup", 9.50, 1, 5.0, "", "CANCELLED"),
                OrderItemEntity(0, "ORD-1005", 7, "Artisanal Margherita Pizza", 13.00, 1, 5.0, "", "CANCELLED")
            )

            db.orderDao().insertOrder(sampleOrder1)
            db.orderDao().insertOrderItems(sampleItems1)

            db.orderDao().insertOrder(sampleOrder2)
            db.orderDao().insertOrderItems(sampleItems2)

            db.orderDao().insertOrder(sampleOrder3)
            db.orderDao().insertOrderItems(sampleItems3)

            db.orderDao().insertOrder(sampleOrder4)
            db.orderDao().insertOrderItems(sampleItems4)

            db.orderDao().insertOrder(sampleOrder5)
            db.orderDao().insertOrderItems(sampleItems5)
        }
    }
}
