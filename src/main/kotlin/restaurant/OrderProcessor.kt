package restaurant

import database.Database
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OrderProcessor {
    private val executor = Executors.newFixedThreadPool(5)

    fun processOrder(order: Order) {
        executor.submit {
            synchronized(Database.lock) {
                order.status = OrderStatus.PROCESSING
            }

            order.items.forEach { item ->
                Thread.sleep(item.complexity * 1000) // пока что в секундах лдя тестов
            }

            synchronized(Database.lock) {
                order.status = OrderStatus.READY

            }
        }
    }

    fun shutdown() {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow()
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    println("Ошибка завершения потока")
                }
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}