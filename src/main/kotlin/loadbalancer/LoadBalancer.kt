package loadbalancer

import dispatcher.Request
import dispatcher.RequestDispatcher
import dispatcher.Response
import exceptions.CollectionSizeException
import generators.ProviderGenerator
import generators.implementation.RandomGenerator
import providers.ProviderInterface
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.NoSuchElementException
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class LoadBalancer(private val checkPeriod : Long) {

    private val capacity: Int = 10

    private var checkForHeartbeat = true
    private val heartbeat: Thread = thread(start = false, isDaemon = true) {
        while(this.checkForHeartbeat) {
            this.check()
            Thread.sleep(this.checkPeriod)
        }
    }

    var generator: ProviderGenerator = RandomGenerator()

    var providers: Map<String, ProviderInterface> = HashMap<String, ProviderInterface>()
        private set

    private lateinit var requestQueue: BlockingQueue<Request>
    private lateinit var responseQueue: BlockingQueue<Response>

    private lateinit var requestDispatcher: RequestDispatcher

    fun register(providers: Map<String, ProviderInterface>) {
        if(providers.size > this.capacity) {
            throw CollectionSizeException("Maximum size of providers is ${this.capacity}")
        }
        this.providers = providers
        val requestCapacity: Int = this.getRequestCapacity()
        this.requestQueue = ArrayBlockingQueue<Request>(requestCapacity)
        this.responseQueue = ArrayBlockingQueue<Response>(requestCapacity)

        this.requestDispatcher = RequestDispatcher(requestCapacity, this.requestQueue, this.responseQueue)
        this.requestDispatcher.start()
    }

    fun get(): String {
        if(this.providers.isEmpty()) {
            throw CollectionSizeException("Number of providers must be greater than 0")
        }

        val nextKey = this.getNextIdentifier()

        return this.providers[nextKey]?.get()
            ?: throw NoSuchElementException("Provider with identifier $nextKey is not found")
    }

    fun request() {
        if(this.providers.isEmpty()) {
            throw CollectionSizeException("Number of providers must be greater than 0")
        }

        val nextKey = this.getNextIdentifier()

        val provider = this.providers[nextKey]
            ?: throw NoSuchElementException("Provider with identifier $nextKey is not found")

        val request = Request(Request.Command.GET, provider)

        this.requestDispatcher.submitRequest(request)
    }

    fun include(identifier: String) : Unit {
        val provider = this.providers[identifier]
            ?: throw NoSuchElementException("Provider with identifier $identifier is not found")
        if(!provider.active) {
            provider.active = true
            this.requestDispatcher.currentCapacity++
        }
    }

    fun exclude(identifier: String) : Unit {
        val provider = this.providers[identifier]
            ?: throw NoSuchElementException("Provider with identifier $identifier is not found")
        if(provider.active) {
            provider.active = false
            this.requestDispatcher.currentCapacity--
        }
    }

    private fun check() : Unit {
        println("Checking providers...")
        this.providers.forEach { (key, provider) ->
            run {
                if (provider.active && !provider.check()) {
                    this.exclude(key)
                    provider.heartbeatChecked = false
                    println("Provider $key not active. Excluding...")
                } else if (!provider.active && provider.check()) {
                    if(provider.heartbeatChecked) {
                        this.include(key)
                        println("Provider $key active. Including...")
                    } else {
                        provider.heartbeatChecked = true
                    }
                }
            }
        }
    }

    private fun getNextIdentifier() : String {
        if(this.providers.isEmpty()) {
            throw CollectionSizeException("Number of providers must be greater than 0")
        }

        val keys = this.providers.filter { (_, entry) -> entry.active }.keys
        val nextKeyIndex = this.generator.next(keys.size)

        return keys.elementAt(nextKeyIndex)
    }

    private fun getRequestCapacity(): Int {
        return providers.asSequence().sumOf { (_, entry) -> entry.capacity }
    }

    fun startHeartbeat() : Unit {
        this.checkForHeartbeat = true
        this.heartbeat.start()
    }

    fun shutdown() {
        this.checkForHeartbeat = false
        this.requestDispatcher.stop()
    }

}