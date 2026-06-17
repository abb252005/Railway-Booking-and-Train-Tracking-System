package com.example.railway.util

import com.example.railway.domain.model.Booking
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File

object PDFTicketGenerator {

    fun generate(
        booking: Booking,
        startStationName: String,
        endStationName: String,
        trainName: String
    ) {
        val home = System.getProperty("user.home")
        val downloadsDir = File(home, "Downloads")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        
        val file = File(downloadsDir, "RailwayTicket_${booking.id}.pdf")

        PDDocument().use { document ->
            // Increased height to 320f to prevent overlaps
            val ticketWidth = 600f
            val ticketHeight = 320f
            val page = PDPage(PDRectangle(ticketWidth, ticketHeight))
            document.addPage(page)

            PDPageContentStream(document, page).use { content ->
                // 1. Background
                content.setNonStrokingColor(Color(245, 245, 245))
                content.addRect(0f, 0f, ticketWidth, ticketHeight)
                content.fill()

                // 2. Ticket Card
                val margin = 15f
                content.setNonStrokingColor(Color.WHITE)
                content.addRect(margin, margin, ticketWidth - 2 * margin, ticketHeight - 2 * margin)
                content.fill()

                val fontBold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                val fontRegular = PDType1Font(Standard14Fonts.FontName.HELVETICA)

                var currentY = ticketHeight - margin - 25f

                // 3. Header
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "RAILWAY PASS", fontRegular, 8f, margin + 25f, currentY)
                
                // Small QR top right
                val qrMatrixSmall = QRCodeGenerator.generateQRMatrix(booking.id, size = 21)
                val pdImageSmall = LosslessFactory.createFromImage(document, matrixToBufferedImage(qrMatrixSmall))
                content.drawImage(pdImageSmall, ticketWidth - margin - 60f, ticketHeight - margin - 60f, 40f, 40f)

                currentY -= 20f
                content.setNonStrokingColor(Color.BLACK)
                drawText(content, "BOARDING TICKET", fontBold, 18f, margin + 25f, currentY)

                // 4. Passenger & Price
                currentY -= 35f
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "PASSENGER", fontRegular, 8f, margin + 25f, currentY)
                drawText(content, "PRICE", fontRegular, 8f, ticketWidth - margin - 85f, currentY)

                currentY -= 15f
                content.setNonStrokingColor(Color.BLACK)
                drawText(content, booking.passengerName.lowercase(), fontBold, 12f, margin + 25f, currentY)
                drawText(content, booking.price, fontBold, 12f, ticketWidth - margin - 85f, currentY)

                // 5. Stations
                currentY -= 35f
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "STATION FROM", fontRegular, 8f, margin + 25f, currentY)
                drawText(content, "STATION TO", fontRegular, 8f, ticketWidth - margin - 150f, currentY)

                currentY -= 20f
                content.setNonStrokingColor(Color.BLACK)
                drawText(content, "$startStationName (${booking.startStationId})", fontBold, 14f, margin + 25f, currentY)
                drawText(content, "$endStationName (${booking.endStationId})", fontBold, 14f, ticketWidth - margin - 150f, currentY)

                // 6. Dotted Line
                currentY -= 25f
                content.setStrokingColor(Color.LIGHT_GRAY)
                content.setLineWidth(1f)
                content.setLineDashPattern(floatArrayOf(3f, 3f), 0f)
                content.moveTo(margin + 25f, currentY)
                content.lineTo(ticketWidth - margin - 25f, currentY)
                content.stroke()
                content.setLineDashPattern(floatArrayOf(), 0f)

                // 7. Footer Info
                currentY -= 35f
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "TRAIN ID", fontRegular, 8f, margin + 25f, currentY)
                
                // Large QR code repositioned to NOT overlap
                val qrMatrixLarge = QRCodeGenerator.generateQRMatrix(booking.id, size = 21)
                val pdImageLarge = LosslessFactory.createFromImage(document, matrixToBufferedImage(qrMatrixLarge))
                content.drawImage(pdImageLarge, ticketWidth - margin - 95f, margin + 25f, 75f, 75f)

                currentY -= 15f
                content.setNonStrokingColor(Color.BLACK)
                drawText(content, booking.trainId, fontBold, 12f, margin + 25f, currentY)

                currentY -= 25f
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "SEAT NO", fontRegular, 8f, margin + 25f, currentY)

                currentY -= 15f
                content.setNonStrokingColor(Color.BLACK)
                val carNum = booking.carriageId.split("_").last()
                drawText(content, "Car $carNum | ${booking.seatNumber}", fontBold, 12f, margin + 25f, currentY)
            }
            document.save(file)
        }
    }

    private fun drawText(content: PDPageContentStream, text: String, font: PDType1Font, size: Float, x: Float, y: Float) {
        content.beginText()
        content.setFont(font, size)
        content.newLineAtOffset(x, y)
        content.showText(text)
        content.endText()
    }

    private fun matrixToBufferedImage(matrix: Array<BooleanArray>): BufferedImage {
        val size = matrix.size
        // Use a scale factor for higher quality QR codes in the PDF
        val scale = 10
        val image = BufferedImage(size * scale, size * scale, BufferedImage.TYPE_BYTE_BINARY)
        val graphics = image.createGraphics()
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, size * scale, size * scale)
        graphics.color = Color.BLACK
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (matrix[y][x]) {
                    graphics.fillRect(x * scale, y * scale, scale, scale)
                }
            }
        }
        graphics.dispose()
        return image
    }
}
