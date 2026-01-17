package util

/**
 * Escapes special characters in a string for JSON encoding.
 *
 * @param src The source string to escape
 * @return The escaped string suitable for JSON
 */
fun jsonEscape(src: String): String {
    val escaped = StringBuilder(src.length)
    
    for (c in src) {
        when (c) {
            '\b' -> escaped.append("\\b")
            '\u000C' -> escaped.append("\\f")  // Form feed
            '\n' -> escaped.append("\\n")
            '\r' -> escaped.append("\\r")
            '\t' -> escaped.append("\\t")
            '"' -> escaped.append("\\\"")
            '\\' -> escaped.append("\\\\")
            else -> escaped.append(c)
        }
    }
    
    return escaped.toString()
}
