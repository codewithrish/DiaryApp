package com.codewithrish.mydiaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.codewithrish.mydiaryapp.model.Diary
import com.google.accompanist.pager.PagerState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    selectedDiary: Diary?,
    pagerState: PagerState,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            WriteTopBar(
                selectedDiary = selectedDiary,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed,
            )
        },
        content = {
            WriteContent(
                paddingValues = it,
                pagerState = pagerState,
                title = "",
                onTitleChanged = {},
                description = "",
                onDescriptionChanged = {}
            )
        }
    )
}