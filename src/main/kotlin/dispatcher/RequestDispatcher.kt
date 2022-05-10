package dispatcher

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

class RequestDispatcher(private val capacity: Int, private val requestQueue: BlockingQueue<Request>,
                        private val responseQueue: BlockingQueue<Response>) {

    private val executor = Executors.newFixedThreadPool(capacity)

    var currentCapacity: Int = capacity

    fun start() {
        println("Starting executor")
        val responseWorker = ResponseRunnable(this.responseQueue)
        this.executor.execute(responseWorker)
    }

    fun submitRequest(request: Request) {
        if(this.requestQueue.size >= this.currentCapacity) {
            this.responseQueue.put(Response("Rejected request"))
            return
        }
        try {
            this.requestQueue.put(request)
            val requestWorker = RequestRunnable(this.requestQueue, this.responseQueue)
            this.executor.execute(requestWorker)
        } catch (e: RejectedExecutionException) {
            this.responseQueue.put(Response("Rejected request"))
        }
    }

    fun stop() {
        this.executor.shutdownNow()
        this.executor.awaitTermination(5, TimeUnit.SECONDS)
    }

}