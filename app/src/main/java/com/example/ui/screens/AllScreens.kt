package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.FirebaseRepo
import com.example.data.Trip
import com.example.data.UserProfile
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.QrCodeAnalyzer
import com.example.util.QrUtil
import com.example.util.Translator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- LOGIN / AUTH SCREEN ---
@Composable
fun AuthScreen(
    repo: FirebaseRepo,
    language: AppLanguage,
    onAuthenticated: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf("phone") } // "phone", "email", "google"
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    
    // Email inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var verificationId by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(24.dp)
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            // Stylized Logo Icon (Saffron Auto Wheel)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(SaffronPrimary.copy(alpha = 0.15f), shape = CircleShape)
                    .border(2.dp, SaffronPrimary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "AutoQR Logo",
                    tint = SaffronPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Translator.translate("auth_welcome", language),
                style = Typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = Translator.translate("auth_subtitle", language),
                style = Typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "phone" to Translator.translate("auth_tab_phone", language),
                    "email" to Translator.translate("auth_tab_email", language),
                    "google" to Translator.translate("auth_tab_google", language)
                ).forEach { (tabKey, tabLabel) ->
                    val isSelected = activeTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) SaffronPrimary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                activeTab = tabKey
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (activeTab) {
                "phone" -> {
                    if (!isOtpSent) {
                        // Phone Number Entry Form
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it.filter { c -> c.isDigit() }.take(10) },
                            label = { Text(Translator.translate("auth_phone_label", language), color = TextSecondary) },
                            prefix = { Text("+91 ", color = Color.White, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaffronPrimary,
                                unfocusedBorderColor = SurfaceElevated,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (phoneNumber.length == 10) {
                                    errorMessage = null
                                    repo.sendOtp(
                                        phoneNumber = "+91$phoneNumber",
                                        onCodeSent = { vid ->
                                            verificationId = vid
                                            isOtpSent = true
                                        },
                                        onFailure = { err -> errorMessage = err }
                                    )
                                } else {
                                    errorMessage = if (language == AppLanguage.HINDI) "कृपया 10 अंकों का वैध नंबर दर्ज करें।" else if (language == AppLanguage.ODIA) "ଦୟାକରି ୧୦ ଅଙ୍କର ବୈଧ ନମ୍ବର ଦିଅନ୍ତୁ।" else "Please enter a valid 10-digit phone number."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("send_otp_button")
                        ) {
                            Text(
                                text = Translator.translate("auth_send_otp_btn", language),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        // OTP Entry Form
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text(Translator.translate("auth_otp_label", language), color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaffronPrimary,
                                unfocusedBorderColor = SurfaceElevated,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = if (language == AppLanguage.HINDI) "सिम्युलेटर मोड: बाईपास के लिए कोई भी 6 अंक दर्ज करें।" else if (language == AppLanguage.ODIA) "ସିମ୍ୟୁଲେଟର ମୋଡ୍: ବାଇପାସ୍ ପାଇଁ ଯେକୌଣସି ୬ ଅଙ୍କ ଦିଅନ୍ତୁ।" else "Simulator Mode: Enter any 6 digits to bypass.",
                            fontSize = 12.sp,
                            color = WarningAmber
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (otpCode.length == 6) {
                                    errorMessage = null
                                    isVerifying = true
                                    repo.verifyOtp(
                                        verificationId = verificationId,
                                        otpCode = otpCode,
                                        phoneNumber = "+91$phoneNumber",
                                        onSuccess = { uid ->
                                            isVerifying = false
                                            repo.loginSuccess(uid, phone = "+91$phoneNumber") {
                                                onAuthenticated(uid)
                                            }
                                        },
                                        onFailure = { err ->
                                            isVerifying = false
                                            errorMessage = err
                                        }
                                    )
                                } else {
                                    errorMessage = if (language == AppLanguage.HINDI) "कृपया 6-अंकीय OTP कोड दर्ज करें।" else if (language == AppLanguage.ODIA) "ଦୟାକରି ୬-ଅଙ୍କର OTP ଦିଅନ୍ତୁ।" else "Please enter a 6-digit OTP code."
                                }
                            },
                            enabled = !isVerifying,
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("verify_otp_button")
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = Translator.translate("auth_verify_btn", language),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = { isOtpSent = false }) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "फ़ोन नंबर बदलें" else if (language == AppLanguage.ODIA) "ଫୋନ୍ ନମ୍ବର ବଦଳାନ୍ତୁ" else "Change Phone Number",
                                color = SaffronPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                "email" -> {
                    // Email input field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text(Translator.translate("auth_email_label", language), color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password input field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(Translator.translate("auth_password_label", language), color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.contains("@") && password.length >= 6) {
                                errorMessage = null
                                isVerifying = true
                                if (isSignUpMode) {
                                    repo.signUpWithEmailPassword(
                                        email = email,
                                        password = password,
                                        onSuccess = { uid ->
                                            isVerifying = false
                                            repo.loginSuccess(uid, email = email) {
                                                onAuthenticated(uid)
                                            }
                                        },
                                        onFailure = { err ->
                                            isVerifying = false
                                            errorMessage = err
                                        }
                                    )
                                } else {
                                    repo.loginWithEmailPassword(
                                        email = email,
                                        password = password,
                                        onSuccess = { uid ->
                                            isVerifying = false
                                            repo.loginSuccess(uid, email = email) {
                                                onAuthenticated(uid)
                                            }
                                        },
                                        onFailure = { err ->
                                            isVerifying = false
                                            errorMessage = err
                                        }
                                    )
                                }
                            } else {
                                errorMessage = if (language == AppLanguage.HINDI) "कृपया एक वैध ईमेल और कम से कम 6 अंकों का पासवर्ड दर्ज करें।" else if (language == AppLanguage.ODIA) "ଦୟାକରି ଏକ ବୈଧ ଇମେଲ୍ ଏବଂ ଅତିକମରେ ୬ ଅଙ୍କର ପାସୱାର୍ଡ ଦିଅନ୍ତୁ।" else "Please enter a valid email and at least 6 characters for password."
                            }
                        },
                        enabled = !isVerifying,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("email_auth_button")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSignUpMode) Translator.translate("auth_signup_btn", language) else Translator.translate("auth_login_btn", language),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                        Text(
                            text = if (isSignUpMode) {
                                if (language == AppLanguage.HINDI) "पहले से खाता है? लॉगिन करें" else if (language == AppLanguage.ODIA) "ପୂର୍ବରୁ ଆକାଉଣ୍ଟ ଅଛି? ଲଗଇନ୍ କରନ୍ତୁ" else "Already have an account? Log In"
                            } else {
                                if (language == AppLanguage.HINDI) "खाता नहीं है? साइनअप करें" else if (language == AppLanguage.ODIA) "ଆକାଉଣ୍ଟ ନାହିଁ? ସାଇନଅପ୍ କରନ୍ତୁ" else "Don't have an account? Sign Up"
                            },
                            color = SaffronPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                "google" -> {
                    Text(
                        text = if (language == AppLanguage.HINDI) "सुरक्षित रूप से लॉगिन करने के लिए Google के साथ आगे बढ़ें।" else if (language == AppLanguage.ODIA) "ସୁରକ୍ଷିତ ଭାବେ ଲଗଇନ୍ କରିବା ପାଇଁ Google ସହିତ ଆଗକୁ ବଢ଼ନ୍ତୁ।" else "Continue with Google for safe, instant, and secure auto fare management.",
                        style = Typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isVerifying = true
                            errorMessage = null
                            repo.loginWithGoogle(
                                email = "yatri.guest@gmail.com",
                                name = "Yatri Guest",
                                onSuccess = { uid ->
                                    isVerifying = false
                                    repo.loginSuccess(uid, email = "yatri.guest@gmail.com", displayName = "Yatri Guest") {
                                        onAuthenticated(uid)
                                    }
                                },
                                onFailure = { err ->
                                    isVerifying = false
                                    errorMessage = err
                                }
                            )
                        },
                        enabled = !isVerifying,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SurfaceElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("google_login_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Icon",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = Translator.translate("auth_google_btn", language),
                                color = NearBlackBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- ROLE SELECTION SCREEN ---
@Composable
fun RoleSelectionScreen(
    repo: FirebaseRepo,
    uid: String,
    phone: String,
    language: AppLanguage,
    onRoleCompleted: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var fareRate by remember { mutableStateOf("18.0") }
    var upiId by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<String?>(null) } // "driver" | "passenger"
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(24.dp)
            .testTag("role_selection_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            Text(
                text = Translator.translate("role_select_title", language),
                style = Typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (selectedRole == null) {
                // Large buttons for role selection
                Button(
                    onClick = { selectedRole = "driver" },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .testTag("role_driver_button")
                ) {
                    Text(
                        text = Translator.translate("role_driver_btn", language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { selectedRole = "passenger" },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SaffronPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .testTag("role_passenger_button")
                ) {
                    Text(
                        text = Translator.translate("role_passenger_btn", language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            } else {
                // Detail profile entry based on role
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Translator.translate("full_name_label", language), color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        unfocusedBorderColor = SurfaceElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input")
                )

                if (selectedRole == "driver") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it.uppercase() },
                        label = { Text(Translator.translate("vehicle_number_label", language), color = TextSecondary) },
                        placeholder = { Text("e.g. OD-02-AQ-9988", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vehicle_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = fareRate,
                        onValueChange = { fareRate = it },
                        label = { Text(Translator.translate("fare_rate_label", language), color = TextSecondary) },
                        prefix = { Text("₹ ", color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fare_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it.filter { c -> !c.isWhitespace() } },
                        label = { Text(Translator.translate("upi_id_label", language), color = TextSecondary) },
                        placeholder = { Text("e.g. rakesh@paytm", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upi_input")
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (name.trim().isNotEmpty()) {
                            val rate = fareRate.toDoubleOrNull() ?: 18.0
                            if (selectedRole == "driver" && (vehicleNumber.trim().isEmpty() || upiId.trim().isEmpty())) {
                                errorMessage = "कृपया सभी ड्राइवर विवरण भरें।"
                            } else {
                                repo.selectRoleAndCreateProfile(
                                    uid = uid,
                                    phone = phone,
                                    name = name.trim(),
                                    role = selectedRole!!,
                                    vehicle = if (selectedRole == "driver") vehicleNumber.trim() else null,
                                    rate = if (selectedRole == "driver") rate else null,
                                    upiId = if (selectedRole == "driver") upiId.trim() else null
                                )
                                onRoleCompleted()
                            }
                        } else {
                            errorMessage = "कृपया अपना पूरा नाम दर्ज करें।"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("complete_role_button")
                ) {
                    Text(
                        text = "प्रोफ़ाइल सहेजें",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { selectedRole = null }) {
                    Text("भूमिका फिर से चुनें", color = TextSecondary)
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- DRIVER HOME DASHBOARD ---
@Composable
fun DriverDashboardScreen(
    repo: FirebaseRepo,
    language: AppLanguage,
    onStartTrip: () -> Unit,
    onGoToSettings: () -> Unit
) {
    val user = repo.currentUser.collectAsState().value
    val trips = repo.myTrips.collectAsState().value

    val todayTripsCount = trips.size
    val todayEarningsSum = trips.sumOf { it.finalFare ?: 0.0 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(16.dp)
            .testTag("driver_dashboard"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            
            // Welcome Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "नमस्ते, ${user?.name ?: "चालक"}",
                        style = Typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "AutoQR Smart Driver Hub",
                        style = Typography.titleMedium,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onGoToSettings) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = SaffronPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ऑटो विवरण",
                        style = Typography.titleMedium,
                        color = SaffronPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "गाड़ी नंबर", fontSize = 12.sp, color = TextSecondary)
                            Text(text = user?.vehicleNumber ?: "N/A", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text(text = "मूल्य दर", fontSize = 12.sp, color = TextSecondary)
                            Text(text = "₹${user?.fareRatePerKm ?: "18"}/km", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BIG ACTION START BUTTON
            Button(
                onClick = onStartTrip,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("start_trip_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translator.translate("start_trip_btn", language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Today Summary Header
            Text(
                text = "आज का विवरण",
                style = Typography.headlineMedium,
                color = Color.White,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Count Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = Translator.translate("today_trips", language), fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$todayTripsCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SaffronPrimary)
                    }
                }

                // Earnings Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = Translator.translate("today_earnings", language), fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "₹${todayEarningsSum.toInt()}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = UpiSuccess)
                    }
                }
            }
        }
    }
}

// --- DRIVER MY QR SCREEN ---
@Composable
fun DriverQrScreen(
    repo: FirebaseRepo,
    language: AppLanguage
) {
    val user = repo.currentUser.collectAsState().value
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            repo.uploadUpiQrImage(
                uri = uri,
                onSuccess = { isUploading = false },
                onFailure = { isUploading = false }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(24.dp)
            .testTag("driver_qr_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "मेरा UPI QR कोड",
                style = Typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Display QR Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(280.dp)
                    .border(1.dp, SurfaceElevated, shape = RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.upiQrImageUrl != null && user.upiQrImageUrl != "default" && user.upiQrImageUrl != "default_qr") {
                        AsyncImage(
                            model = user.upiQrImageUrl,
                            contentDescription = "Uploaded UPI QR",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        // Dynamically generate a stylized UPI QR vector representation using zxing
                        val upiStr = "upi://pay?pa=${user?.upiId ?: "autoqr@pay"}&pn=${user?.name ?: "Driver"}&mc=0000"
                        val qrBitmap = remember(upiStr) { QrUtil.generateQrCode(upiStr) }
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Generated UPI QR",
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "QR", tint = SaffronPrimary, modifier = Modifier.size(120.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // UPI ID Display Label
            Text(
                text = "UPI ID: ${user?.upiId ?: "rakesh@paytm"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // New QR upload trigger button
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SaffronPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("upload_qr_button")
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = SaffronPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Upload", tint = SaffronPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Translator.translate("upload_qr_btn", language),
                            color = SaffronPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- DRIVER TRIP WAITING SCREEN (SHOWS SCAN-ME QR) ---
@Composable
fun DriverTripWaitingScreen(
    repo: FirebaseRepo,
    tripId: String,
    language: AppLanguage,
    onActiveTripStarted: () -> Unit,
    onCancel: () -> Unit
) {
    val trip = repo.currentTrip.collectAsState().value

    // Real-time listener for status change to "active"
    LaunchedEffect(trip?.status) {
        if (trip?.status == "active") {
            onActiveTripStarted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(24.dp)
            .testTag("driver_waiting_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "यात्री स्कैन कर रहा है",
                style = Typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Translator.translate("passenger_scan_instruction", language),
                style = Typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Displays generated Deep Link QR Code
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(280.dp)
                    .padding(8.dp)
            ) {
                val deepLink = "autoqr://trip/$tripId"
                val qrBitmap = remember(deepLink) { QrUtil.generateQrCode(deepLink) }
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Trip QR Code",
                        modifier = Modifier.fillMaxSize().padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "सफ़र आईडी: $tripId",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = SaffronPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            TextButton(onClick = onCancel) {
                Text("सफ़र रद्द करें", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// --- ACTIVE TRIP SCREEN (DRIVER & PASSENGER VIEWS SHARED CONFIG) ---
@Composable
fun DriverActiveTripScreen(
    repo: FirebaseRepo,
    language: AppLanguage,
    onTripEnded: () -> Unit
) {
    val trip = repo.currentTrip.collectAsState().value
    val distance = trip?.finalDistanceKm ?: 0.0
    val fare = trip?.finalFare ?: 0.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .testTag("driver_active_screen"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translator.translate("active_trip_title", language),
                    style = Typography.headlineMedium,
                    fontSize = 18.sp,
                    color = Color.White
                )
                GpsStatusPill(gpsMode = trip?.gpsMode ?: "driver_only", language = language)
            }

            // Interactive Map Radar
            LiveMapCanvas(
                driverLoc = trip?.driverLocation,
                passengerLoc = trip?.passengerLocation,
                driverPath = trip?.driverPath ?: emptyList(),
                passengerPath = trip?.passengerPath ?: emptyList(),
                gpsMode = trip?.gpsMode ?: "driver_only"
            )

            // Billing info card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(SurfaceCard)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "दूरी", color = TextSecondary, fontSize = 14.sp)
                            Text(
                                text = String.format("%.2f km", distance),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "किराया", color = TextSecondary, fontSize = 14.sp)
                            Text(
                                text = "₹${fare.toInt()}",
                                style = FareAmountStyle,
                                color = SaffronPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // End trip action trigger button
                    Button(
                        onClick = {
                            repo.endTrip(trip?.tripId ?: "")
                            onTripEnded()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("end_trip_button")
                    ) {
                        Text(
                            text = Translator.translate("end_trip_btn", language),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// --- DRIVER TRIP COMPLETED SUMMARY SCREEN ---
@Composable
fun DriverTripCompletedScreen(
    repo: FirebaseRepo,
    language: AppLanguage,
    onDone: () -> Unit
) {
    val trip = repo.currentTrip.collectAsState().value
    val user = repo.currentUser.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(24.dp)
            .testTag("driver_completed_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = UpiSuccess,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Translator.translate("trip_completed", language),
                style = Typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Summary Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "कुल किराया", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "₹${trip?.finalFare?.toInt() ?: 0}",
                        style = FareAmountStyle,
                        color = UpiSuccess
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = SurfaceElevated)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "दूरी:", color = TextSecondary)
                        Text(text = String.format("%.2f km", trip?.finalDistanceKm ?: 0.0), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "यात्री का नाम:", color = TextSecondary)
                        Text(text = trip?.passengerName ?: "Yatri", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Show payment QR for easy scan
            Text(text = "यात्री को भुगतान करने दें", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(160.dp).padding(4.dp)
            ) {
                val upiStr = "upi://pay?pa=${user?.upiId ?: "pay@autoqr"}&pn=${user?.name ?: "Driver"}&am=${trip?.finalFare ?: "10"}"
                val qrBitmap = remember(upiStr) { QrUtil.generateQrCode(upiStr, 256, 256) }
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "UPI Payment QR",
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    repo.clearActiveTrip()
                    onDone()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("new_trip_button")
            ) {
                Text(
                    text = Translator.translate("new_trip_nav", language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// --- TRIP HISTORY LIST SCREEN (SHARED FOR BOTH DRIVER & PASSENGER) ---
@Composable
fun TripHistoryScreen(
    repo: FirebaseRepo,
    language: AppLanguage
) {
    val trips = repo.myTrips.collectAsState().value
    var expandedTripId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(16.dp)
            .testTag("history_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        if (trips.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.List, contentDescription = "History empty", tint = SurfaceElevated, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "कोई पिछला सफ़र नहीं मिला", color = TextSecondary, style = Typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = Translator.translate("nav_history", language),
                        style = Typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                items(trips) { trip ->
                    val isExpanded = expandedTripId == trip.tripId
                    val dateStr = remember(trip.createdAt) {
                        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        sdf.format(Date(trip.createdAt))
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedTripId = if (isExpanded) null else trip.tripId }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = dateStr, fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (repo.currentUser.collectAsState().value?.role == "driver") {
                                            "यात्री: ${trip.passengerName ?: "Yatri"}"
                                        } else {
                                            "ड्राइवर: ${trip.driverName}"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "₹${trip.finalFare?.toInt() ?: 0}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UpiSuccess
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    Divider(color = SurfaceElevated)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "सफ़र दूरी:", color = TextSecondary, fontSize = 14.sp)
                                        Text(text = String.format("%.2f km", trip.finalDistanceKm ?: 0.0), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "ऑटो गाड़ी नंबर:", color = TextSecondary, fontSize = 14.sp)
                                        Text(text = trip.vehicleNumber, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "स्थिति:", color = TextSecondary, fontSize = 14.sp)
                                        Text(
                                            text = if (trip.status == "paid") "सफल भुगतान ✓" else "पूरा हुआ",
                                            color = if (trip.status == "paid") UpiSuccess else WarningAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SETTINGS CONFIG EDITOR ---
@Composable
fun DriverSettingsScreen(
    repo: FirebaseRepo,
    language: AppLanguage,
    onLanguageSwitched: (AppLanguage) -> Unit
) {
    val user = repo.currentUser.collectAsState().value
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var vehicle by remember(user) { mutableStateOf(user?.vehicleNumber ?: "") }
    var fareRate by remember(user) { mutableStateOf((user?.fareRatePerKm ?: 18.0).toString()) }
    var upiId by remember(user) { mutableStateOf(user?.upiId ?: "") }
    var showSuccessMsg by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(24.dp)
            .testTag("settings_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            Text(
                text = Translator.translate("nav_settings", language),
                style = Typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Settings Block
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(Translator.translate("full_name_label", language), color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronPrimary,
                    unfocusedBorderColor = SurfaceElevated,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (user?.role == "driver") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { vehicle = it.uppercase() },
                    label = { Text(Translator.translate("vehicle_number_label", language), color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        unfocusedBorderColor = SurfaceElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = fareRate,
                    onValueChange = { fareRate = it },
                    label = { Text(Translator.translate("fare_rate_label", language), color = TextSecondary) },
                    prefix = { Text("₹ ", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        unfocusedBorderColor = SurfaceElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text(Translator.translate("upi_id_label", language), color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        unfocusedBorderColor = SurfaceElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MULTI-LANGUAGE DYNAMIC LANGUAGE SWITCHER SELECTOR
            Text(
                text = Translator.translate("switch_lang", language),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.values().forEach { langOption ->
                    val isSelected = langOption == language
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) SaffronPrimary else SurfaceCard,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) SaffronPrimary else SurfaceElevated,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onLanguageSwitched(langOption) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = langOption.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    repo.updateProfile(
                        name = name,
                        vehicle = if (user?.role == "driver") vehicle else null,
                        rate = if (user?.role == "driver") fareRate.toDoubleOrNull() else null,
                        upiId = if (user?.role == "driver") upiId else null
                    )
                    showSuccessMsg = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_settings_button")
            ) {
                Text(
                    text = Translator.translate("save_btn", language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showSuccessMsg) {
                LaunchedEffect(Unit) {
                    delay(3000)
                    showSuccessMsg = false
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "सेटिंग्स सफलतापूर्वक सहेजी गईं! ✓",
                    color = UpiSuccess,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = SurfaceElevated)
            Spacer(modifier = Modifier.height(24.dp))

            // Log out button
            Button(
                onClick = { repo.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("logout_button")
            ) {
                Text(
                    text = Translator.translate("logout_btn", language),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- PASSENGER CORE CAMERA SCAN HOME SCREEN ---
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PassengerDashboardScreen(
    repo: FirebaseRepo,
    language: AppLanguage,
    onTripScanned: (String) -> Unit
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(android.Manifest.permission.CAMERA)
    )

    var manualTripId by remember { mutableStateOf("") }
    var scanError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .testTag("passenger_scan_screen"),
        contentAlignment = Alignment.Center
    ) {
        if (permissionsState.allPermissionsGranted) {
            // Real camera QR Scanner
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(onQrScanned = { deepLink ->
                    // Deep links format is autoqr://trip/{tripId}
                    try {
                        val parsedUri = Uri.parse(deepLink)
                        val tripId = parsedUri.lastPathSegment
                        if (tripId != null) {
                            onTripScanned(tripId)
                        } else {
                            scanError = "अमान्य QR कोड प्रारूप।"
                        }
                    } catch (e: Exception) {
                        scanError = "स्कैन करने में समस्या: ${e.localizedMessage}"
                    }
                })

                // Beautiful Overlay Frame over scanner
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    // Frame cut-out indicator in center
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .border(4.dp, SaffronPrimary, shape = RoundedCornerShape(24.dp))
                            .align(Alignment.Center)
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 120.dp)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Translator.translate("passenger_scan_title", language),
                            color = Color.White,
                            style = Typography.headlineMedium,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        scanError?.let { err ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = err, color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            // Requests permissions nicely
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Camera", tint = SaffronPrimary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "QR कोड स्कैन करने के लिए कैमरा अनुमति की आवश्यकता है",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionsState.launchMultiplePermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("अनुमति दें", color = Color.White)
                }
            }
        }

        // Float manual bypass input drawer (Ideal for simulator environments without camera feeds)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualTripId,
                        onValueChange = { manualTripId = it },
                        placeholder = { Text("या सफ़र ID दर्ज करें...", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).height(50.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (manualTripId.trim().isNotEmpty()) {
                                onTripScanned(manualTripId.trim())
                            }
                        },
                        modifier = Modifier
                            .background(SaffronPrimary, shape = CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Submit", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, QrCodeAnalyzer { qrText ->
                            onQrScanned(qrText)
                        })
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

// --- PASSENGER TRIP PREVIEW SCREEN ---
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PassengerTripPreviewScreen(
    repo: FirebaseRepo,
    tripId: String,
    language: AppLanguage,
    onTripConfirmed: () -> Unit,
    onCancel: () -> Unit
) {
    var scannedTrip by remember { mutableStateOf<Trip?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val user = repo.currentUser.collectAsState().value
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(tripId) {
        repo.scanTripQr(
            tripId = tripId,
            passengerProfile = user ?: UserProfile(),
            onSuccess = { trip ->
                scannedTrip = trip
                isLoading = false
            },
            onFailure = { err ->
                errorMessage = err
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(24.dp)
            .testTag("passenger_preview_screen"),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = SaffronPrimary)
        } else if (errorMessage != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)) {
                    Text("वापस जाएं", color = Color.White)
                }
            }
        } else {
            scannedTrip?.let { trip ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "सफ़र शुरू करने की पुष्टि करें",
                        style = Typography.headlineMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detail Driver Summary
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(48.dp).background(SaffronPrimary, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = "Driver", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = trip.driverName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "चालक / Driver", fontSize = 12.sp, color = TextSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = SurfaceElevated)
                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "ऑटो गाड़ी नंबर:", color = TextSecondary)
                                Text(text = trip.vehicleNumber, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "किराया दर:", color = TextSecondary)
                                Text(text = "₹${trip.fareRatePerKm}/km", color = SaffronPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // GPS Permission request info
                    Text(
                        text = Translator.translate("gps_permission_explanation", language),
                        color = WarningAmber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // GPS confirm button
                    Button(
                        onClick = {
                            if (locationPermissionState.allPermissionsGranted) {
                                repo.confirmPassengerTrip(trip.tripId, user?.uid ?: "user_pass", user?.name ?: "Yatri", true)
                                onTripConfirmed()
                            } else {
                                locationPermissionState.launchMultiplePermissionRequest()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("confirm_gps_button")
                    ) {
                        Text(
                            text = Translator.translate("start_with_gps", language),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // No GPS secondary button
                    Button(
                        onClick = {
                            repo.confirmPassengerTrip(trip.tripId, user?.uid ?: "user_pass", user?.name ?: "Yatri", false)
                            onTripConfirmed()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SaffronPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("confirm_no_gps_button")
                    ) {
                        Text(
                            text = Translator.translate("start_without_gps", language),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onCancel) {
                        Text("रद्द करें", color = Color.Red)
                    }
                }
            }
        }
    }
}

// --- PASSENGER ACTIVE TRIP SCREEN ---
@Composable
fun PassengerActiveTripScreen(
    repo: FirebaseRepo,
    language: AppLanguage,
    onTripCompleted: () -> Unit
) {
    val trip = repo.currentTrip.collectAsState().value
    val distance = trip?.finalDistanceKm ?: 0.0
    val fare = trip?.finalFare ?: 0.0

    // Listening to state complete transitions
    LaunchedEffect(trip?.status) {
        if (trip?.status == "completed") {
            onTripCompleted()
        }
    }

    // Interactive animated counter for live fare counts
    val animFare = remember { Animatable(0f) }
    LaunchedEffect(fare) {
        animFare.animateTo(
            targetValue = fare.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .testTag("passenger_active_screen"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translator.translate("active_trip_title", language),
                    style = Typography.headlineMedium,
                    fontSize = 18.sp,
                    color = Color.White
                )
                GpsStatusPill(gpsMode = trip?.gpsMode ?: "driver_only", language = language)
            }

            // Radar Map Layout
            LiveMapCanvas(
                driverLoc = trip?.driverLocation,
                passengerLoc = trip?.passengerLocation,
                driverPath = trip?.driverPath ?: emptyList(),
                passengerPath = trip?.passengerPath ?: emptyList(),
                gpsMode = trip?.gpsMode ?: "driver_only"
            )

            // Dynamic Counter Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(SurfaceCard)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "दूरी", color = TextSecondary, fontSize = 14.sp)
                            Text(
                                text = String.format("%.2f km", distance),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "किराया", color = TextSecondary, fontSize = 14.sp)
                            Text(
                                text = "₹${animFare.value.toInt()}",
                                style = FareAmountStyle,
                                color = SaffronPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Static Passenger Instruction Tag
                    Box(
                        modifier = Modifier
                            .background(WarningAmber.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, WarningAmber, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Translator.translate("driver_will_end", language),
                            color = WarningAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// --- PASSENGER PAYMENT QR SCAN/VERIFY ACTION ---
@Composable
fun PassengerPaymentScreen(
    repo: FirebaseRepo,
    language: AppLanguage,
    onPaymentDone: () -> Unit
) {
    val trip = repo.currentTrip.collectAsState().value
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlackBg)
            .padding(24.dp)
            .testTag("passenger_payment_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "किराया भुगतान करें",
                style = Typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Big Fare Meter
            Text(
                text = "₹${trip?.finalFare?.toInt() ?: 0}",
                style = FareAmountStyle,
                color = UpiSuccess,
                fontSize = 56.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Math breakdown
            Text(
                text = "${String.format("%.2f km", trip?.finalDistanceKm ?: 0.0)} × ₹${trip?.fareRatePerKm ?: 0.0}/km = ₹${trip?.finalFare?.toInt() ?: 0}",
                color = TextSecondary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Displays Driver's UPI QR Code
            Text(
                text = Translator.translate("payment_qr_instruction", language),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(220.dp)
                    .padding(8.dp)
            ) {
                // Generates deep link upi scheme for driver
                val upiStr = "upi://pay?pa=${trip?.vehicleNumber ?: "pay@autoqr"}&pn=${trip?.driverName ?: "Driver"}&am=${trip?.finalFare ?: "10"}"
                val qrBitmap = remember(upiStr) { QrUtil.generateQrCode(upiStr, 256, 256) }
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Driver UPI QR",
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    repo.markTripAsPaid(trip?.tripId ?: "")
                    repo.clearActiveTrip()
                    onPaymentDone()
                },
                colors = ButtonDefaults.buttonColors(containerColor = UpiSuccess),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("done_payment_button")
            ) {
                Text(
                    text = Translator.translate("done_payment_btn", language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

// --- PASSENGER TRIP HISTORY ---
@Composable
fun PassengerHistoryScreen(
    repo: FirebaseRepo,
    language: AppLanguage
) {
    TripHistoryScreen(repo = repo, language = language)
}
