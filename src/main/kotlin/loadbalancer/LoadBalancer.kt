package loadbalancer

import exceptions.CollectionSizeException
import iterators.ProviderGenerator
import iterators.implementation.RandomGenerator
import provider.ProviderInterface

class LoadBalancer {

    private val capacity: Int = 10

    var iterator: ProviderGenerator = RandomGenerator()

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

        val nextKey = this.iterator.next(this.providers.size)

        return this.providers.elementAt(nextKey).get()
    }

}