package dispatcher

import java.util.concurrent.BlockingQueue

/**
 * Runnable used to handle the responses from response queue and show them in the console
 *
 * @param responseQueue
 */
class ResponseRunnable(private val responseQueue: BlockingQueue<Response>) : Runnable {

    /**
     * Handles the response.
     *
     * On interrupt, it will try to handle all the remaining responses before exiting.
     */
    override fun run() {
        try {
            while(true) {
                val response: Response = responseQueue.take()
                println("Response: ${response.message}")
            }
        } catch (e: InterruptedException) {
            println("Interrupted execution of response runnable")
            println("Response queue empty: ${this.responseQueue.isEmpty()}")
            while(!this.responseQueue.isEmpty()) {
                val response = responseQueue.take()
                println("Response: ${response.message}")
            }
        }
    }

}