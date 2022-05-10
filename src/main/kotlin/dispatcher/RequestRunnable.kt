package dispatcher

import java.util.concurrent.BlockingQueue

/**
 * Runnable used to handle the requests from request queue and store result in response queue
 *
 * @param requestQueue
 * @param responseQueue
 */
class RequestRunnable(private val requestQueue: BlockingQueue<Request>,
                      private val responseQueue: BlockingQueue<Response>) : Runnable {

    /**
     * Handles the request.
     *
     * On interrupt, it will store the message of interrupted request in the response queue.
     */
    override fun run() {
        try {
            val request: Request = this.requestQueue.take()
            val response: Response = when(request.command) {
                Request.Command.GET -> Response(request.provider.get())
                else -> Response("Invalid request")
            }
            this.responseQueue.add(response)
        } catch(e: InterruptedException) {
            val response: Response = Response("Interrupted request")
            this.responseQueue.add(response)
        }
    }

}