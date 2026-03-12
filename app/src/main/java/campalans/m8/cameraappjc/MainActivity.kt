@file:OptIn(ExperimentalPermissionsApi::class)

package campalans.m8.cameraappjc

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.MoreExecutors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Demanem permisos (Android 13+)
                    val permissionsState = rememberPermissionState(android.Manifest.permission.CAMERA)

                    // Si tots els permisos estan acceptats entrem a la app
                    if (permissionsState.status.isGranted) {
                        CameraScreen()
                    } else {
                        // Si falta algun permis el demanem
                        PantallaPermisos(click = {
                            permissionsState.launchPermissionRequest()
                        })
                    }
                }
            }
        }
    }
}
@Composable
fun PantallaPermisos(click: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = click) { Text("Donar permís o no entras crack") }
    }
}
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher per capturar la foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            capturedImageUri = imageUri
        }
    }

    // Funció per crear el fitxer i obtenir l'URI
    fun createImageFile(): Uri {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDateAndTime = sdf.format(Date())

        val imageFile = File(
            context.getExternalFilesDir(null),
            "Pol_Busquets ${currentDateAndTime}.mp4"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Càmera amb Intent",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = {
                imageUri = createImageFile()
                cameraLauncher.launch(imageUri!!)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obrir Càmera")
        }

        // Mostrar la imatge capturada
        capturedImageUri?.let { uri ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                VideoPlayer(uri)
            }

        } ?: run {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("La imatge apareixerà aquí")
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(uri: Uri) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    AndroidView(
        factory = { PlayerView(it).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}