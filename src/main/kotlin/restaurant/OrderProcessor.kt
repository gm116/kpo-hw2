package restaurant

import database.Database
import java.util.concurrent.Executors

class OrderProcessor {
    private val executor = Executors.newFixedThreadPool(5)

    fun processOrder(order: Order) {
        executor.submit {
            synchronized(Database.lock) {
                order.status = OrderStatus.PROCESSING
            }

            order.items.forEach { item ->
                println("Готовится блюдо: ${item.name}")
                Thread.sleep(item.complexity * 1000) // пока что в секундах лдя тестов
            }

            synchronized(Database.lock) {
                order.status = OrderStatus.READY
                println("Заказ готов!")

            }
        }
    }
}