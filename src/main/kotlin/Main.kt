import restaurant.Restaurant

fun main() {
    val restaurant = Restaurant()

    var exitProgram = false

    while (!exitProgram) {
        println("Выберите опцию:")
        println("1. Войти")
        println("2. Зарегистрироваться")
        println("0. Выйти из программы")

        when (readlnOrNull()?.toIntOrNull() ?: 0) {
            1 -> {
                println("Введите логин:")
                val username = readln()
                println("Введите пароль:")
                val password = readln()

                val user = restaurant.authenticateUser(username, password)
                if (user != null) {
                    val thread = Thread {
                        restaurant.start(user)
                    }
                    thread.start()

                    while (thread.isAlive) {
                        Thread.sleep(100)
                    }
                } else {
                    println("Ошибка аутентификации. Неправильные логин или пароль.")
                }
            }

            2 -> restaurant.registerUser()
            0 -> exitProgram = true
            else -> println("Неверный выбор.")
        }
    }
}
