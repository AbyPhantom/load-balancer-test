package iterators.implementation

import iterators.ProviderIterator
import java.util.concurrent.ThreadLocalRandom

class RandomIterator(private val size: Int) : ProviderIterator(size) {

    override fun new(size: Int): ProviderIterator {
        return RandomIterator(size)
    }

    override fun next(): Int {
        return ThreadLocalRandom.current().nextInt(0, this.size)
    }

}