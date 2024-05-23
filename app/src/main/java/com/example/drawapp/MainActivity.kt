package com.example.drawapp

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.drawapp.db.AppDatabase
import com.example.drawapp.db.ImageDao
import com.example.drawapp.db.ImageEntity
import com.example.drawapp.ui.theme.DrawAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "image-database"
        ).build()

        setContent {
            val pathData = remember {
                mutableStateOf(PathData())
            }
            val pathList = remember {
                mutableStateListOf(PathData())
            }

            val imageDao = db.imageDao()
            // val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            // val imageBitmap = bitmap.asImageBitmap()

            val dialogState = remember {
                mutableStateOf(false)
            }
            if (dialogState.value) {
                DialogSearch(dialogState, onSubmit = {

                }, this)
            }


            DrawAppTheme {
                Column {
                    DrawCanvas(pathData, pathList)
                    BottomPanel(
                        { color ->
                            pathData.value = pathData.value.copy(
                                color = color
                            )
                        },
                        { lineWidth ->
                            pathData.value = pathData.value.copy(
                                lineWidth = lineWidth
                            )
                        },
                        {
                            pathList.removeIf { pathD ->
                                pathList[pathList.size - 1] == pathD
                            }
                        },
                        { cap ->
                            pathData.value = pathData.value.copy(
                                cap = cap
                            )
                        }
                    ) {
                        dialogState.value = true
                    }
                }
            }
        }
    }


    fun saveImage(
        imageData: ImageBitmap,
        fileName: String,
        imageDao: ImageDao
    ) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageData.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val imageEntity = ImageEntity(
            fileName = fileName,
            imageData = imageBytes
        )


        CoroutineScope(Dispatchers.Main).launch {
            imageDao.insertImage(imageEntity)
        }
    }
}

@Composable
fun DrawCanvas(pathData: MutableState<PathData>, pathList: SnapshotStateList<PathData>) {
    var tempPath = Path()
    var imageData = remember { mutableStateOf<ImageBitmap?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.70f)
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = {
                        tempPath = Path()
                    },
                    onDragEnd = {
                        pathList.add(
                            pathData.value.copy(
                                path = tempPath
                            )
                        )
                    }
                ) { change, dragAmount ->
                    tempPath.moveTo(
                        change.position.x - dragAmount.x,
                        change.position.y - dragAmount.y
                    )
                    tempPath.lineTo(
                        change.position.x,
                        change.position.y
                    )

                    if (pathList.size > 0) {
                        pathList.removeAt(pathList.size - 1)
                    }
                    pathList.add(
                        pathData.value.copy(
                            path = tempPath
                        )
                    )
                }
            }
    ) {
        pathList.forEach { pathData ->
            drawPath(
                pathData.path,
                color = pathData.color,
                style = Stroke(
                    pathData.lineWidth,
                    cap = pathData.cap
                )
            )
        }
        Log.d("MyLog", "Size: ${pathList.size}")
    }
//    Canvas(
//        modifier = Modifier.size(0.dp)
//    ) {
//        drawRect(
//            color = Color.Transparent,
//            size = size
//        )
//        val imageBitmap = drawContext.canvas.toPixelMap().toComposeImageBitmap()
//        imageData = imageBitmap
//    }
}

@Composable
fun DialogSearch(dialogState: MutableState<Boolean>, onSubmit: (String) -> Unit, context: Context) {
    val dialogText = remember {
        mutableStateOf("")
    }
    AlertDialog(
        onDismissRequest = {
            dialogState.value = false
        },
        confirmButton = {
            TextButton(onClick = {
                onSubmit(dialogText.value)
                dialogState.value = false
                Toast.makeText(context, "Картина сохранена!", Toast.LENGTH_SHORT).show()
            }) {
                Text(text = "Ok")
            }
        },

        dismissButton = {
            TextButton(onClick = {
                dialogState.value = false
            }) {
                Text(text = "Cancel")
            }
        },

        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Введите название картинки")
                TextField(
                    value = dialogText.value,
                    onValueChange = {
                        dialogText.value = it
                    },
                    singleLine = true
                )
            }
        }
    )
}