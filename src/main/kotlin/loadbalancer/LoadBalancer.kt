package loadbalancer

import exceptions.CollectionSizeException
import iterators.ProviderIterator
import iterators.implementation.RandomIterator
import provider.ProviderInterface

class LoadBalancer {

    private val capacity: Int = 10

    var iterator: ProviderIterator = RandomIterator(0)

    var providers: Set<ProviderInterface> = LinkedHashSet<ProviderInterface>()
        private set

    fun register(providers: Set<ProviderInterface>) {
        if(providers.size > this.capacity) {
            throw CollectionSizeException("Maximum size of providers is ${this.capacity}")
        }
        this.providers = providers
        this.iterator = this.iterator.new(this.providers.size)
    }

    fun get(): String {
        if(this.providers.isEmpty()) {
            throw CollectionSizeException("Number of providers must be greater than 0")
        }

        val nextKey = this.iterator.next()

        return this.providers.elementAt(nextKey).get()
    }

}