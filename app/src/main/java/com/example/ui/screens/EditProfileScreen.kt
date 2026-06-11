package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.ui.theme.*
import com.example.ui.viewmodel.IntelligenceViewModel
import java.io.ByteArrayOutputStream
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: IntelligenceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUploading by viewModel.isProfileUploading.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    
    var fullNameInput by remember(userName) { mutableStateOf(userName) }
    var emailInput by remember(userEmail) { mutableStateOf(userEmail) }
    var changeEmailPassword by remember { mutableStateOf("") }
    
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showTypeDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeletePasswordDialog by remember { mutableStateOf(false) }
    var deleteAccountPasswordInput by remember { mutableStateOf("") }
    var deleteAccountConfirmInput by remember { mutableStateOf("") }

    // Camera capture Uri helper
    val tempCacheFile = remember { File(context.cacheDir, "camera_avatar.jpg") }
    val cameraImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempCacheFile
        )
    }

    // Capture from Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bytes = processAndCompress(context, cameraImageUri)
            if (bytes != null) {
                viewModel.uploadProfilePhoto(bytes) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Error processing camera image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Pick from Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bytes = processAndCompress(context, uri)
            if (bytes != null) {
                viewModel.uploadProfilePhoto(bytes) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Error processing gallery image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepMidnight),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        fontFamily = DMSerifDisplayFontFamily,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 20.sp,
                        color = TextPrimaryColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface1)
            )
        },
        containerColor = DeepMidnight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepMidnight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // SECTION: PROFILE PHOTO AVATAR
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface1),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PROFILE PHOTO",
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = TextMutedColor,
                            fontFamily = DMMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Circular Avatar Frame
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Surface2)
                                .border(1.5.dp, ElectricViolet, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userPhotoUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(context)
                                            .data(userPhotoUrl)
                                            .crossfade(true)
                                            .build()
                                    ),
                                    contentDescription = "User Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initial = if (userName.isNotEmpty()) userName.first().uppercase() else "E"
                                Text(
                                    text = initial,
                                    fontFamily = DMSerifDisplayFontFamily,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontSize = 36.sp,
                                    color = PremiumCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Photo Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showPhotoOptions = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                enabled = !isUploading
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change Photo",
                                        modifier = Modifier.size(15.dp),
                                        tint = Color.White
                                    )
                                    Text("Change URL", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.removeProfilePhoto { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                enabled = !isUploading && userPhotoUrl.isNotEmpty()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Photo",
                                        modifier = Modifier.size(15.dp),
                                        tint = ErrorColor
                                    )
                                    Text("Remove Photo", fontSize = 11.sp, color = ErrorColor)
                                }
                            }
                        }
                    }
                }

                // SECTION 1: PERSONAL INFORMATION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface1),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PERSONAL INFORMATION",
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = TextMutedColor,
                            fontFamily = DMMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )

                        // Change Name Field
                        Text(
                            text = "Full Name",
                            fontSize = 11.sp,
                            fontFamily = InstrumentSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = fullNameInput,
                                onValueChange = { fullNameInput = it },
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontFamily = InstrumentSansFontFamily, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimaryColor,
                                    unfocusedTextColor = TextPrimaryColor,
                                    focusedBorderColor = ElectricViolet,
                                    unfocusedBorderColor = BorderSubtle
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = !isUploading
                            )

                            Button(
                                onClick = {
                                    viewModel.updateProfileName(fullNameInput) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isUploading && fullNameInput.isNotBlank(),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text("Update", fontSize = 11.sp, color = PremiumCyan, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = BorderSubtle, thickness = 0.8.dp)

                        // Change Email Field
                        Text(
                            text = "Email Address",
                            fontSize = 11.sp,
                            fontFamily = InstrumentSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryColor
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontFamily = InstrumentSansFontFamily, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryColor,
                                unfocusedTextColor = TextPrimaryColor,
                                focusedBorderColor = ElectricViolet,
                                unfocusedBorderColor = BorderSubtle
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !isUploading
                        )

                        Text(
                            text = "To secure email update, verify your current account password.",
                            fontSize = 10.sp,
                            color = TextMutedColor,
                            fontFamily = DMMonoFontFamily
                        )

                        OutlinedTextField(
                            value = changeEmailPassword,
                            onValueChange = { changeEmailPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Verify Current Password", fontSize = 11.sp, color = TextMutedColor) },
                            textStyle = LocalTextStyle.current.copy(fontFamily = InstrumentSansFontFamily, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryColor,
                                unfocusedTextColor = TextPrimaryColor,
                                focusedBorderColor = ElectricViolet,
                                unfocusedBorderColor = BorderSubtle
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !isUploading
                        )

                        Button(
                            onClick = {
                                viewModel.updateProfileEmail(emailInput, changeEmailPassword) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        changeEmailPassword = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading && emailInput.isNotBlank() && changeEmailPassword.isNotBlank()
                        ) {
                            Text("Update Email Address", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // SECTION 3: SECURITY
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface1),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "SECURITY",
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = TextMutedColor,
                            fontFamily = DMMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Reset Password",
                            fontSize = 11.sp,
                            fontFamily = InstrumentSansFontFamily,
                            color = TextSecondaryColor
                        )

                        Text(
                            text = "Send a secure password reset link to your registered email address.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = TextMutedColor,
                            fontFamily = InstrumentSansFontFamily
                        )

                        Button(
                            onClick = {
                                viewModel.sendPasswordReset(userEmail) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading
                        ) {
                            Text("Send Reset Link", fontSize = 11.sp, color = PremiumCyan)
                        }
                    }
                }

                // SECTION 4: ACCOUNT MANAGEMENT (DANGER ZONE)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface1),
                    border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "DANGER ZONE",
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = ErrorColor,
                            fontFamily = DMMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Deconstruction & Deletion",
                            fontSize = 11.sp,
                            fontFamily = InstrumentSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryColor
                        )

                        Text(
                            text = "Permanently delete your profile and completely wipe all conversation histories, settings parameters, custom files, and cloud synchronizations.",
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = TextMutedColor,
                            fontFamily = InstrumentSansFontFamily
                        )

                        Button(
                            onClick = { showDeleteAccountDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading
                        ) {
                            Text("Delete Account", fontSize = 11.sp, color = ErrorColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Global screen uploading progress veil
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RichNavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = PremiumCyan)
                            Text(
                                "Synchronizing Reality State...",
                                fontSize = 11.sp,
                                fontFamily = DMMonoFontFamily,
                                color = TextPrimaryColor
                            )
                        }
                    }
                }
            }
        }
    }

    // Photo Sources Bottom Sheet Dialog Box
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            containerColor = RichNavy,
            title = { Text("Choose Avatar Source", color = TextPrimaryColor, fontFamily = InstrumentSansFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showPhotoOptions = false
                            try {
                                cameraLauncher.launch(cameraImageUri)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Camera launch failed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = PremiumCyan)
                            Text("Take Photo via Camera", color = TextPrimaryColor, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            showPhotoOptions = false
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = PremiumCyan)
                            Text("Select of Media Library", color = TextPrimaryColor, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptions = false }) {
                    Text("Cancel", color = TextMutedColor)
                }
            }
        )
    }

    // Danger Zone Dialog 1: Warn Deletion Contents
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = RichNavy,
            title = { Text("Delete Account?", color = ErrorColor, fontFamily = InstrumentSansFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This action permanently removes:",
                        color = TextPrimaryColor,
                        fontSize = 12.sp,
                        fontFamily = InstrumentSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Profile Identity & Metadata\n• Chats & Thread Histories\n• Analysis History & Timeline Predictions\n• Cloud Sync & Encrypted Backups\n• Explorer Preferences\n• Uploaded Documents & Media Files",
                        color = TextSecondaryColor,
                        lineHeight = 16.sp,
                        fontSize = 11.sp,
                        fontFamily = DMMonoFontFamily
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This deconstruction cannot be undone.",
                        color = ErrorColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InstrumentSansFontFamily
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        showDeletePasswordDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Continue", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = PremiumCyan)
                }
            }
        )
    }

    // Danger Zone Dialog 2: Reauthentication current Password
    if (showDeletePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePasswordDialog = false },
            containerColor = RichNavy,
            title = { Text("Reauthenticate Credentials", color = TextPrimaryColor, fontFamily = InstrumentSansFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "For safety, enter your current account password before deleting.",
                        color = TextSecondaryColor,
                        fontSize = 11.sp,
                        fontFamily = InstrumentSansFontFamily
                    )
                    OutlinedTextField(
                        value = deleteAccountPasswordInput,
                        onValueChange = { deleteAccountPasswordInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter Current Password", fontSize = 11.sp, color = TextMutedColor) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryColor,
                            unfocusedTextColor = TextPrimaryColor,
                            focusedBorderColor = ErrorColor,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteAccountPasswordInput.isBlank()) {
                            Toast.makeText(context, "Password is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showDeletePasswordDialog = false
                        showTypeDeleteConfirmDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                    enabled = deleteAccountPasswordInput.isNotBlank()
                ) {
                    Text("Next", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeletePasswordDialog = false
                    deleteAccountPasswordInput = ""
                }) {
                    Text("Cancel", color = PremiumCyan)
                }
            }
        )
    }

    // Danger Zone Dialog 3: Type DELETE to complete
    if (showTypeDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDeleteConfirmDialog = false },
            containerColor = RichNavy,
            title = { Text("Final Confirmation", color = ErrorColor, fontFamily = InstrumentSansFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "To authorize immediate, full deconstruction, type the word DELETE below.",
                        color = TextSecondaryColor,
                        fontSize = 11.sp,
                        fontFamily = InstrumentSansFontFamily
                    )
                    OutlinedTextField(
                        value = deleteAccountConfirmInput,
                        onValueChange = { deleteAccountConfirmInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Type DELETE to confirm", fontSize = 11.sp, color = TextMutedColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryColor,
                            unfocusedTextColor = TextPrimaryColor,
                            focusedBorderColor = ErrorColor,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteAccountConfirmInput == "DELETE") {
                            showTypeDeleteConfirmDialog = false
                            viewModel.deleteUserAccount(deleteAccountPasswordInput) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) {
                                    deleteAccountConfirmInput = ""
                                    deleteAccountPasswordInput = ""
                                    onNavigateBack() // Automatic logout takes us to LoginScreen
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                    enabled = deleteAccountConfirmInput == "DELETE"
                ) {
                    Text("DESTRUCT EVERYWHERE", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTypeDeleteConfirmDialog = false
                    deleteAccountConfirmInput = ""
                    deleteAccountPasswordInput = ""
                }) {
                    Text("Cancel", color = PremiumCyan)
                }
            }
        )
    }
}

// Crop & Compress Helper function
private fun processAndCompress(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        inputStream?.close()

        // Center Crop to square aspect ratio
        val size = minOf(originalBitmap.width, originalBitmap.height)
        val x = (originalBitmap.width - size) / 2
        val y = (originalBitmap.height - size) / 2
        val cropped = Bitmap.createBitmap(originalBitmap, x, y, size, size)

        // Sensible avatar dimension: 400x400
        val finalSize = 400
        val scaled = Bitmap.createScaledBitmap(cropped, finalSize, finalSize, true)

        // Compress to JPEG size-efficient array
        val bos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, bos)
        bos.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
