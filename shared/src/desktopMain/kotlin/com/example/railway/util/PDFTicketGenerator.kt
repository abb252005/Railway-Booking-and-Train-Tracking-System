package com.example.railway.util

import com.example.railway.domain.model.Booking
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

object PDFTicketGenerator {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm z")
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")

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
            val ticketWidth = 600f
            val ticketHeight = 450f // Increased height for more data
            val page = PDPage(PDRectangle(ticketWidth, ticketHeight))
            document.addPage(page)

            PDPageContentStream(document, page).use { content ->
                // 1. Clean White Background (Accessibility/Printability)
                content.setNonStrokingColor(Color.WHITE)
                content.addRect(0f, 0f, ticketWidth, ticketHeight)
                content.fill()

                // 2. High Contrast Header Band
                content.setNonStrokingColor(Color(0, 34, 68)) // Deep Blue
                content.addRect(0f, ticketHeight - 60f, ticketWidth, 60f)
                content.fill()

                val fontBold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                val fontRegular = PDType1Font(Standard14Fonts.FontName.HELVETICA)
                val fontMono = PDType1Font(Standard14Fonts.FontName.COURIER_BOLD)

                // Header Text
                content.setNonStrokingColor(Color.WHITE)
                drawText(content, "RAILTRACK PRO - eTICKET", fontBold, 14f, 25f, ticketHeight - 35f)
                drawText(content, "STATUS: ${booking.status.name}", fontBold, 10f, ticketWidth - 150f, ticketHeight - 35f)

                var currentY = ticketHeight - 90f
                content.setNonStrokingColor(Color.BLACK)

                // 3. Passenger Information
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "PASSENGER", fontRegular, 8f, 25f, currentY)
                drawText(content, "TICKET ID / RESERVATION", fontRegular, 8f, ticketWidth - 200f, currentY)

                currentY -= 15f
                content.setNonStrokingColor(Color.BLACK)
                drawText(content, booking.passengerName.uppercase(), fontBold, 14f, 25f, currentY)
                drawText(content, "${booking.id} / ${booking.reservationId}", fontBold, 12f, ticketWidth - 200f, currentY)

                currentY -= 40f
                content.setStrokingColor(Color.LIGHT_GRAY)
                content.setLineWidth(0.5f)
                content.moveTo(25f, currentY + 10f)
                content.lineTo(ticketWidth - 25f, currentY + 10f)
                content.stroke()

