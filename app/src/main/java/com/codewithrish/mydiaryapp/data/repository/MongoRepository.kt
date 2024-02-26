package com.codewithrish.mydiaryapp.data.repository

import com.codewithrish.mydiaryapp.model.Diary
import com.codewithrish.mydiaryapp.util.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

interface MongoRepository {
    fun configureTheRealm()
    fun getAllDiaries(): Flow<Diaries>
}