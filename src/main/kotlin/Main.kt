import exceptions.CollectionSizeException
import loadbalancer.LoadBalancer
import provider.ProviderInterface
import provider.implementation.DefaultProvider
import java.util.stream.IntStream
import kotlin.streams.asSequence

fun main() {

    val loadBalancer : LoadBalancer = LoadBalancer()
    //loadBalancer.iterator = RandomIterator(0)

    try {
        loadProviders(loadBalancer, 0, 12)
    } catch(e: CollectionSizeException) {
        println("Tried to load 12 providers. Failed with:")
        println("${e.message}")
    }

    println()

    loadProviders(loadBalancer, 0, 10)

    println("Load balancer provider count: ${loadBalancer.providers.size}")
    loadBalancer.providers.forEach { println("Provider ${it.get()} loaded.") }

    println()

    println("Getting random provider identifiers (10 iterations):")
    IntStream.range(0, 10).asSequence()
        .forEach { _ ->
            println("${loadBalancer.get()}")
        }

}

fun loadProviders(loadBalancer: LoadBalancer, rangeStart: Int, rangeFinish: Int) {
    val namePrefix : String = "provider_"

    val providers : HashSet<ProviderInterface> = IntStream.range(rangeStart, rangeFinish).asSequence()
        .map { DefaultProvider(namePrefix+it) }
        .toHashSet();

    loadBalancer.register(providers)
}