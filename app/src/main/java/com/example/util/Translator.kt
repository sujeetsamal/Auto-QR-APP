package com.example.util

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी"),
    ODIA("or", "ଓଡ଼ିଆ")
}

object Translator {
    
    private val dictionary = mapOf(
        "app_name" to mapOf(
            AppLanguage.ENGLISH to "AutoQR",
            AppLanguage.HINDI to "AutoQR",
            AppLanguage.ODIA to "AutoQR"
        ),
        
        // Navigation
        "nav_dashboard" to mapOf(
            AppLanguage.ENGLISH to "Dashboard",
            AppLanguage.HINDI to "डैशबोर्ड",
            AppLanguage.ODIA to "ଡ୍ୟାସବୋର୍ଡ"
        ),
        "nav_qr_code" to mapOf(
            AppLanguage.ENGLISH to "My QR",
            AppLanguage.HINDI to "मेरा QR",
            AppLanguage.ODIA to "ମୋ QR କୋଡ୍"
        ),
        "nav_history" to mapOf(
            AppLanguage.ENGLISH to "History",
            AppLanguage.HINDI to "इतिहास",
            AppLanguage.ODIA to "ଇତିହାସ"
        ),
        "nav_settings" to mapOf(
            AppLanguage.ENGLISH to "Settings",
            AppLanguage.HINDI to "सेटिंग्स",
            AppLanguage.ODIA to "ସେଟିଂସ"
        ),
        
        // Role Selection
        "role_select_title" to mapOf(
            AppLanguage.ENGLISH to "Choose Your Role",
            AppLanguage.HINDI to "अपनी भूमिका चुनें",
            AppLanguage.ODIA to "ଭୂମିକା ବାଛନ୍ତୁ"
        ),
        "role_driver_btn" to mapOf(
            AppLanguage.ENGLISH to "Main Driver Hoon 🚗",
            AppLanguage.HINDI to "मैं ड्राइवर हूँ 🚗",
            AppLanguage.ODIA to "ମୁଁ ଡ୍ରାଇଭର ଅଟେ 🚗"
        ),
        "role_passenger_btn" to mapOf(
            AppLanguage.ENGLISH to "Main Passenger Hoon 👤",
            AppLanguage.HINDI to "मैं यात्री हूँ 👤",
            AppLanguage.ODIA to "ମୁଁ ଯାତ୍ରୀ ଅଟେ 👤"
        ),
        
        // Auth
        "auth_welcome" to mapOf(
            AppLanguage.ENGLISH to "Welcome to AutoQR",
            AppLanguage.HINDI to "AutoQR में आपका स्वागत है",
            AppLanguage.ODIA to "AutoQR କୁ ସ୍ଵାଗତ"
        ),
        "auth_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Smart Fare Management for Indian Rickshaws",
            AppLanguage.HINDI to "भारतीय ऑटो रिक्शा के लिए स्मार्ट किराया प्रबंधन",
            AppLanguage.ODIA to "ଭାରତୀୟ ଅଟୋ ରିକ୍ସା ପାଇଁ ସ୍ମାର୍ଟ ଭଡ଼ା ପରିଚାଳନା"
        ),
        "auth_phone_label" to mapOf(
            AppLanguage.ENGLISH to "Phone Number",
            AppLanguage.HINDI to "फ़ोन नंबर",
            AppLanguage.ODIA to "ଫୋନ ନମ୍ବର"
        ),
        "auth_otp_label" to mapOf(
            AppLanguage.ENGLISH to "Enter 6-Digit OTP",
            AppLanguage.HINDI to "6-अंकीय OTP दर्ज करें",
            AppLanguage.ODIA to "୬-ଅଙ୍କ ବିଶିଷ୍ଟ OTP ଦିଅନ୍ତୁ"
        ),
        "auth_send_otp_btn" to mapOf(
            AppLanguage.ENGLISH to "Send OTP",
            AppLanguage.HINDI to "OTP भेजें",
            AppLanguage.ODIA to "OTP ପଠାନ୍ତୁ"
        ),
        "auth_verify_btn" to mapOf(
            AppLanguage.ENGLISH to "Verify & Log In",
            AppLanguage.HINDI to "सत्यापित करें और लॉगिन करें",
            AppLanguage.ODIA to "ଯାଞ୍ଚ କରି ଲଗଇନ୍ କରନ୍ତୁ"
        ),
        
