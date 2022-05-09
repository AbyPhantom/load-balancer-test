import exceptions.CollectionSizeException
import iterators.implementation.RoundRobinGenerator
import loadbalancer.LoadBalancer
import provider.ProviderInterface
import provider.implementation.DefaultProvider
import java.util.stream.IntStream
import kotlin.streams.asSequence

fun main() {

    val loadBalancer : LoadBalancer = LoadBalancer(5000)

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
    IntStream.range(0, 10)
        .forEach { _ ->
            println("${loadBalancer.get()}")
        }

    println()

    loadBalancer.generator = RoundRobinGenerator()
    println("Getting round robin provider identifiers (20 iterations):")
    IntStream.range(0, 20).forEach { _ ->
        println("${loadBalancer.get()}")
    }

    println()

    println("Round Robin with 5 providers excluded")
    IntStream.range(0, 5).forEach { loadBalancer.exclude("provider_$it") }
    IntStream.range(0, 20).forEach { _ ->
        println("${loadBalancer.get()}")
    }

    println()

    println("Testing heartbeat check:")
    IntStream.range(0, 5).forEach { loadBalancer.providers.elementAt(it).alive = false }
    loadBalancer.startHeartbeat()
    Thread.sleep(10 * 1000)

}

fun loadProviders(loadBalancer: LoadBalancer, rangeStart: Int, rangeFinish: Int) {
    val namePrefix : String = "provider_"

    val providers : HashSet<ProviderInterface> = IntStream.range(rangeStart, rangeFinish).asSequence()
        .map { DefaultProvider(namePrefix+it) }
        .toHashSet();

    loadBalancer.register(providers)
}