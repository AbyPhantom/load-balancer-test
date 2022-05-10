package generators.implementation

import generators.ProviderGenerator
import java.util.concurrent.ThreadLocalRandom

class RandomGenerator : ProviderGenerator() {

    override fun next(size: Int): Int {
        return ThreadLocalRandom.current().nextInt(0, size)
    }

}