                // 4. Itinerary
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "FROM", fontRegular, 8f, 25f, currentY)
                drawText(content, "TO", fontRegular, 8f, ticketWidth - 200f, currentY)

                currentY -= 20f
                content.setNonStrokingColor(Color.BLACK)
                drawText(content, startStationName, fontBold, 16f, 25f, currentY)
                drawText(content, endStationName, fontBold, 16f, ticketWidth - 200f, currentY)

                currentY -= 15f
                content.setNonStrokingColor(Color(0, 102, 204))
                drawText(content, booking.startStationCode, fontBold, 10f, 25f, currentY)
                drawText(content, booking.endStationCode, fontBold, 10f, ticketWidth - 200f, currentY)

                currentY -= 30f
                content.setNonStrokingColor(Color.BLACK)
                val depTime = Instant.ofEpochMilli(booking.departureTimeMillis).atZone(ZoneId.of(booking.timezone))
                val arrTime = Instant.ofEpochMilli(booking.arrivalTimeMillis).atZone(ZoneId.of(booking.timezone))
                
                drawText(content, depTime.format(timeFormatter), fontBold, 22f, 25f, currentY)
                drawText(content, arrTime.format(timeFormatter), fontBold, 22f, ticketWidth - 200f, currentY)

                currentY -= 20f
                drawText(content, depTime.format(dateFormatter), fontRegular, 10f, 25f, currentY)
                drawText(content, arrTime.format(dateFormatter), fontRegular, 10f, ticketWidth - 200f, currentY)

                // 5. Train & Seat
                currentY -= 50f
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "SERVICE / TRAIN", fontRegular, 8f, 25f, currentY)
                drawText(content, "CLASS / ACCOMMODATION", fontRegular, 8f, ticketWidth - 200f, currentY)

                currentY -= 15f
                content.setNonStrokingColor(Color.BLACK)
                drawText(content, "${booking.serviceName} ${booking.publicTrainNumber}", fontBold, 12f, 25f, currentY)
                val carNum = booking.carriageId.split("-").last()
                drawText(content, "${booking.fareClass.name} - Car $carNum Seat ${booking.seatNumber}", fontBold, 12f, ticketWidth - 200f, currentY)

                // 6. QR Code Area (Centered and Large)
                currentY -= 130f
                val qrContent = booking.barcodePayload.ifEmpty { "RAILTKT-v1-${booking.id}" }
                val qrMatrix = QRCodeGenerator.generateQRMatrix(qrContent, size = 25)
                val pdImage = LosslessFactory.createFromImage(document, matrixToBufferedImage(qrMatrix))
                content.drawImage(pdImage, (ticketWidth / 2) - 60f, currentY, 120f, 120f)
                
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "SCAN FOR BOARDING VALIDATION", fontBold, 7f, (ticketWidth / 2) - 65f, currentY - 10f)

                // 7. Payment & Footer
                currentY -= 50f
                content.setNonStrokingColor(Color.GRAY)
                drawText(content, "TOTAL PRICE", fontRegular, 8f, 25f, currentY)
                drawText(content, "ISSUED AT", fontRegular, 8f, ticketWidth - 200f, currentY)

                currentY -= 15f
                content.setNonStrokingColor(Color.BLACK)
                drawText(content, "${booking.totalPrice.currency} ${booking.totalPrice.amountCents / 100.0}", fontBold, 14f, 25f, currentY)
                val issueTime = Instant.ofEpochMilli(booking.timestamp).atZone(ZoneId.of("UTC"))
                drawText(content, issueTime.format(DateTimeFormatter.ISO_INSTANT), fontRegular, 8f, ticketWidth - 200f, currentY)

                // 8. Fine Print
                currentY -= 25f
                content.setNonStrokingColor(Color.LIGHT_GRAY)
                val footerText = "Valid for travel on ${booking.serviceDate} only. Photo ID required onboard. SAMPLE - NOT VALID FOR TRAVEL."
                drawText(content, footerText, fontRegular, 7f, 25f, currentY)
            }
            document.save(file)
        }
    }

    fun generatePaymentReport(
        booking: Booking,
        startStationName: String,
        endStationName: String,
        trainName: String
    ) {
        val home = System.getProperty("user.home")
        val downloadsDir = File(home, "Downloads")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val file = File(downloadsDir, "PaymentReport_${booking.id}.pdf")

        PDDocument().use { document ->
            val pageWidth = 600f
            val pageHeight = 800f
            val page = PDPage(PDRectangle(pageWidth, pageHeight))
            document.addPage(page)

            PDPageContentStream(document, page).use { content ->
                content.setNonStrokingColor(Color.WHITE)
                content.addRect(0f, 0f, pageWidth, pageHeight)
                content.fill()

                content.setNonStrokingColor(Color(16, 40, 64)) // Header Blue
                content.addRect(0f, pageHeight - 100f, pageWidth, 100f)
                content.fill()

                val fontBold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                val fontRegular = PDType1Font(Standard14Fonts.FontName.HELVETICA)

                content.setNonStrokingColor(Color.WHITE)
                drawText(content, "U.S. INTERCITY RAIL", fontBold, 18f, 40f, pageHeight - 50f)
                drawText(content, "PAYMENT RECEIPT / REPORT", fontRegular, 12f, 40f, pageHeight - 75f)

                var currentY = pageHeight - 150f
                content.setNonStrokingColor(Color.BLACK)

                drawText(content, "TRANSACTION DETAILS", fontBold, 14f, 40f, currentY)
                currentY -= 30f

                val details = listOf(
                    "Receipt Number" to booking.id,
                    "Reservation ID" to booking.reservationId,
                    "Transaction Date" to Instant.ofEpochMilli(booking.timestamp).atZone(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME),
                    "Passenger" to booking.passengerName,
                    "Payment Method" to booking.paymentMethod.name,
                    "Train" to "$trainName (${booking.publicTrainNumber})",
                    "Route" to "$startStationName to $endStationName",
                    "Class" to booking.fareClass.name,
                    "Seat" to "Car ${booking.carriageId.split("_").last()} | ${booking.seatNumber}"
                )

                details.forEach { (label, value) ->
                    content.setNonStrokingColor(Color.GRAY)
                    drawText(content, label, fontRegular, 10f, 40f, currentY)
                    content.setNonStrokingColor(Color.BLACK)
                    drawText(content, value, fontBold, 11f, 200f, currentY)
                    currentY -= 20f
                }

                currentY -= 40f
                content.setStrokingColor(Color.LIGHT_GRAY)
                content.setLineWidth(1f)
                content.moveTo(40f, currentY)
                content.lineTo(pageWidth - 40f, currentY)
                content.stroke()

                currentY -= 40f
                drawText(content, "FARE SUMMARY", fontBold, 14f, 40f, currentY)
                currentY -= 30f

                val baseFare = booking.totalPrice.baseAmountCents / 100.0
                val taxes = booking.totalPrice.taxAmountCents / 100.0

                content.setNonStrokingColor(Color.BLACK)
                drawText(content, "Base Fare", fontRegular, 12f, 40f, currentY)
                drawText(content, "${booking.totalPrice.currency} ${"%.2f".format(baseFare)}", fontRegular, 12f, pageWidth - 150f, currentY)
                currentY -= 25f

                drawText(content, "Taxes & Fees (8.25%)", fontRegular, 12f, 40f, currentY)
                drawText(content, "${booking.totalPrice.currency} ${"%.2f".format(taxes)}", fontRegular, 12f, pageWidth - 150f, currentY)
                currentY -= 40f

                content.setStrokingColor(Color.BLACK)
                content.setLineWidth(2f)
                content.moveTo(40f, currentY)
                content.lineTo(pageWidth - 40f, currentY)
                content.stroke()

                currentY -= 30f
                drawText(content, "TOTAL PAID", fontBold, 16f, 40f, currentY)
                drawText(content, "${booking.totalPrice.currency} ${"%.2f".format(booking.totalPrice.amountCents / 100.0)}", fontBold, 16f, pageWidth - 150f, currentY)

                currentY = 100f
                content.setNonStrokingColor(Color.GRAY)
                val footerText = "Thank you for choosing U.S. Intercity Rail. This is an official payment report for your records."
                drawText(content, footerText, fontRegular, 9f, 40f, currentY)
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
