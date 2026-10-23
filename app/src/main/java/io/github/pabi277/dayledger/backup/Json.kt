package io.github.pabi277.dayledger.backup

/**
 * A deliberately small JSON model + parser + writer.
 *
 * The app has no JSON library (no backend, no analytics, no extra dependency), and
 * a backup format is security-relevant: it is the only file the owner's data ever
 * leaves the phone in. Keeping the parser in-tree means the restore path can be
 * unit-tested on the JVM with no Android runtime, and the encoder can stay
 * deterministic so two exports of the same data are comparable.
 */
sealed class JsonValue {
    data class Obj(val entries: List<Pair<String, JsonValue>>) : JsonValue() {
        fun get(key: String): JsonValue? = entries.firstOrNull { it.first == key }?.second
        fun string(key: String): String? = (get(key) as? Str)?.value
        fun int(key: String): Int? = (get(key) as? Num)?.asLong()?.toInt()
        fun long(key: String): Long? = (get(key) as? Num)?.asLong()
        fun array(key: String): List<JsonValue>? = (get(key) as? Arr)?.items
    }

    data class Arr(val items: List<JsonValue>) : JsonValue()

    data class Str(val value: String) : JsonValue()

    /** Numbers keep their raw text so integers never lose precision. */
    data class Num(val raw: String) : JsonValue() {
        fun asLong(): Long? = raw.toLongOrNull()
        fun asDouble(): Double? = raw.toDoubleOrNull()
    }

    data class Bool(val value: Boolean) : JsonValue()

    object Null : JsonValue()
}

class JsonParseException(message: String) : Exception(message)

object Json {

    // ---------------------------------------------------------------- encoding

    fun write(value: JsonValue): String = StringBuilder().also { writeTo(it, value) }.toString()

    private fun writeTo(sb: StringBuilder, value: JsonValue) {
        when (value) {
            is JsonValue.Obj -> {
                sb.append('{')
                value.entries.forEachIndexed { index, (key, item) ->
                    if (index > 0) sb.append(',')
                    writeString(sb, key)
                    sb.append(':')
                    writeTo(sb, item)
                }
                sb.append('}')
            }
            is JsonValue.Arr -> {
                sb.append('[')
                value.items.forEachIndexed { index, item ->
                    if (index > 0) sb.append(',')
                    writeTo(sb, item)
                }
                sb.append(']')
            }
            is JsonValue.Str -> writeString(sb, value.value)
            is JsonValue.Num -> sb.append(value.raw)
            is JsonValue.Bool -> sb.append(if (value.value) "true" else "false")
            is JsonValue.Null -> sb.append("null")
        }
    }

    private fun writeString(sb: StringBuilder, text: String) {
        sb.append('"')
        for (ch in text) {
            when (ch) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                else -> if (ch < ' ') sb.append("\\u").append(ch.code.toString(16).padStart(4, '0')) else sb.append(ch)
            }
        }
        sb.append('"')
    }

    // ---------------------------------------------------------------- decoding

    fun parse(text: String): JsonValue {
        val parser = Parser(text)
        val value = parser.parseValue()
        parser.skipWhitespace()
        if (!parser.atEnd()) throw JsonParseException("Trailing content at position ${parser.position}")
        return value
    }

    private class Parser(private val text: String) {
        var position: Int = 0
            private set

        fun atEnd(): Boolean = position >= text.length

        fun skipWhitespace() {
            while (position < text.length && text[position].isWhitespace()) position++
        }

        fun parseValue(): JsonValue {
            skipWhitespace()
            if (atEnd()) throw JsonParseException("Unexpected end of input")
            return when (val ch = text[position]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> JsonValue.Str(parseString())
                't' -> literal("true", JsonValue.Bool(true))
                'f' -> literal("false", JsonValue.Bool(false))
                'n' -> literal("null", JsonValue.Null)
                else -> if (ch == '-' || ch.isDigit()) parseNumber()
                else throw JsonParseException("Unexpected character '$ch' at position $position")
            }
        }

        private fun literal(word: String, value: JsonValue): JsonValue {
            if (!text.startsWith(word, position)) {
                throw JsonParseException("Expected $word at position $position")
            }
            position += word.length
            return value
        }

        private fun parseObject(): JsonValue {
            position++ // {
            val entries = ArrayList<Pair<String, JsonValue>>()
            skipWhitespace()
            if (!atEnd() && text[position] == '}') {
                position++
                return JsonValue.Obj(entries)
            }
            while (true) {
                skipWhitespace()
                if (atEnd() || text[position] != '"') throw JsonParseException("Expected object key at position $position")
                val key = parseString()
                skipWhitespace()
                if (atEnd() || text[position] != ':') throw JsonParseException("Expected ':' at position $position")
                position++
                entries.add(key to parseValue())
                skipWhitespace()
                if (atEnd()) throw JsonParseException("Unterminated object")
                when (text[position]) {
                    ',' -> position++
                    '}' -> { position++; return JsonValue.Obj(entries) }
                    else -> throw JsonParseException("Expected ',' or '}' at position $position")
                }
            }
        }

        private fun parseArray(): JsonValue {
            position++ // [
            val items = ArrayList<JsonValue>()
            skipWhitespace()
            if (!atEnd() && text[position] == ']') {
                position++
                return JsonValue.Arr(items)
            }
            while (true) {
                items.add(parseValue())
                skipWhitespace()
                if (atEnd()) throw JsonParseException("Unterminated array")
                when (text[position]) {
                    ',' -> position++
                    ']' -> { position++; return JsonValue.Arr(items) }
                    else -> throw JsonParseException("Expected ',' or ']' at position $position")
                }
            }
        }

        private fun parseString(): String {
            position++ // opening quote
            val sb = StringBuilder()
            while (true) {
                if (atEnd()) throw JsonParseException("Unterminated string")
                when (val ch = text[position]) {
                    '"' -> { position++; return sb.toString() }
                    '\\' -> {
                        position++
                        if (atEnd()) throw JsonParseException("Unterminated escape")
                        when (val esc = text[position]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                if (position + 4 >= text.length) throw JsonParseException("Bad \\u escape")
                                val hex = text.substring(position + 1, position + 5)
                                val code = hex.toIntOrNull(16)
                                    ?: throw JsonParseException("Bad \\u escape '$hex'")
                                sb.append(code.toChar())
                                position += 4
                            }
                            else -> throw JsonParseException("Bad escape '\\$esc'")
                        }
                        position++
                    }
                    else -> { sb.append(ch); position++ }
                }
            }
        }

        private fun parseNumber(): JsonValue {
            val start = position
            if (!atEnd() && text[position] == '-') position++
            while (!atEnd() && (text[position].isDigit() || text[position] in ".eE+-")) position++
            val raw = text.substring(start, position)
            if (raw.toDoubleOrNull() == null) throw JsonParseException("Bad number '$raw'")
            return JsonValue.Num(raw)
        }
    }
}
