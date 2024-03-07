package restaurant

data class Review(
    val order: Order,
    val rating: Int,
    val comment: String
)