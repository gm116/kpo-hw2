package restaurant

import database.CsvManager
import database.Database
import user.Admin
import user.User
import java.util.*

class Restaurant {
    val orderProcessor = OrderProcessor()
    private val scanner = Scanner(System.`in`)

    fun authenticateUser(username: String, password: String): User? {
        synchronized(Database.lock) {
            val allUsers = Database.users + Database.admins
            return allUsers.find { it.username == username && it.password == password }
        }
    }

    private fun isUsernameTaken(username: String): Boolean {
        synchronized(Database.lock) {
            val allUsers = Database.users + Database.admins
            return allUsers.any { it.username == username }
        }
    }


    fun registerUser() {
        println("Регистрация нового пользователя:")
        println("Введите логин:")
        val username = scanner.next()
        if (isUsernameTaken(username)) {
            println("Пользователь с таким логином уже существует. Выберите другой логин.")
            return
        }
        println("Введите пароль:")
        val password = scanner.next()

        synchronized(Database.lock) {
            val newUser = User(username, password)
            Database.users.add(newUser)
            CsvManager.saveUser(newUser)
            println("Пользователь успешно зарегистрирован.")
        }
    }


    private fun displayOptions(user: User) {
        println("Выберите действие:")
        println("1. Посмотреть меню")
        println("2. Добавить блюдо в заказ")
        println("3. Посмотреть заказ")
        println("4. Отменить заказ")
        println("5. Оплатить заказ")
        println("0. Выход")

        if (user is Admin) {
            println("6. Посмотреть статистику")
            println("7. Добавить блюдо в меню")
            println("8. Удалить блюдо из меню")
        }
    }

    fun start(user: User) {
        var exit = false

        while (!exit) {
            displayOptions(user)
            when (scanner.nextInt()) {
                1 -> displayMenu()
                2 -> addToOrder(user)
                3 -> displayOrder(user)
                4 -> cancelOrder(user)
                5 -> payOrder(user)
                6 -> if (user is Admin) viewStatistics()
                7 -> if (user is Admin) addMenuItem()
                8 -> if (user is Admin) removeMenuItem()
                0 -> exit = true
                else -> println("Неверный ввод. Пожалуйста, введите корректное значение.")
            }
        }
    }

    private fun displayMenu() {
        synchronized(Database.lock) {
            println("Меню:")
            Database.menu.forEachIndexed { index, item ->
                println("${index + 1}. ${item.name} - ${item.price} $")
            }
        }
    }

    private fun addToOrder(user: User) {
        displayMenu()
        println("Выберите номер блюда для добавления в заказ (0 для завершения заказа):")

        val order = findOrCreateOrder(user)

        while (true) {
            val menuItemIndex = scanner.nextInt() - 1

            if (menuItemIndex == -1) {
                if (order.items.isNotEmpty()) {
                    order.status = OrderStatus.PROCESSING
                    println("Заказ сформирован. Ваш заказ готовится.")
                    orderProcessor.processOrder(order)
                    break
                } else {
                    println("Заказ пуст. Добавьте блюда в заказ.")
                }
            } else if (menuItemIndex in Database.menu.indices) {
                val menuItem = Database.menu[menuItemIndex]
                order.items.add(menuItem)
                println("${menuItem.name} добавлено в заказ.")
            } else {
                println("Неверный номер блюда.")
            }
        }
    }


    private fun findOrCreateOrder(user: User): Order {
        synchronized(Database.lock) {
            val existingOrder = Database.orders.find { it.user == user && it.status == OrderStatus.PROCESSING }
            return if (existingOrder != null) {
                existingOrder
            } else {
                val newOrder = Order(UUID.randomUUID().toString(), user, mutableListOf(), OrderStatus.ACCEPTED)
                Database.orders.add(newOrder)
                newOrder
            }
        }
    }


    private fun displayOrder(user: User): Boolean {
        synchronized(Database.lock) {
            val ordersForUser = Database.orders.filter { it.user == user }
            if (ordersForUser.isNotEmpty()) {
                ordersForUser.forEach { order ->
                    println("ID заказа: ${order.id}, Статус: ${order.status}")
                    order.items.forEachIndexed { index, item ->
                        println("${index + 1}. ${item.name} - ${item.price}")
                    }
                    println("------")
                    return true
                }
            } else {
                println("Заказы отсутствуют.")
            }
        }
        return false
    }

