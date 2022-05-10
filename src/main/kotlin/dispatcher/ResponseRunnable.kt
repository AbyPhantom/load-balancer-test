package dispatcher

import java.util.concurrent.BlockingQueue

class ResponseRunnable(private val responseQueue: BlockingQueue<Response>) : Runnable {

    override fun run() {
        try {
            while(true) {
                //println("Picking up response.")
                val response: Response = responseQueue.take()
                println("Response: ${response.message}")
            }
        } catch (e: InterruptedException) {
            println("Interrupted execution of response runnable")
            //Thread.sleep(2000)
            println("Response queue empty: ${this.responseQueue.isEmpty()}")
            while(!this.responseQueue.isEmpty()) {
                val response = responseQueue.take()
                println("Response: ${response.message}")
            }
        }
    }

}