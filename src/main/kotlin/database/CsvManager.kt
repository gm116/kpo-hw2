package database

import restaurant.MenuItem
import restaurant.Review
import user.Admin
import user.User
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CsvManager {
    companion object {
        private const val USERS_FILE = "src/main/kotlin/database/users.csv"
        private const val ADMINS_FILE = "src/main/kotlin/database/admins.csv"
        private const val MENU_FILE = "src/main/kotlin/database/menu.csv"
        private const val REVENUE_FILE = "src/main/kotlin/database/revenue.csv"
        private const val REVIEWS_FILE = "src/main/kotlin/database/reviews.csv"

        private val dateFormat = SimpleDateFormat("dd-MM-yyyy")

        fun saveUser(user: User) {
            saveData(USERS_FILE, "${user.username};${user.password}")
        }

        fun saveReview(review: Review) {
            saveData(REVIEWS_FILE, "${review.orderId};${review.rating};${review.comment}")
        }


        fun saveMenuItem(menuItem: MenuItem) {
            saveData(MENU_FILE, "${menuItem.name};${menuItem.price};${menuItem.complexity}")
        }

        fun addRevenueEntry(amount: Double) {
            val currentDate = dateFormat.format(Date())

            val todayRevenueEntryIndex = Database.revenueEntries.indexOfFirst { it.first == currentDate }

            if (todayRevenueEntryIndex != -1) {
                val updatedAmount = Database.revenueEntries[todayRevenueEntryIndex].second + amount
                Database.revenueEntries[todayRevenueEntryIndex] = currentDate to updatedAmount
            } else {
                Database.revenueEntries.add(currentDate to amount)
            }

            saveData(REVENUE_FILE, Database.revenueEntries.joinToString("\n") { "${it.first};${it.second}" }, false)
        }

        fun readUsers(): List<User> {
            return readUsersFromCsv(USERS_FILE)
        }

        fun readAdmins(): List<Admin> {
            return readAdminsFromCsv(ADMINS_FILE)
        }

        fun readMenu(): List<MenuItem> {
            return readMenuFromCsv(MENU_FILE)
        }

        fun readRevenueEntries(): List<Pair<String, Double>> {
            return readRevenueFromCsv(REVENUE_FILE)
        }

        fun readReviews(): List<Review> {
            return readReviewsFromCsv(REVIEWS_FILE)
        }

        private fun saveData(filePath: String, data: String, append: Boolean = false) {
            val file = File(filePath)
            if (!file.exists()) {
                file.createNewFile()
            }

            file.appendText("$data\n")
        }

        private fun readUsersFromCsv(filePath: String): List<User> {
            return File(filePath).useLines { lines ->
                lines.map { line ->
                    val (username, password) = line.split(";")
                    User(username, password)
                }.toList()
            }
        }

        private fun readAdminsFromCsv(filePath: String): List<Admin> {
            return File(filePath).useLines { lines ->
                lines.map { line ->
                    val (username, password) = line.split(";")
                    Admin(username, password)
                }.toList()
            }
        }

        private fun readMenuFromCsv(filePath: String): List<MenuItem> {
            return File(filePath).useLines { lines ->
                lines.map { line ->
                    val (name, price, complexity) = line.split(";")
                    MenuItem(name, price.toDouble(), complexity.toLong())
                }.toList()
            }
        }

        private fun readRevenueFromCsv(filePath: String): List<Pair<String, Double>> {
            return File(filePath).useLines { lines ->
                lines.map { line ->
                    val (date, amount) = line.split(";")
                    date to amount.toDouble()
                }.toList()
            }
        }

        private fun readReviewsFromCsv(filePath: String): List<Review> {
            return File(filePath).useLines { lines ->
                lines.map { line ->
                    val (orderId, rating, comment) = line.split(";")
                    val review = Review(orderId, rating.toInt(), comment)
                    Database.reviews.add(review)
                    review
                }.toList()
            }
        }
        fun removeMenuItem(menuItem: MenuItem) {
            val tempFile = File("$MENU_FILE.temp")
            File(MENU_FILE).forEachLine {
                if (!it.startsWith("${menuItem.name};${menuItem.price};${menuItem.complexity}")) {
                    tempFile.appendText("$it\n")
                }
            }
            tempFile.renameTo(File(MENU_FILE))
        }

    }
}
