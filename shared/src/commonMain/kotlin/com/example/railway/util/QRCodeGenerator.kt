package com.example.railway.util

object QRCodeGenerator {
    /**
     * Generates a simple bit matrix representing a QR code for the given content.
     * For demo purposes, we'll generate a pseudo-random pattern based on the string hash.
     */
    fun generateQRMatrix(content: String, size: Int = 21): Array<BooleanArray> {
        val matrix = Array(size) { BooleanArray(size) }
        val hash = content.hashCode()
        val random = kotlin.random.Random(hash)

        // Draw standard QR-like corners
        drawCorner(matrix, 0, 0)
        drawCorner(matrix, size - 7, 0)
        drawCorner(matrix, 0, size - 7)

        for (y in 0 until size) {
            for (x in 0 until size) {
                // Don't overwrite corners
                if (isInsideCorner(x, y, size)) continue
                matrix[y][x] = random.nextBoolean()
            }
        }
        return matrix
    }

    private fun drawCorner(matrix: Array<BooleanArray>, ox: Int, oy: Int) {
        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val isBorder = x == 0 || x == 6 || y == 0 || y == 6
                val isInner = x in 2..4 && y in 2..4
                matrix[oy + y][ox + x] = isBorder || isInner
            }
        }
    }

    private fun isInsideCorner(x: Int, y: Int, size: Int): Boolean {
        return (x < 7 && y < 7) || (x >= size - 7 && y < 7) || (x < 7 && y >= size - 7)
    }
}
