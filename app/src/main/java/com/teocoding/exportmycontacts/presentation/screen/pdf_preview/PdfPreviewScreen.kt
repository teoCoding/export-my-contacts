package com.teocoding.exportmycontacts.presentation.screen.pdf_preview

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.teocoding.exportmycontacts.R
import kotlinx.coroutines.Dispatchers


private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f

@Composable
fun PdfPreviewScreen(
    onEvent: (PdfPreviewEvent) -> Unit,
    screenState: PdfPreviewState
) {

    var boxSize by remember {
        mutableStateOf(IntSize.Zero)
    }


    Box(
        Modifier
            .onGloballyPositioned {
                boxSize = it.size
            }
            .background(Color(0xFFF1F1F1))
            .fillMaxSize()
    ) {

        if (screenState.bitmapFiles.isNotEmpty()) {

            var offset by remember { mutableStateOf(Offset.Zero) }
            var zoom by remember { mutableFloatStateOf(1f) }

            LazyColumn(
                modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {

                    detectTransformGestures { centroid, pan, gestureZoom, gestureRotate ->

                        offset = offset.calculateNewOffset(
                            centroid, pan, zoom, gestureZoom, size, boxSize
                        )

                        zoom = (zoom * gestureZoom).coerceIn(MIN_ZOOM..MAX_ZOOM)

                    }
                }
                .graphicsLayer {

                    translationX = -offset.x * zoom
                    translationY = -offset.y * zoom
                    scaleX = zoom
                    scaleY = zoom
                    transformOrigin = TransformOrigin(0f, 0f)
                },
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                )
            ) {

                items(screenState.bitmapFiles) { imagePath ->

                    val context = LocalContext.current
                    val imageRequest by remember {

                        val imageRequest = ImageRequest.Builder(context)
                            .data(imagePath)
                            .dispatcher(Dispatchers.IO)
                            .memoryCacheKey(imagePath)
                            .diskCacheKey(imagePath)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build()

                        mutableStateOf(imageRequest)
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                        shape = RectangleShape

                    ) {

                        AsyncImage(
                            model = imageRequest,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                }

            }

            val createFileResult = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/pdf")
            ) { uri ->

                uri?.let {
                    onEvent(
                        PdfPreviewEvent.OnSavePdf(
                            fileUri = it
                        )
                    )
                }

            }

            val myPhoneBookString = stringResource(R.string.my_phone_book)

            ExtendedFloatingActionButton(
                onClick = {
                    createFileResult.launch("$myPhoneBookString.pdf")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_save_24dp),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(text = stringResource(R.string.save))
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }


    }

}


private fun Offset.calculateNewOffset(
    centroid: Offset,
    pan: Offset,
    zoom: Float,
    gestureZoom: Float,
    size: IntSize,
    containerSize: IntSize
): Offset {

    val newScale = (zoom * gestureZoom).coerceIn(MIN_ZOOM..MAX_ZOOM)
    val newOffset = (this + centroid / zoom) -
            (centroid / newScale + pan / zoom)

    val maxYOffset = if (size.height < containerSize.height) {
        ((size.height / zoom) * (zoom - 1f))
    } else {

        size.height + ((containerSize.height / zoom) * (zoom - 1f)) - containerSize.height
    }

    return Offset(
        x = newOffset.x.coerceIn(0f, (size.width / zoom) * (zoom - 1f)),
        y = newOffset.y.coerceIn(0f, maxYOffset)
    )
}