        // Driver Dashboard
        "driver_title" to mapOf(
            AppLanguage.ENGLISH to "Driver Dashboard",
            AppLanguage.HINDI to "ड्राइवर डैशबोर्ड",
            AppLanguage.ODIA to "ଡ୍ରାଇଭର ଡ୍ୟାସବୋର୍ଡ"
        ),
        "start_trip_btn" to mapOf(
            AppLanguage.ENGLISH to "Start New Trip",
            AppLanguage.HINDI to "नया सफ़र शुरू करो",
            AppLanguage.ODIA to "ନୂଆ ଯାତ୍ରା ଆରମ୍ଭ କରନ୍ତୁ"
        ),
        "today_trips" to mapOf(
            AppLanguage.ENGLISH to "Today's Trips",
            AppLanguage.HINDI to "आज के सफ़र",
            AppLanguage.ODIA to "ଆଜିର ଯାତ୍ରା"
        ),
        "today_earnings" to mapOf(
            AppLanguage.ENGLISH to "Today's Earnings",
            AppLanguage.HINDI to "आज की कमाई",
            AppLanguage.ODIA to "ଆଜିର ରୋଜଗାର"
        ),
        
        // Start Trip
        "passenger_scan_instruction" to mapOf(
            AppLanguage.ENGLISH to "Ask passenger to scan this QR",
            AppLanguage.HINDI to "यात्री को यह QR स्कैन करने दें",
            AppLanguage.ODIA to "ଯାତ୍ରୀଙ୍କୁ ଏହି QR ସ୍କାନ କରିବାକୁ କୁହନ୍ତୁ"
        ),
        
        // Active Trip
        "active_trip_title" to mapOf(
            AppLanguage.ENGLISH to "Active Trip",
            AppLanguage.HINDI to "सक्रिय सफ़र",
            AppLanguage.ODIA to "ଚାଲୁ ରହିଥିବା ଯାତ୍ରା"
        ),
        "gps_both_mode" to mapOf(
            AppLanguage.ENGLISH to "📍 Both GPS Averaged",
            AppLanguage.HINDI to "📍 दोनों GPS से average",
            AppLanguage.ODIA to "📍 ଉଭୟ GPS ହାରାହାରି"
        ),
        "gps_driver_mode" to mapOf(
            AppLanguage.ENGLISH to "📍 Driver GPS Only",
            AppLanguage.HINDI to "📍 सिर्फ Driver GPS",
            AppLanguage.ODIA to "📍 କେବଳ ଡ୍ରାଇଭର GPS"
        ),
        "end_trip_btn" to mapOf(
            AppLanguage.ENGLISH to "End Trip",
            AppLanguage.HINDI to "सफ़र खत्म करो",
            AppLanguage.ODIA to "ଯାତ୍ରା ଶେଷ କରନ୍ତୁ"
        ),
        
        // Completed Trip
        "trip_completed" to mapOf(
            AppLanguage.ENGLISH to "Trip Completed",
            AppLanguage.HINDI to "सफ़र समाप्त हुआ",
            AppLanguage.ODIA to "ଯାତ୍ରା ସମ୍ପୂର୍ଣ୍ଣ ହେଲା"
        ),
        "payment_qr_instruction" to mapOf(
            AppLanguage.ENGLISH to "Scan QR to Pay",
            AppLanguage.HINDI to "भुगतान के लिए इस QR को स्कैन करें",
            AppLanguage.ODIA to "ପେମେଣ୍ଟ କରିବାକୁ ଏହି QR ସ୍କାନ କରନ୍ତୁ"
        ),
        "new_trip_nav" to mapOf(
            AppLanguage.ENGLISH to "New Trip",
            AppLanguage.HINDI to "नया सफ़र",
            AppLanguage.ODIA to "ନୂଆ ଯାତ୍ରା"
        ),
        
