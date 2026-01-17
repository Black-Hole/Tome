package noise

/**
 * A seed value for random number generation.
 * 
 * @property value The underlying seed value
 */
@JvmInline
value class Seed(val value: Long) {
    companion object {
        /**
         * Creates a Seed from a Long value.
         */
        fun from(value: Long): Seed = Seed(value)
    }
}
