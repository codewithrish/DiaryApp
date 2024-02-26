package com.codewithrish.mydiaryapp.presentation.screens.write

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithrish.mydiaryapp.data.repository.MongoDB
import com.codewithrish.mydiaryapp.model.Diary
import com.codewithrish.mydiaryapp.model.Mood
import com.codewithrish.mydiaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.codewithrish.mydiaryapp.util.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    var uiState by mutableStateOf(UiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument() {
        uiState = uiState.copy(
            selectedDairyId = savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun fetchSelectedDiary() {
        uiState.selectedDairyId?.let { selectedDairyId ->
            viewModelScope.launch(Dispatchers.IO) {
                val diary = MongoDB.getSelectedDiary(
                    diaryId = ObjectId.invoke(selectedDairyId)
                )
                if (diary is RequestState.Success) {
                    withContext(Dispatchers.Main) {
                        setSelectedDiary(diary = diary.data)
                        setTitle(title = diary.data.title)
                        setDescription(description = diary.data.description)
                        setMood(mood = Mood.valueOf(diary.data.mood))
                    }
                }
            }
        }
    }

    private fun setSelectedDiary(diary: Diary) { uiState = uiState.copy(selectedDairy = diary) }
    fun setTitle(title: String) { uiState = uiState.copy(title = title) }
    fun setDescription(description: String) { uiState = uiState.copy(description = description) }
    private fun setMood(mood: Mood) { uiState = uiState.copy(mood = mood) }
    fun insertDiary(
        diary: Diary,
        onSuccess:() -> Unit,
        onError:(String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = MongoDB.addNewDiary(diary = diary)
            if (result is RequestState.Success) {
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else if (result is RequestState.Error) {
                withContext(Dispatchers.Main) {
                    onError(result.error.message.toString())
                }
            }
        }
    }
}

data class UiState(
    val selectedDairyId: String? = null,
    val selectedDairy: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
)