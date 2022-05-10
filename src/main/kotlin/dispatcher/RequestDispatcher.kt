package dispatcher

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

/**
 * Provides the asynchronous functionality to the request/response cycle.
 *
 * It creates new threads per request which take the request from request queue
 * and put the response in response queue after completion.
 *
 * On start it creates one response thread which handles sending back the responses from response queue.
 *
 * When RequestDispatcher is no longer needed, use stop method.
 *
 * @param capacity Maximum number of threads
 * @param requestQueue Queue where the requests will be stored and taken from
 * @param responseQueue Queue where the responses will be stored and taken from
 */
class RequestDispatcher(private val capacity: Int, private val requestQueue: BlockingQueue<Request>,
                        private val responseQueue: BlockingQueue<Response>) {

    private val executor = Executors.newFixedThreadPool(capacity)

    var currentCapacity: Int = capacity

    private var isStarted: Boolean = false

    /**
     * Starts the executor and spawns one response thread.
     */
    fun start() {
        if(!this.isStarted) {
            println("Starting executor")
            val responseWorker = ResponseRunnable(this.responseQueue)
            this.executor.execute(responseWorker)
            this.isStarted = true
        }
    }

    /**
     * Submits a Request to the request queue
     * and spawns a thread to manage it
     *
     * @param request
     */
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

    /**
     * Shuts down the thread pool. It will allow all threads to try and finish the work.
     */
    fun stop() {
        this.isStarted = false
        this.executor.shutdownNow()
        this.executor.awaitTermination(5, TimeUnit.SECONDS)
    }

}