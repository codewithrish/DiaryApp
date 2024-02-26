package com.codewithrish.mydiaryapp.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithrish.mydiaryapp.data.repository.Diaries
import com.codewithrish.mydiaryapp.data.repository.MongoDB
import com.codewithrish.mydiaryapp.util.RequestState
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
    val diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observeAllDiaries()
    }

    private fun observeAllDiaries() {
        viewModelScope.launch {
            MongoDB.getAllDiaries().collect { result ->
                diaries.value = result
            }
        }
    }
}