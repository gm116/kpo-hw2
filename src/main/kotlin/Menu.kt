class Menu {
    private val menuItems = mutableListOf<MenuItem>()

    fun addMenuItem(menuItem: MenuItem) {
        menuItems.add(menuItem)
    }

    fun removeMenuItem(menuItem: MenuItem) {
        menuItems.remove(menuItem)
    }
}