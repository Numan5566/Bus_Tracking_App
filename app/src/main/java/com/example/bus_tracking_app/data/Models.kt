package com.example.bus_tracking_app.data

import android.net.Uri

data class University(
    val id: String,
    val name: String,
    val city: String
)

data class User(
    val email: String,
    val name: String,
    val regNumber: String,
    val city: String,
    val university: String,
    val studentCardUri: String?, // Stores local image path or URI
    val passwordHash: String
)

data class SmtpConfig(
    val host: String = "smtp.gmail.com",
    val port: Int = 465,
    val username: String = "",
    val password: String = "", // Can be a Gmail App Password
    val useSSL: Boolean = true,
    val isEnabled: Boolean = false
)

data class SimulatedEmail(
    val recipient: String,
    val subject: String,
    val body: String,
    val timestamp: String,
    val deviceName: String
)

object UniversityData {
    val cities = listOf("Lahore", "Karachi", "Islamabad", "Faisalabad", "Multan")

    val universities = listOf(
        // Lahore Universities
        University("lhr_fast", "FAST-NUCES Lahore", "Lahore"),
        University("lhr_uet", "UET Lahore", "Lahore"),
        University("lhr_pu", "Punjab University (PU)", "Lahore"),
        University("lhr_lums", "LUMS", "Lahore"),
        University("lhr_itu", "Information Technology University (ITU)", "Lahore"),
        University("lhr_lse", "Lahore School of Economics (LSE)", "Lahore"),
        University("lhr_gcu", "Government College University (GCU)", "Lahore"),
        University("lhr_comsats", "COMSATS University Lahore", "Lahore"),
        University("lhr_umt", "UMT Lahore", "Lahore"),

        // Karachi Universities
        University("khi_iba", "IBA Karachi", "Karachi"),
        University("khi_ned", "NED University of Engineering & Tech", "Karachi"),
        University("khi_ku", "Karachi University (KU)", "Karachi"),
        University("khi_fast", "FAST-NUCES Karachi", "Karachi"),
        University("khi_habib", "Habib University", "Karachi"),
        University("khi_szabist", "SZABIST Karachi", "Karachi"),
        University("khi_iobm", "IoBM Karachi", "Karachi"),

        // Islamabad Universities
        University("isb_nust", "NUST Islamabad", "Islamabad"),
        University("isb_fast", "FAST-NUCES Islamabad", "Islamabad"),
        University("isb_comsats", "COMSATS Islamabad", "Islamabad"),
        University("isb_qau", "Quaid-i-Azam University (QAU)", "Islamabad"),
        University("isb_iiui", "Islamic University Islamabad (IIUI)", "Islamabad"),
        University("isb_giki", "GIKI (Topi / Islamabad Office)", "Islamabad"),

        // Faisalabad Universities
        University("fsd_uaf", "University of Agriculture Faisalabad (UAF)", "Faisalabad"),
        University("fsd_gcuf", "GC University Faisalabad (GCUF)", "Faisalabad"),
        University("fsd_ntu", "National Textile University (NTU)", "Faisalabad"),
        University("fsd_tuf", "The University of Faisalabad (TUF)", "Faisalabad"),

        // Multan Universities
        University("mul_bzu", "Bahauddin Zakariya University (BZU)", "Multan"),
        University("mul_nust", "NUST Multan Campus", "Multan"),
        University("mul_muf", "Multan University of Science & Tech", "Multan"),
        University("mul_wum", "Women University Multan", "Multan")
    )

    fun getUniversitiesForCity(city: String): List<University> {
        return universities.filter { it.city.equals(city, ignoreCase = true) }
    }
}
