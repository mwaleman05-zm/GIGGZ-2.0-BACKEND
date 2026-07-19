package com.example.ui.screens

import android.widget.Toast
import com.example.ui.components.showSafeToast
import com.example.ui.components.ImageAttachmentPicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GiggzViewModel
import com.example.ui.theme.GiggzGold
import com.example.ui.theme.GiggzGreen
import androidx.compose.ui.focus.onFocusChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: GiggzViewModel,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val lastRole by viewModel.lastRole.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Brand Logo
                Image(
                    painter = painterResource(id = R.drawable.giggz_logo_refined_with_z_1783929293429),
                    contentDescription = "Giggz Logo",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(24.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Giggz",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = GiggzGreen,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Smart Job Marketplace & Community Trade",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Hero Image Flat Illustration
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_giggz_hero_clean_1784132285707),
                        contentDescription = "Workers collaborating",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Onboarding Welcome Tagline
                Text(
                    text = "Connect with top skilled workers. Discover fast, hyper-local community trades on the Ama Sampo.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onNavigate("login") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Fast Mock Logins Panel for Reviewers
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GiggzGreen.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Developer Fast-Testing Panel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GiggzGreen
                        )
                        Text(
                            text = "Click to log in instantly to any simulated role:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.login("john.carpenter@giggz.com", "password") { _, _ -> }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Worker", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    viewModel.login("buildtech@giggz.com", "password") { _, _ -> }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GiggzGold),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Employer", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: GiggzViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val lastRole by viewModel.lastRole.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var selectedRole by remember(lastRole) { mutableStateOf(lastRole ?: "Worker") } // "Worker", "Employer"
    var email by remember { mutableStateOf("john.carpenter@giggz.com") }
    var password by remember { mutableStateOf("password") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Custom Palette for Dark Mode
    val darkPrimaryBg = Color(0xFF121417) // very dark charcoal
    val darkPrimaryAccent = Color(0xFF10B981) // Mint Green
    val darkSecondaryAccent = Color(0xFFFBBF24) // Gold
    val darkText = Color.White
    val darkSecondaryText = Color(0xFF9CA3AF) // Light Gray

    // Prefill whenever the user taps on a role tab
    LaunchedEffect(selectedRole) {
        when (selectedRole) {
            "Worker" -> {
                email = "john.carpenter@giggz.com"
                password = "password"
            }
            "Employer" -> {
                email = "buildtech@giggz.com"
                password = "password"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(if (isDarkMode) darkPrimaryBg else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E2228) else MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.5.dp, if (isDarkMode) Color(0xFF2E333D) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.giggz_logo_refined_with_z_1783929293429),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to Giggz",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "jobs near you, opportunities everywhere",
                    fontSize = 13.sp,
                    color = if (isDarkMode) darkSecondaryText else Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
                )

                // Modern segmented role selector
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) darkPrimaryBg else Color(0xFFF3F4F6)), // adapted track
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val availableRoles = listOf("Worker", "Employer")
                        availableRoles.forEach { role ->
                            val isSelected = selectedRole == role
                            val activeBg = if (isSelected) (if (isDarkMode) Color(0xFF1E2228) else Color.White) else Color.Transparent
                            val activeText = if (isSelected) (if (isDarkMode) darkPrimaryAccent else GiggzGreen) else (if (isDarkMode) darkSecondaryText else Color.Gray)
                            val activeShadow = 0.dp

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = role },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = activeBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = activeShadow)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = when (role) {
                                                "Worker" -> Icons.Filled.Engineering
                                                "Employer" -> Icons.Filled.Business
                                                else -> Icons.Filled.Badge
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) (if (isDarkMode) darkPrimaryAccent else GiggzGreen) else (if (isDarkMode) darkSecondaryText else Color.Gray),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = role,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = activeText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Role Info Badge using gold secondary accent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(if (isDarkMode) darkSecondaryAccent.copy(alpha = 0.1f) else GiggzGold.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, if (isDarkMode) darkSecondaryAccent.copy(alpha = 0.3f) else GiggzGold.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isDarkMode) darkSecondaryAccent else GiggzGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logging in as: $selectedRole (Demo credentials prefilled)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkMode) darkSecondaryAccent else Color(0xFFD97706)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = if (isDarkMode) darkPrimaryAccent else GiggzGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDarkMode) darkText else Color.Black,
                        unfocusedTextColor = if (isDarkMode) darkText else Color.Black,
                        focusedLabelColor = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                        unfocusedLabelColor = if (isDarkMode) darkSecondaryText else Color.Gray,
                        focusedBorderColor = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF2E333D) else Color(0xFFE5E7EB),
                        focusedContainerColor = if (isDarkMode) darkPrimaryBg else Color.White,
                        unfocusedContainerColor = if (isDarkMode) darkPrimaryBg else Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = if (isDarkMode) darkPrimaryAccent else GiggzGreen) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = if (isDarkMode) darkPrimaryAccent else GiggzGreen)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDarkMode) darkText else Color.Black,
                        unfocusedTextColor = if (isDarkMode) darkText else Color.Black,
                        focusedLabelColor = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                        unfocusedLabelColor = if (isDarkMode) darkSecondaryText else Color.Gray,
                        focusedBorderColor = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF2E333D) else Color(0xFFE5E7EB),
                        focusedContainerColor = if (isDarkMode) darkPrimaryBg else Color.White,
                        unfocusedContainerColor = if (isDarkMode) darkPrimaryBg else Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        context.showSafeToast("Password reset link sent to $email (Simulated)")
                    }) {
                        Text("Forgot Password?", color = if (isDarkMode) Color(0xFF10B981) else GiggzGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (email.isBlank() || password.isBlank()) {
                            context.showSafeToast("Please complete all fields.")
                            return@Button
                        }
                        isLoading = true
                        viewModel.login(email, password) { success, message ->
                            isLoading = false
                            context.showSafeToast(message)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) darkPrimaryAccent else GiggzGreen),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Don't have an account? ", fontSize = 13.sp, color = if (isDarkMode) darkSecondaryText else Color.Gray)
                    Text(
                        text = "Register",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                        modifier = Modifier.clickable { onNavigate("register") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(if (isDarkMode) Color(0xFF2E333D) else Color(0xFFE5E7EB)))
                    Text(
                        text = "OR DEMO ACCESS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) darkSecondaryText else Color.Gray,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        letterSpacing = 1.sp
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(if (isDarkMode) Color(0xFF2E333D) else Color(0xFFE5E7EB)))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.login("john.carpenter@giggz.com", "password") { _, _ -> }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) darkPrimaryAccent.copy(alpha = 0.12f) else GiggzGreen.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, if (isDarkMode) darkPrimaryAccent else GiggzGreen),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Worker Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) darkPrimaryAccent else GiggzGreen)
                    }

                    Button(
                        onClick = {
                            viewModel.login("buildtech@giggz.com", "password") { _, _ -> }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) darkSecondaryAccent.copy(alpha = 0.12f) else GiggzGold.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, if (isDarkMode) darkSecondaryAccent else GiggzGold),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Employer Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) darkSecondaryAccent else GiggzGold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign In Mock Button
                OutlinedButton(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.loginWithGoogle { success, message ->
                            context.showSafeToast(message)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2E333D) else Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isDarkMode) darkPrimaryBg else Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = if (isDarkMode) darkSecondaryAccent else GiggzGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google (Demo)", color = if (isDarkMode) darkText else Color.DarkGray, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: GiggzViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Worker") } // "Worker", "Employer"
    var isLoading by remember { mutableStateOf(false) }

    // Custom Palette for Dark Mode
    val darkPrimaryBg = Color(0xFF121417) // very dark charcoal
    val darkPrimaryAccent = Color(0xFF10B981) // Mint Green
    val darkSecondaryAccent = Color(0xFFFBBF24) // Gold
    val darkText = Color.White
    val darkSecondaryText = Color(0xFF9CA3AF) // Light Gray

    BackHandler {
        onNavigate("login")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(if (isDarkMode) darkPrimaryBg else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E2228) else MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.5.dp, if (isDarkMode) Color(0xFF2E333D) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                 Text(
                    text = "Join Giggz smart job marketplace",
                    fontSize = 12.sp,
                    color = if (isDarkMode) darkSecondaryText else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isDarkMode) darkText else Color.Black,
                    unfocusedTextColor = if (isDarkMode) darkText else Color.Black,
                    focusedLabelColor = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                    unfocusedLabelColor = if (isDarkMode) darkSecondaryText else Color.Gray,
                    focusedBorderColor = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                    unfocusedBorderColor = if (isDarkMode) Color(0xFF2E333D) else Color(0xFFE5E7EB),
                    focusedContainerColor = if (isDarkMode) darkPrimaryBg else Color.White,
                    unfocusedContainerColor = if (isDarkMode) darkPrimaryBg else Color.White
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name / Business Name") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = if (isDarkMode) darkPrimaryAccent else GiggzGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = if (isDarkMode) darkPrimaryAccent else GiggzGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Choose Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = if (isDarkMode) darkPrimaryAccent else GiggzGreen) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = if (isDarkMode) darkPrimaryAccent else GiggzGreen)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "I want to sign up as a:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) darkText else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rolesList = listOf("Worker", "Employer")
                    rolesList.forEach { role ->
                        val isSelected = selectedRole == role
                        val cardColor = if (isSelected) (if (isDarkMode) darkPrimaryAccent.copy(alpha = 0.15f) else GiggzGreen.copy(alpha = 0.12f)) else Color.Transparent
                        val borderCol = if (isSelected) (if (isDarkMode) darkPrimaryAccent else GiggzGreen) else (if (isDarkMode) Color(0xFF2E333D) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedRole = role },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when (role) {
                                        "Worker" -> Icons.Filled.Engineering
                                        "Employer" -> Icons.Filled.Business
                                        else -> Icons.Filled.Badge
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) (if (isDarkMode) darkPrimaryAccent else GiggzGreen) else (if (isDarkMode) darkSecondaryText else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = role,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) (if (isDarkMode) darkPrimaryAccent else GiggzGreen) else (if (isDarkMode) darkSecondaryText else MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                            context.showSafeToast("Please fill in all details.")
                            return@Button
                        }
                        isLoading = true
                        viewModel.register(email, password, fullName, selectedRole) { success, message ->
                            isLoading = false
                            context.showSafeToast(message)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) darkPrimaryAccent else GiggzGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Register Now", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Already have an account? ", fontSize = 12.sp, color = if (isDarkMode) darkSecondaryText else MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "Login",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) darkPrimaryAccent else GiggzGreen,
                        modifier = Modifier.clickable { onNavigate("login") }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkerProfileSetupScreen(
    viewModel: GiggzViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var nationality by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var experienceString by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePhoto by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        viewModel.logout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Complete Worker Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GiggzGreen,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Employers find workers based on these skills and experiences",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        ImageAttachmentPicker(
            imageUrl = profilePhoto,
            onImageSelected = { profilePhoto = it },
            label = "Profile Photo (from Device or TaskRabbit Preset)",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            placeholder = { Text("+254 712 345678") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location (City / Area)") },
            placeholder = { Text("Nairobi CBD, Kenya") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nationality,
            onValueChange = { nationality = it },
            label = { Text("Nationality") },
            placeholder = { Text("Kenyan") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        var expandedSkills by remember { mutableStateOf(false) }
        val categoryOptions = listOf(
            "All",
            "Carpentry",
            "Plumbing",
            "Electrical Wiring",
            "Electronics Repair",
            "Painting & Decoration",
            "General Cleaning",
            "Gardening & Landscaping",
            "Catering & Cooking",
            "Photography & Video",
            "Mobile App Development",
            "Web & Graphic Design",
            "Furniture Making",
            "Welding & Metalwork",
            "Automotive Mechanics",
            "Tailoring & Fashion Design",
            "Academic Tutoring",
            "Health & Wellness",
            "Real Estate Services",
            "Other Casual Gigs"
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                label = { Text("Your Skills / Categories (click to select)") },
                placeholder = { Text("Select your trade categories") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select Skills",
                        tint = GiggzGreen
                    )
                }
            )
            // Invisible clickable overlay to capture clicks reliably across the entire field
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expandedSkills = true }
            )

            DropdownMenu(
                expanded = expandedSkills,
                onDismissRequest = { expandedSkills = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                DropdownMenuItem(
                    text = { Text("❌ Clear Selection", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    onClick = {
                        skills = ""
                        expandedSkills = false
                    }
                )
                categoryOptions.forEach { category ->
                    val isSelected = skills.split(",").map { it.trim().lowercase() }.contains(category.lowercase())
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = GiggzGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category, fontSize = 14.sp)
                            }
                        },
                        onClick = {
                            val currentList = skills.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .toMutableList()
                            
                            if (category.equals("All", ignoreCase = true)) {
                                if (isSelected) {
                                    currentList.removeAll { it.equals("All", ignoreCase = true) }
                                } else {
                                    currentList.clear()
                                    currentList.add("All")
                                }
                            } else {
                                currentList.removeAll { it.equals("All", ignoreCase = true) }
                                if (isSelected) {
                                    currentList.removeAll { it.equals(category, ignoreCase = true) }
                                } else {
                                    currentList.add(category)
                                }
                            }
                            skills = currentList.joinToString(", ")
                        }
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.3f))
                
                DropdownMenuItem(
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(GiggzGreen, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    },
                    onClick = { expandedSkills = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = experienceString,
            onValueChange = { experienceString = it },
            label = { Text("Years of Experience") },
            placeholder = { Text("5") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(200)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Short Professional Biography") },
            placeholder = { Text("Describe your expertise, typical jobs you cover, and work availability.") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(200)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (phone.isBlank() || location.isBlank() || skills.isBlank()) {
                    context.showSafeToast("Please complete vital phone, location, and skills fields.")
                    return@Button
                }
                val exp = experienceString.toIntOrNull() ?: 0
                viewModel.completeWorkerProfile(nationality, location, phone, skills, exp, bio, profilePhoto) {
                    context.showSafeToast("Profile activated!")
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Launch My Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

