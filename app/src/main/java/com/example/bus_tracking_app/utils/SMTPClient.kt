package com.example.bus_tracking_app.utils

import android.os.Build
import android.util.Base64
import com.example.bus_tracking_app.data.SmtpConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import javax.net.ssl.SSLSocketFactory

object SMTPClient {

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER ?: "Android"
        val model = Build.MODEL ?: "Device"
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }

    suspend fun sendEmail(
        config: SmtpConfig,
        recipient: String,
        subject: String,
        bodyHtml: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!config.isEnabled || config.username.isBlank() || config.password.isBlank()) {
            return@withContext Result.failure(Exception("SMTP not configured or disabled"))
        }

        try {
            val factory = SSLSocketFactory.getDefault()
            val socket = factory.createSocket(config.host, config.port)
            socket.soTimeout = 10000 // 10 seconds timeout

            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)

            fun readResponse(expectedCode: String) {
                var line = reader.readLine() ?: throw Exception("SMTP connection closed unexpectedly")
                while (line.length > 3 && line[3] == '-') {
                    line = reader.readLine() ?: throw Exception("SMTP connection closed unexpectedly")
                }
                if (!line.startsWith(expectedCode)) {
                    throw Exception("SMTP error response: $line")
                }
            }

            readResponse("220")

            writer.println("EHLO ${config.host}")
            readResponse("250")

            writer.println("AUTH LOGIN")
            readResponse("334")

            val userB64 = Base64.encodeToString(config.username.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            writer.println(userB64)
            readResponse("334")

            val passB64 = Base64.encodeToString(config.password.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            writer.println(passB64)
            readResponse("235")

            writer.println("MAIL FROM:<${config.username}>")
            readResponse("250")

            writer.println("RCPT TO:<$recipient>")
            readResponse("250")

            writer.println("DATA")
            readResponse("354")

            // Write email headers and body
            writer.println("From: Bus Tracking App <${config.username}>")
            writer.println("To: $recipient")
            writer.println("Subject: $subject")
            writer.println("MIME-Version: 1.0")
            writer.println("Content-Type: text/html; charset=UTF-8")
            writer.println()
            writer.println(bodyHtml)
            writer.println(".")
            readResponse("250")

            writer.println("QUIT")
            socket.close()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
