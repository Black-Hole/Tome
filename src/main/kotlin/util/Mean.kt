package util

/**
 * Calculates the arithmetic mean of Float values in an iterable.
 *
 * @return The mean value, or 0.0 if the iterable is empty
 */
fun Iterable<Float>.mean(): Float {
    var sum = 0.0f
    var count = 0
    
    for (value in this) {
        sum += value
        count++
    }
    
    return if (count > 0) {
        sum / count
    } else {
        0.0f
    }
}

/**
 * Calculates the arithmetic mean of Float values in a sequence.
 *
 * @return The mean value, or 0.0 if the sequence is empty
 */
fun Sequence<Float>.mean(): Float {
    var sum = 0.0f
    var count = 0
    
    for (value in this) {
        sum += value
        count++
    }
    
    return if (count > 0) {
        sum / count
    } else {
        0.0f
    }
}

/**
 * Calculates the arithmetic mean of Double values in an iterable.
 *
 * @return The mean value, or 0.0 if the iterable is empty
 */
fun Iterable<Double>.meanDouble(): Double {
    var sum = 0.0
    var count = 0
    
    for (value in this) {
        sum += value
        count++
    }
    
    return if (count > 0) {
        sum / count
    } else {
        0.0
    }
}

/**
 * Calculates the arithmetic mean of Double values in a sequence.
 *
 * @return The mean value, or 0.0 if the sequence is empty
 */
fun Sequence<Double>.meanDouble(): Double {
    var sum = 0.0
    var count = 0
    
    for (value in this) {
        sum += value
        count++
    }
    
    return if (count > 0) {
        sum / count
    } else {
        0.0
    }
}
