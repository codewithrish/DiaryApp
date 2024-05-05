package com.codewithrish.mydiaryapp.presentation.screens.write

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithrish.mydiaryapp.data.repository.MongoDB
import com.codewithrish.mydiaryapp.model.Diary
import com.codewithrish.mydiaryapp.model.GalleryImage
import com.codewithrish.mydiaryapp.model.GalleryState
import com.codewithrish.mydiaryapp.model.Mood
import com.codewithrish.mydiaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.codewithrish.mydiaryapp.model.RequestState
import com.codewithrish.mydiaryapp.util.fetchImagesFromFirebase
import com.codewithrish.mydiaryapp.util.toRealmInstant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    val galleryState = GalleryState()
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
                MongoDB.getSelectedDiary(
                    diaryId = ObjectId.invoke(selectedDairyId)
                ).catch {
                    emit(RequestState.Error(Exception("Diary is already deleted")))
                }.collect { diary ->
                    if (diary is RequestState.Success) {
                        withContext(Dispatchers.Main) {
                            setSelectedDiary(diary = diary.data)
                            setTitle(title = diary.data.title)
                            setDescription(description = diary.data.description)
                            setMood(mood = Mood.valueOf(diary.data.mood))

                            fetchImagesFromFirebase(
                                remoteImagePaths = diary.data.images,
                                onImageDownload = { downloadedImage ->
                                    galleryState.addImage(
                                        GalleryImage(
                                            image = downloadedImage,
                                            remoteImagePath = extractImagePath(
                                                fullImageUrl = downloadedImage.path.toString(),
                                            )
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setSelectedDiary(diary: Diary) { uiState = uiState.copy(selectedDairy = diary) }
    fun setTitle(title: String) { uiState = uiState.copy(title = title) }
    fun setDescription(description: String) { uiState = uiState.copy(description = description) }
    private fun setMood(mood: Mood) { uiState = uiState.copy(mood = mood) }
    fun updateDateTime(zonedDateTime: ZonedDateTime) { uiState = uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant()) }
    fun upsertDiary(
        diary: Diary,
        onSuccess:() -> Unit,
        onError:(String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.selectedDairyId?.let {
                updateDairy(diary, onSuccess, onError)
            } ?: insertDiary(diary, onSuccess, onError)
        }
    }
    private suspend fun insertDiary(
        diary: Diary,
        onSuccess:() -> Unit,
        onError:(String) -> Unit,
    ) {
        val result = MongoDB.insertDiary(diary = diary.apply {
                uiState.updatedDateTime?.let { date = uiState.updatedDateTime!!
            }
        })
        if (result is RequestState.Success) {
            uploadImagesToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    private suspend fun updateDairy(
        diary: Diary,
        onSuccess:() -> Unit,
        onError:(String) -> Unit,
    ) {
        val result = MongoDB.updateDiary(diary = diary.apply {
            _id = ObjectId.invoke(uiState.selectedDairyId!!)
            date =  uiState.updatedDateTime?.let { uiState.updatedDateTime!! } ?: uiState.selectedDairy!!.date
        })
        if (result is RequestState.Success) {
            uploadImagesToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    fun deleteDiary(
        onSuccess:() -> Unit,
        onError:(String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.selectedDairyId?.let {
                val result = MongoDB.deleteDiary(id = ObjectId.invoke(uiState.selectedDairyId!!))
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

    fun addImage(image: Uri, imageType: String) {
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath
            )
        )
    }

    private fun uploadImagesToFirebase() {
        val storageRef = FirebaseStorage.getInstance().reference
        galleryState.images.forEach {
            val imageRef = storageRef.child(it.remoteImagePath)
            imageRef.putFile(it.image)
        }
    }

    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks.last()
        return "images/${FirebaseAuth.getInstance().currentUser?.uid}/$imageName"
    }
}

data class UiState(
    val selectedDairyId: String? = null,
    val selectedDairy: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null
)