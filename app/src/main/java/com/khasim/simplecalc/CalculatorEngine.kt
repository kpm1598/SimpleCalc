package com.khasim.simplecalc

import java.math.BigDecimal

/**
 * Small arithmetic parser. It supports operator precedence and parentheses
 * without evaluating arbitrary Kotlin/Java code.
 */
object CalculatorEngine {
    fun evaluate(source: String): String {
        val normalized = source.replace('×', '*').replace('÷', '/').replace('−', '-')
        val value = Parser(normalized).parse()
        if (!value.isFinite()) throw ArithmeticException("Invalid result")
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    }

    private class Parser(private val input: String) {
        private var pos = 0

        fun parse(): Double {
            val value = expression()
            skipSpaces()
            if (pos != input.length) throw IllegalArgumentException("Invalid expression")
            return value
        }

        private fun expression(): Double {
            var value = term()
            while (true) {
                skipSpaces()
                value = when {
                    consume('+') -> value + term()
                    consume('-') -> value - term()
                    else -> return value
                }
            }
        }

        private fun term(): Double {
            var value = factor()
            while (true) {
                skipSpaces()
                value = when {
                    consume('*') -> value * factor()
                    consume('/') -> {
                        val divisor = factor()
                        if (divisor == 0.0) throw ArithmeticException("Cannot divide by zero")
                        value / divisor
                    }
                    else -> return value
                }
            }
        }

        private fun factor(): Double {
            skipSpaces()
            if (consume('+')) return factor()
            if (consume('-')) return -factor()
            if (consume('(')) {
                val value = expression()
                skipSpaces()
                if (!consume(')')) throw IllegalArgumentException("Missing closing bracket")
                return value
            }

            val start = pos
            var dotSeen = false
            while (pos < input.length) {
                val c = input[pos]
                when {
                    c.isDigit() -> pos++
                    c == '.' && !dotSeen -> { dotSeen = true; pos++ }
                    else -> break
                }
            }
            if (start == pos) throw IllegalArgumentException("Number expected")
            return input.substring(start, pos).toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number")
        }

        private fun skipSpaces() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }

        private fun consume(char: Char): Boolean {
            if (pos < input.length && input[pos] == char) {
                pos++
                return true
            }
            return false
        }
    }
}
