package iterators

abstract class ProviderIterator(private val size: Int) : Iterator<Int> {

    abstract fun new(size: Int) : ProviderIterator

    override fun hasNext(): Boolean {
        return true
    }

}