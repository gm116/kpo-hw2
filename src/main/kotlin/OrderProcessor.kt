import java.util.concurrent.Executors

class OrderProcessor {
    private val executor = Executors.newFixedThreadPool(5)

    fun processOrder(order: Order) {
        var timeToWait = order.items.sumOf { it.complexity }
        executor.submit {
            synchronized(Database.lock) {
                order.status = OrderStatus.PROCESSING
                Thread.sleep(timeToWait * 60 * 1000) // тк в милисекундах sleep работает
                order.status = OrderStatus.READY
                Database.revenue += order.items.sumOf { it.price }
            }
        }
    }
}