        // Passenger flow
        "passenger_scan_title" to mapOf(
            AppLanguage.ENGLISH to "Scan Driver QR Code",
            AppLanguage.HINDI to "Driver का QR Code स्कैन करें",
            AppLanguage.ODIA to "ଡ୍ରାଇଭରର QR କୋଡ୍ ସ୍କାନ କରନ୍ତୁ"
        ),
        "gps_permission_explanation" to mapOf(
            AppLanguage.ENGLISH to "GPS is required for accurate fare calculation.",
            AppLanguage.HINDI to "सटीक किराया जानने के लिए GPS ज़रूरी है",
            AppLanguage.ODIA to "ସଠିକ୍ ଭଡ଼ା ଜାଣିବା ପାଇଁ GPS ଜରୁରୀ ଅଟେ"
        ),
        "start_with_gps" to mapOf(
            AppLanguage.ENGLISH to "Start with GPS",
            AppLanguage.HINDI to "GPS चालू करके शुरू करो",
            AppLanguage.ODIA to "GPS ସହ ଆରମ୍ଭ କରନ୍ତୁ"
        ),
        "start_without_gps" to mapOf(
            AppLanguage.ENGLISH to "Start without GPS",
            AppLanguage.HINDI to "बिना GPS शुरू करो",
            AppLanguage.ODIA to "ବିନା GPS ରେ ଆରମ୍ଭ କରନ୍ତୁ"
        ),
        "driver_will_end" to mapOf(
            AppLanguage.ENGLISH to "Driver will end the trip",
            AppLanguage.HINDI to "Driver सफ़र खत्म करेगा",
            AppLanguage.ODIA to "ଡ୍ରାଇଭର ଯାତ୍ରା ଶେଷ କରିବେ"
        ),
        "done_payment_btn" to mapOf(
            AppLanguage.ENGLISH to "Done ✓",
            AppLanguage.HINDI to "हो गया ✓",
            AppLanguage.ODIA to "ହୋଇଗଲା ✓"
        ),
        
        // Errors
        "no_internet_banner" to mapOf(
            AppLanguage.ENGLISH to "No Internet — Trip is paused",
            AppLanguage.HINDI to "इंटरनेट नहीं है — सफ़र रुका हुआ है",
            AppLanguage.ODIA to "ଇଣ୍ଟରନେଟ୍ ନାହିଁ — ଯାତ୍ରା ଅଟକି ଯାଇଛି"
        ),
        "invalid_qr_error" to mapOf(
            AppLanguage.ENGLISH to "This QR is invalid or expired",
            AppLanguage.HINDI to "यह QR invalid है या expired है",
            AppLanguage.ODIA to "ଏହି QR କୋଡ୍ ଅବୈଧ କିମ୍ବା ମିଆଦ ପୂର୍ଣ୍ଣ ଅଟେ"
        ),
        
