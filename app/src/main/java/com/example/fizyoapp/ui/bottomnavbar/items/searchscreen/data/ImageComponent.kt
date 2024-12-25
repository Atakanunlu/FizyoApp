package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.CircleCropTransformation
import androidx.compose.ui.platform.LocalContext

@Composable
fun CircleImage(imageUrl:String){
    val painter = rememberAsyncImagePainter(
        model=ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .size(Size.ORIGINAL)
            .transformations(CircleCropTransformation())
            .crossfade(true)
            .build()
    )
    Image(painter=painter,contentDescription="circle ımage")
}