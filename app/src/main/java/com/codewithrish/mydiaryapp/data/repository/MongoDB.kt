package com.codewithrish.mydiaryapp.data.repository

import com.codewithrish.mydiaryapp.model.Diary
import com.codewithrish.mydiaryapp.util.Constants.APP_ID
import com.codewithrish.mydiaryapp.util.RequestState
import com.codewithrish.mydiaryapp.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.ZoneId
import kotlin.Exception

object MongoDB: MongoRepository {
    private val app = App.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }
    override fun configureTheRealm() {
        user?.let {
            val config = SyncConfiguration
                .Builder(user, setOf(Diary::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query<Diary>(query = "ownerId == $0", user.id),
                        name = "User's Diaries"
                    )
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        return user?.let {
            try {
                realm.query<Diary>(query = "ownerId == $0", user.id)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } ?: flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
    }

    override fun getSelectedDiary(diaryId: ObjectId): Flow<RequestState<Diary>> {
        return user?.let {
            try {
                realm.query<Diary>(query = "_id == $0", diaryId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } ?: flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
    }

    override suspend fun insertDiary(diary: Diary): RequestState<Diary> {
        return user?.let {
            realm.write {
                try {
                    val addedDiary = copyToRealm(diary.apply { ownerId = user.id })
                    RequestState.Success(data = addedDiary)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } ?: RequestState.Error(UserNotAuthenticatedException())
    }

    override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
        return user?.let {
            realm.write {
                val queryDiary = query<Diary>(query = "_id == $0", diary._id).first().find()
                queryDiary?.let {
                    queryDiary.title = diary.title
                    queryDiary.description = diary.description
                    queryDiary.mood = diary.mood
                    queryDiary.images = diary.images
                    queryDiary.date = diary.date
                    RequestState.Success(data = queryDiary)
                } ?: RequestState.Error(Exception("Queried Diary does not exist."))
            }
        } ?: RequestState.Error(UserNotAuthenticatedException())
    }
}

private class UserNotAuthenticatedException: Exception("User is not logged in.")