    private fun cancelOrder(user: User) {
        if (!displayOrder(user)) {
            return
        }
        println("Введите номер заказа для отмены:")
        val orderId = scanner.next()

        synchronized(Database.lock) {
            val order = Database.orders.find { it.id == orderId && it.user == user }
            if (order != null && order.status == OrderStatus.PROCESSING) {
                order.status = OrderStatus.CANCELED
                println("Заказ отменен.")
            } else {
                println("Неверный номер заказа или нельзя отменить заказ в текущем статусе.")
            }
        }
    }

    private fun payOrder(user: User) {
        if (!displayOrder(user)) {
            return
        }

        println("Введите ID заказа для оплаты:")
        val orderId = scanner.next()

        synchronized(Database.lock) {
            val order = Database.orders.find { it.id == orderId && it.user == user }
            if (order != null && order.status == OrderStatus.READY) {
                println("Заказ готов к оплате.")
                println("Сумма к оплате: ${order.items.sumOf { it.price }}")
                println("Хотите оплатить заказ? (1 - да, 2 - нет)")

                when (scanner.nextInt()) {
                    1 -> {
                        println("Заказ оплачен.")
                        CsvManager.addRevenueEntry(order.items.sumOf { it.price }) // Запись выручки в файл
                    }

                    2 -> println("Так нельзя. Вас все равно заставили заплатить.")
                    else -> println("Что-то пошло не так, вы все равно оплатили заказ")
                }
                order.status = OrderStatus.PAID
                leaveReview(order)
            } else {
                println("Неверный ID заказа или заказ не готов для оплаты.")
            }
        }
    }


    private fun leaveReview(order: Order) {
        println("Оставьте отзыв о заказе:")
        println("Оценка от 1 до 5:")
        val rating = scanner.nextInt()
        scanner.nextLine() // считываем символ новой строки

        println("Комментарий:")
        val comment = scanner.nextLine()

        synchronized(Database.lock) {
            val review = Review(order.id, rating, comment)
            Database.reviews.add(review)
            CsvManager.saveReview(review)
            println("Отзыв успешно добавлен.")
        }
    }


    private fun viewStatistics() {
        synchronized(Database.lock) {
            println("Статистика:")
            println("Выручка: ${Database.revenue}")
            println("Отзывы:")
            Database.reviews.forEachIndexed { index, review ->
                println("${index + 1}. Заказ ID: ${review.orderId}, Оценка: ${review.rating}, Комментарий: ${review.comment}")
            }
            val averageRating = if (Database.reviews.isNotEmpty()) {
                Database.reviews.map { it.rating }.average()
            } else {
                0.0
            }
            val formattedAverageRating = String.format("%.2f", averageRating)
            println("Средняя оценка: $formattedAverageRating")
        }
    }


    private fun addMenuItem() {
        println("Добавление нового блюда:")
        println("Введите название блюда:")
        val name = scanner.next()
        println("Введите цену блюда:")
        val price = scanner.nextDouble()
        println("Введите сложность выполнения блюда (время в минутах):")
        val complexity = scanner.nextLong()

        synchronized(Database.lock) {
            val newMenuItem = MenuItem(name, price, complexity)
            Database.menu.add(newMenuItem)
            CsvManager.saveMenuItem(newMenuItem)
            println("Блюдо добавлено в меню.")
        }
    }

    private fun removeMenuItem() {
        println("Удаление блюда из меню:")
        displayMenu()
        println("Введите номер блюда для удаления (0 для отмены):")

        val menuItemIndex = scanner.nextInt() - 1

        if (menuItemIndex == -1) {
            println("Отменено.")
            return
        }

        synchronized(Database.lock) {
            if (menuItemIndex in Database.menu.indices) {
                val removedMenuItem = Database.menu.removeAt(menuItemIndex)
                println("${removedMenuItem.name} удалено из меню.")
                CsvManager.removeMenuItem(removedMenuItem)
            } else {
                println("Неверный номер блюда.")
            }
        }
    }
}