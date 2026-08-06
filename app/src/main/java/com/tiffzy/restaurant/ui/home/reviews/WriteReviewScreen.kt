package com.tiffzy.restaurant.ui.home.reviews

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tiffzy.restaurant.core.base.UiState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    slug: String,
    viewModel: ReviewViewModel,
    onBack: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var selectedItemId by remember { mutableStateOf<Int?>(null) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current
    val actionState by viewModel.actionState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = (selectedImages + uris).take(5)
    }

    LaunchedEffect(actionState) {
        if (actionState is UiState.Success) {
            viewModel.resetActionState()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Write a Review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("How was your experience?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            // Rating Stars
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { index ->
                    val starIndex = index + 1
                    Icon(
                        imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (starIndex <= rating) Color(0xFFFFB300) else Color.LightGray,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { rating = starIndex }
                    )
                }
            }
            
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Share your thoughts...") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                placeholder = { Text("What did you like or dislike about the food and service?") }
            )
            
            // Image Upload Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Add Photos (Optional)", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { launcher.launch("image/*") },
                        color = Color(0xFFEEEEEE)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(24.dp), tint = Color.Gray)
                    }
                    
                    selectedImages.forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val multipartImages = selectedImages.map { uri ->
                        val file = File(context.cacheDir, "review_img_${System.currentTimeMillis()}.jpg")
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val outputStream = FileOutputStream(file)
                        inputStream?.copyTo(outputStream)
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("images", file.name, requestFile)
                    }
                    // Pass selectedItemId if available
                    viewModel.addReview(slug, rating.toDouble(), comment, multipartImages)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = rating > 0 && actionState !is UiState.Loading
            ) {
                if (actionState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("SUBMIT REVIEW")
                }
            }
        }
    }
}
