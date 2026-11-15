package com.khasim.simplecalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF101114)
private val KeyColor = Color(0xFF292C33)
private val Accent = Color(0xFFB69CFF)
private val MainText = Color(0xFFF5F3FA)
private val MutedText = Color(0xFF9698A3)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Background,
                surface = Color(0xFF1B1D22),
                primary = Accent
            )) {
                CalculatorApp()
            }
        }
    }
}

@Composable
private fun CalculatorApp() {
    var expression by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var showHistory by remember { mutableStateOf(false) }
    var justEvaluated by remember { mutableStateOf(false) }

    fun append(value: String) {
        error = ""
        if (justEvaluated) {
            expression = if (value.firstOrNull()?.isDigit() == true || value == ".") "0" else expression
        }
        justEvaluated = false
        expression = if (expression == "0" && (value.firstOrNull()?.isDigit() == true || value == ".")) {
            value
        } else {
            expression + value
        }
    }

    fun calculate() {
        try {
            val answer = CalculatorEngine.evaluate(expression)
            history = (listOf(expression to answer) + history).take(30)
            expression = answer
            error = ""
            justEvaluated = true
        } catch (exception: Exception) {
            error = exception.message ?: "Invalid expression"
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SIMPLE", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("Calculator", color = MainText, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = if (showHistory) "⌕" else "↶",
                    color = Accent,
                    fontSize = 27.sp,
                    modifier = Modifier.clickable { showHistory = !showHistory }.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showHistory) {
                Text("HISTORY", color = MutedText, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(10.dp))
                if (history.isEmpty()) {
                    Text("No calculations yet", color = MutedText, modifier = Modifier.padding(vertical = 24.dp))
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(history) { item ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        expression = item.second
                                        error = ""
                                        justEvaluated = true
                                        showHistory = false
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(item.first, color = MutedText, fontSize = 16.sp)
                                Text("= ${item.second}", color = MainText, fontSize = 22.sp)
                            }
                            HorizontalDivider(color = KeyColor)
                        }
                    }
                }
                TextButton(onClick = { history = emptyList() }) {
                    Text("Clear history", color = Accent)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text(
                        text = expression,
                        color = MutedText,
                        fontSize = 30.sp,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = Color(0xFFFF7777),
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))

                val rows = listOf(
                    listOf("AC", "⌫", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "−"),
                    listOf("1", "2", "3", "+"),
                    listOf("±", "0", ".", "=")
                )

                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { label ->
                            val isOperator = label in listOf("AC", "⌫", "%", "÷", "×", "−", "+", "±")
                            val isEquals = label == "="
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f).padding(vertical = 5.dp)
                                    .background(
                                        color = if (isEquals) Accent else if (isOperator) Color(0xFF34313F) else KeyColor,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        when (label) {
                                            "AC" -> {
                                                expression = "0"
                                                error = ""
                                                justEvaluated = false
                                            }
                                            "⌫" -> {
                                                expression = if (expression.length <= 1) "0" else expression.dropLast(1)
                                                error = ""
                                                justEvaluated = false
                                            }
                                            "=" -> calculate()
                                            "%" -> {
                                                try {
                                                    expression = (CalculatorEngine.evaluate(expression).toDouble() / 100.0).toString()
                                                    error = ""
                                                    justEvaluated = false
                                                } catch (exception: Exception) {
                                                    error = "Invalid percentage"
                                                }
                                            }
                                            "±" -> {
                                                expression = if (expression.startsWith("-")) expression.drop(1) else "-$expression"
                                                error = ""
                                                justEvaluated = false
                                            }
                                            else -> append(label)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isEquals) Background else if (isOperator) Accent else MainText,
                                    fontSize = if (label.length > 1) 19.sp else 25.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Made for everyday calculations",
                modifier = Modifier.fillMaxWidth(),
                color = MutedText,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
