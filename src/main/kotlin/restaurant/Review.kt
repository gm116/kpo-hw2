package restaurant

data class Review(
    val menuItem: MenuItem,
    val rating: Int,
    val comment: String
)