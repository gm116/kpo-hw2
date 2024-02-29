object Database {
    val users = mutableListOf<User>()
    val menu = mutableListOf<MenuItem>()
    val orders = mutableListOf<Order>()
    val reviews = mutableListOf<Review>()
    var revenue = 0.0

    val lock = Object()

    init {
        // Ручное добавление админа
        users.add(Admin("1", "admin", "psswd"))
    }
}