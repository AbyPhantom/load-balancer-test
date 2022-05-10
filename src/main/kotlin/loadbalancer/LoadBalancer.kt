package loadbalancer

import exceptions.CollectionSizeException
import iterators.ProviderGenerator
import iterators.implementation.RandomGenerator
import provider.ProviderInterface
import kotlin.concurrent.thread

class LoadBalancer(private val checkPeriod : Long) {

    private val capacity: Int = 10
    private val heartbeat: Thread = thread(start = false, isDaemon = true) {
        while(true) {
            this.check()
            Thread.sleep(this.checkPeriod)
        }
    }

    var generator: ProviderGenerator = RandomGenerator()

    var providers: Map<String, ProviderInterface> = HashMap<String, ProviderInterface>()
        private set

    fun register(providers: Map<String, ProviderInterface>) {
        if(providers.size > this.capacity) {
            throw CollectionSizeException("Maximum size of providers is ${this.capacity}")
        }
        this.providers = providers
    }

    fun get(): String {
        if(this.providers.isEmpty()) {
            throw CollectionSizeException("Number of providers must be greater than 0")
        }

        val keys = this.providers.filter { (_, entry) -> entry.active }.keys
        val nextKeyIndex = this.generator.next(keys.size)
        val nextKey = keys.elementAt(nextKeyIndex)

        return this.providers[nextKey]?.get()
            ?: throw NoSuchElementException("Provider with identifier $nextKey is not found")
    }

    fun include(identifier: String) : Unit {
        val provider = this.providers[identifier]
            ?: throw NoSuchElementException("Provider with identifier $identifier is not found")
        provider.active = true
    }

    fun exclude(identifier: String) : Unit {
        val provider = this.providers[identifier]
            ?: throw NoSuchElementException("Provider with identifier $identifier is not found")
        provider.active = false
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

    fun startHeartbeat() : Unit {
        this.heartbeat.start()
    }

}