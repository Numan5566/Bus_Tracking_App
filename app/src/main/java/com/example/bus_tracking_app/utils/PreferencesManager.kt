package com.example.bus_tracking_app.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.bus_tracking_app.data.SmtpConfig
import com.example.bus_tracking_app.data.User
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("BusTrackingPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USERS = "registered_users"
        private const val KEY_SMTP_HOST = "smtp_host"
        private const val KEY_SMTP_PORT = "smtp_port"
        private const val KEY_SMTP_USER = "smtp_user"
        private const val KEY_SMTP_PASS = "smtp_pass"
        private const val KEY_SMTP_SSL = "smtp_ssl"
        private const val KEY_SMTP_ENABLED = "smtp_enabled"
    }

    // --- User Management ---

    fun registerUser(user: User): Boolean {
        val users = getAllUsers().toMutableList()
        // Check if user already exists
        if (users.any { it.email.equals(user.email, ignoreCase = true) }) {
            return false // User already registered
        }
        users.add(user)
        saveUsersList(users)
        return true
    }

    fun getAllUsers(): List<User> {
        val jsonString = prefs.getString(KEY_USERS, "[]") ?: "[]"
        val users = mutableListOf<User>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                users.add(
                    User(
                        email = obj.getString("email"),
                        name = obj.getString("name"),
                        regNumber = obj.getString("regNumber"),
                        city = obj.getString("city"),
                        university = obj.getString("university"),
                        studentCardUri = if (obj.has("studentCardUri") && !obj.isNull("studentCardUri")) obj.getString("studentCardUri") else null,
                        passwordHash = obj.getString("passwordHash")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return users
    }

    fun getUser(email: String): User? {
        return getAllUsers().find { it.email.equals(email, ignoreCase = true) }
    }

    fun verifyUser(email: String, passwordHash: String): Boolean {
        val user = getUser(email)
        return user != null && user.passwordHash == passwordHash
    }

    fun updatePassword(email: String, newPasswordHash: String): Boolean {
        val users = getAllUsers().toMutableList()
        val index = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index != -1) {
            val oldUser = users[index]
            users[index] = oldUser.copy(passwordHash = newPasswordHash)
            saveUsersList(users)
            return true
        }
        return false
    }

    private fun saveUsersList(users: List<User>) {
        val jsonArray = JSONArray()
        for (user in users) {
            val obj = JSONObject().apply {
                put("email", user.email)
                put("name", user.name)
                put("regNumber", user.regNumber)
                put("city", user.city)
                put("university", user.university)
                put("studentCardUri", user.studentCardUri)
                put("passwordHash", user.passwordHash)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_USERS, jsonArray.toString()).apply()
    }

    // --- SMTP Settings Management ---

    fun saveSmtpConfig(config: SmtpConfig) {
        prefs.edit().apply {
            putString(KEY_SMTP_HOST, config.host)
            putInt(KEY_SMTP_PORT, config.port)
            putString(KEY_SMTP_USER, config.username)
            putString(KEY_SMTP_PASS, config.password)
            putBoolean(KEY_SMTP_SSL, config.useSSL)
            putBoolean(KEY_SMTP_ENABLED, config.isEnabled)
            apply()
        }
    }

    fun getSmtpConfig(): SmtpConfig {
        return SmtpConfig(
            host = prefs.getString(KEY_SMTP_HOST, "smtp.gmail.com") ?: "smtp.gmail.com",
            port = prefs.getInt(KEY_SMTP_PORT, 465),
            username = prefs.getString(KEY_SMTP_USER, "") ?: "",
            password = prefs.getString(KEY_SMTP_PASS, "") ?: "",
            useSSL = prefs.getBoolean(KEY_SMTP_SSL, true),
            isEnabled = prefs.getBoolean(KEY_SMTP_ENABLED, false)
        )
    }
}
