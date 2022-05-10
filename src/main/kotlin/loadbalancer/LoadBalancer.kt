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

/**
 * A representation of a load balancer.
 *
 * Add providers to it using .register() method and call on the using .request()
 *
 * You can manually include and exclude providers after registering them.
 *
 * To use the heartbeat check use .startHeartbeat()
 *
 * @param checkPeriod period (in milliseconds) on which heartbeat will occur
 * @constructor Returns an instance of this class
 */
class LoadBalancer(private val checkPeriod : Long) {

    private val capacity: Int = 10

    private var checkForHeartbeat = true
    private val heartbeat: Thread = thread(start = false, isDaemon = true) {
        while(this.checkForHeartbeat) {
            this.check()
            Thread.sleep(this.checkPeriod)
        }
    }

    /**
     * Generator used to provide the next provider name of the get() calls.
     */
    var generator: ProviderGenerator = RandomGenerator()

    var providers: Map<String, ProviderInterface> = HashMap<String, ProviderInterface>()
        private set

    private lateinit var requestQueue: BlockingQueue<Request>
    private lateinit var responseQueue: BlockingQueue<Response>

    private lateinit var requestDispatcher: RequestDispatcher

    /**
     * Registers a Map<String, ProviderInterface> of providers to the LoadBalancer
     *
     * Always use this method before using the LoadBalancer
     *
     * @param providers
     */
    fun register(providers: Map<String, ProviderInterface>) {
        if(providers.size > this.capacity || providers.isEmpty()) {
            throw CollectionSizeException("Maximum size of providers is ${this.capacity} and there has to be at least one provider.")
        }
        this.providers = providers
        val requestCapacity: Int = this.getRequestCapacity()
        this.requestQueue = ArrayBlockingQueue<Request>(requestCapacity)
        this.responseQueue = ArrayBlockingQueue<Response>(requestCapacity)

        this.requestDispatcher = RequestDispatcher(requestCapacity, this.requestQueue, this.responseQueue)
        this.requestDispatcher.start()
    }

    /**
     * @return A unique identifier of the next provider as decided by generator.
     */
    fun get(): String {
        if(this.providers.isEmpty()) {
            throw CollectionSizeException("Number of providers must be greater than 0")
        }

        val nextKey = this.getNextIdentifier()

        return this.providers[nextKey]?.get()
            ?: throw NoSuchElementException("Provider with identifier $nextKey is not found")
    }

    /**
     * Send an asynchronous request to next provider as decided by generator.
     */
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

    /**
     * Include the provider to be used in the get and request methods.
     *
     * Manual setting will get overridden by using heartbeat.
     *
     * @param identifier A key of the providers Map
     */
    fun include(identifier: String) {
        val provider = this.providers[identifier]
            ?: throw NoSuchElementException("Provider with identifier $identifier is not found")
        if(!provider.active) {
            provider.active = true
            this.requestDispatcher.currentCapacity++
        }
    }

    /**
     * Exclude the provider from being used in the get and request methods.
     *
     * Manual setting will get overridden by using heartbeat.
     *
     * @param identifier A key of the providers Map
     */
    fun exclude(identifier: String) {
        val provider = this.providers[identifier]
            ?: throw NoSuchElementException("Provider with identifier $identifier is not found")
        if(provider.active) {
            provider.active = false
            this.requestDispatcher.currentCapacity--
        }
    }

    private fun check() {
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

    /**
     * Starts the heartbeat monitor. Using this will automatically include and exclude providers.
     */
    fun startHeartbeat() : Unit {
        this.checkForHeartbeat = true
        this.heartbeat.start()
    }

    /**
     * Stops the heartbeat monitor. Manual inclusion and exclusion will be possible again.
     */
    fun stopHeartbeat() {
        this.checkForHeartbeat = false
    }

    /**
     * Use when LoadBalancer is no longer needed. This stops heartbeat monitor and cleans up RequestDispatcher.
     */
    fun shutdown() {
        this.checkForHeartbeat = false
        this.requestDispatcher.stop()
    }

}