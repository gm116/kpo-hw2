package database

import restaurant.MenuItem
import restaurant.Order
import restaurant.Review
import user.User

object Database {
    val admins = mutableListOf<User>()
    val users = mutableListOf<User>()
    val menu = mutableListOf<MenuItem>()
    val orders = mutableListOf<Order>()
    val reviews = mutableListOf<Review>()
    var revenue = 0.0

    val revenueEntries = mutableListOf<Pair<String, Double>>()

    val lock = Object()

}