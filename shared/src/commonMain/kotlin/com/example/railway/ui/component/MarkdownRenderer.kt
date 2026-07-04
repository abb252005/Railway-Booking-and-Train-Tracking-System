package com.example.railway.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownRenderer(text: String, isUser: Boolean) {
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    
    // Simple logic to detect if the text contains a markdown table
    if (text.contains("|") && text.contains("---")) {
        val blocks = text.split("\n\n")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            blocks.forEach { block ->
                if (block.trim().startsWith("|") && block.contains("---")) {
                    MarkdownTable(block, isUser)
                } else {
                    Text(
                        text = block.trim(),
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    } else {
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun MarkdownTable(tableText: String, isUser: Boolean) {
    val lines = tableText.trim().split("\n").filter { it.isNotBlank() }
    if (lines.size < 2) return

    val rows = lines.filter { !it.contains("---") }.map { line ->
        line.trim().removeSurrounding("|").split("|").map { it.trim() }
    }

    if (rows.isEmpty()) return

    val borderColor = if (isUser) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
    val headerColor = if (isUser) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        rows.forEachIndexed { rowIndex, cells ->
            val isHeader = rowIndex == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { 
                        if (isHeader) it.background(headerColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)) 
                        else it 
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cells.forEach { cell ->
                    Text(
                        text = cell,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
            }
            if (rowIndex < rows.size - 1) {
                HorizontalDivider(color = borderColor)
            }
        }
    }
}