        // Settings / Form Labels
        "vehicle_number_label" to mapOf(
            AppLanguage.ENGLISH to "Vehicle Number",
            AppLanguage.HINDI to "गाड़ी का नंबर",
            AppLanguage.ODIA to "ଗାଡ଼ି ନମ୍ବର"
        ),
        "fare_rate_label" to mapOf(
            AppLanguage.ENGLISH to "Fare Rate (₹/km)",
            AppLanguage.HINDI to "किराया दर (₹/किमी)",
            AppLanguage.ODIA to "ଭଡ଼ା ହାର (₹/କିମି)"
        ),
        "upload_qr_btn" to mapOf(
            AppLanguage.ENGLISH to "Upload New QR",
            AppLanguage.HINDI to "नया QR अपलोड करो",
            AppLanguage.ODIA to "ନୂଆ QR ଅପଲୋଡ୍ କରନ୍ତୁ"
        ),
        "logout_btn" to mapOf(
            AppLanguage.ENGLISH to "Log Out",
            AppLanguage.HINDI to "लॉग आउट",
            AppLanguage.ODIA to "ଲଗ୍ ଆଉଟ୍"
        ),
        "full_name_label" to mapOf(
            AppLanguage.ENGLISH to "Full Name",
            AppLanguage.HINDI to "पूरा नाम",
            AppLanguage.ODIA to "ପୂରା ନାମ"
        ),
        "upi_id_label" to mapOf(
            AppLanguage.ENGLISH to "UPI ID for Payments",
            AppLanguage.HINDI to "भुगतान के लिए UPI ID",
            AppLanguage.ODIA to "ଭୁଗତାନ ପାଇଁ UPI ID"
        ),
        "save_btn" to mapOf(
            AppLanguage.ENGLISH to "Save Settings",
            AppLanguage.HINDI to "सेटिंग्स सुरक्षित करें",
            AppLanguage.ODIA to "ସେଟିଂସ ସଂରକ୍ଷଣ କରନ୍ତୁ"
        ),
        "switch_lang" to mapOf(
            AppLanguage.ENGLISH to "Switch Language / भाषा बदलें",
            AppLanguage.HINDI to "भाषा बदलें / Switch Language",
            AppLanguage.ODIA to "ଭାଷା ବଦଳାନ୍ତୁ"
        ),
        "auth_tab_phone" to mapOf(
            AppLanguage.ENGLISH to "Phone OTP",
            AppLanguage.HINDI to "फ़ोन OTP",
            AppLanguage.ODIA to "ଫୋନ୍ OTP"
        ),
        "auth_tab_email" to mapOf(
            AppLanguage.ENGLISH to "Email ID",
            AppLanguage.HINDI to "ईमेल आईडी",
            AppLanguage.ODIA to "ଇମେଲ୍ ଆଇଡି"
        ),
        "auth_tab_google" to mapOf(
            AppLanguage.ENGLISH to "Google",
            AppLanguage.HINDI to "गूगल",
            AppLanguage.ODIA to "ଗୁଗଲ୍"
        ),
        "auth_email_label" to mapOf(
            AppLanguage.ENGLISH to "Email Address",
            AppLanguage.HINDI to "ईमेल पता",
            AppLanguage.ODIA to "ଇମେଲ୍ ଠିକଣା"
        ),
        "auth_password_label" to mapOf(
            AppLanguage.ENGLISH to "Password (min 6 chars)",
            AppLanguage.HINDI to "पासवर्ड (कम से कम 6 अंक)",
            AppLanguage.ODIA to "ପାସୱାର୍ଡ (ଅତିକମରେ ୬ ଅଙ୍କ)"
        ),
        "auth_login_btn" to mapOf(
            AppLanguage.ENGLISH to "Log In",
            AppLanguage.HINDI to "लॉगिन करें",
            AppLanguage.ODIA to "ଲଗଇନ୍ କରନ୍ତୁ"
        ),
        "auth_signup_btn" to mapOf(
            AppLanguage.ENGLISH to "Sign Up / Create Account",
            AppLanguage.HINDI to "खाता बनाएं / साइनअप",
            AppLanguage.ODIA to "ଖାତା ଖୋଲନ୍ତୁ / ସାଇନଅପ୍"
        ),
        "auth_google_btn" to mapOf(
            AppLanguage.ENGLISH to "Continue with Google",
            AppLanguage.HINDI to "Google के साथ आगे बढ़ें",
            AppLanguage.ODIA to "Google ସହିତ ଆଗକୁ ବଢ଼ନ୍ତୁ"
        )
    )

    fun translate(key: String, language: AppLanguage): String {
        val entry = dictionary[key] ?: return key
        return entry[language] ?: entry[AppLanguage.ENGLISH] ?: key
    }
}
