import database.CsvManager
import database.Database
import restaurant.Restaurant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


fun main() {

    Database.users.addAll(CsvManager.readUsers())
    Database.admins.addAll(CsvManager.readAdmins())
    Database.menu.addAll(CsvManager.readMenu())
    Database.reviews.addAll(CsvManager.readReviews())
    val revenueEntries = CsvManager.readRevenueEntries()
    Database.revenue = revenueEntries.sumOf { it.second }
    val restaurant = Restaurant()
    var exitProgram = false

    while (!exitProgram) {
        println("Выберите опцию:")
        println("1. Войти")
        println("2. Зарегистрироваться")
        println("0. Выйти из программы")

        val choice = readlnOrNull()?.toIntOrNull()


        if (choice != null) {
            when (choice) {
                1 -> {
                    println("Введите логин:")
                    val username = readln()
                    println("Введите пароль:")
                    val password = readln()

                    val user = restaurant.authenticateUser(username, password)
                    if (user != null) {
                        val executor = Executors.newSingleThreadExecutor()
                        executor.submit {
                            restaurant.start(user)
                        }

                        executor.shutdown()
                        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
                    } else {
                        println("Ошибка аутентификации. Неправильные логин или пароль.")
                    }
                }
                2 -> restaurant.registerUser()
                0 -> {
                    exitProgram = true
                    restaurant.orderProcessor.shutdown()
                }
                else -> println("Неверный выбор.")
            }
        } else {
            println("Некорректный ввод. Введите число.")
        }
    }
}
