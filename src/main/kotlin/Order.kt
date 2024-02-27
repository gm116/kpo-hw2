data class Order(
    val id: String,
    val user: User,
    val items: MutableList<MenuItem>,
    var status: OrderStatus
)