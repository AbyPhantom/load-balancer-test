package loadbalancer

import exceptions.CollectionSizeException
import iterators.ProviderGenerator
import iterators.implementation.RandomGenerator
import provider.ProviderInterface

class LoadBalancer {

    private val capacity: Int = 10

    var generator: ProviderGenerator = RandomGenerator()

    var providers: Set<ProviderInterface> = LinkedHashSet<ProviderInterface>()
        private set

    fun register(providers: Set<ProviderInterface>) {
        if(providers.size > this.capacity) {
            throw CollectionSizeException("Maximum size of providers is ${this.capacity}")
        }
        this.providers = providers
    }

    fun get(): String {
        if(this.providers.isEmpty()) {
            throw CollectionSizeException("Number of providers must be greater than 0")
        }

        val nextKey = this.generator.next(this.providers.filter { it.active }.size)

        return this.providers.asSequence().filter { it.active }.elementAt(nextKey).get()
    }

    fun include(identifier: String) : Unit {
        val provider = this.providers.find { it.get() == identifier }
            ?: throw NoSuchElementException("Provider with identifier $identifier is not found")
        provider.active = true
    }

    fun exclude(identifier: String) : Unit {
        val provider = this.providers.find { it.get() == identifier }
            ?: throw NoSuchElementException("Provider with identifier $identifier is not found")
        provider.active = false
    }

}