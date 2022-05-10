package dispatcher

import loadbalancer.LoadBalancer
import java.util.concurrent.BlockingQueue

class RequestRunnable(private val requestQueue: BlockingQueue<Request>,
                      private val responseQueue: BlockingQueue<Response>) : Runnable {

    override fun run() {
        try {
            val request: Request = this.requestQueue.take()
            val response: Response = when(request.command) {
                Request.Command.GET -> Response(request.provider.get())
                else -> Response("Invalid request")
            }
            this.responseQueue.add(response)
            //println("Response queue size: ${responseQueue.size}")
        } catch(e: InterruptedException) {
            val response: Response = Response("Interrupted request")
            this.responseQueue.add(response)
        }
    }

}