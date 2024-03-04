package database

import restaurant.MenuItem
import restaurant.Order
import restaurant.Review
import user.Admin
import user.User

object Database {
    val users = mutableListOf<User>()
    val menu = mutableListOf<MenuItem>()
    val orders = mutableListOf<Order>()
    val reviews = mutableListOf<Review>()
    var revenue = 0.0

    val lock = Object()

    init {
        // Ручное добавление для тестов
        users.add(Admin("1", "admin", "psswd"))
        users.add(User("2", "rita", "1234"))
        menu.add(MenuItem("taco", 30.0, 1))
        menu.add(MenuItem("pasta", 20.0, 2))
        menu.add(MenuItem("burger", 10.0, 3))
        menu.add(MenuItem("pizza", 50.0, 4))
    }
}