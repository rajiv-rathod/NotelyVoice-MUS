package com.module.notelycompose.ai.domain

import com.module.notelycompose.ai.domain.model.AiModel
import com.module.notelycompose.ai.domain.model.AiNotes
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun getApiKey(): String?
    suspend fun setApiKey(key: String)
    suspend fun getOrgId(): String?
    suspend fun setOrgId(orgId: String)
    suspend fun clearKeys()
    fun getAiModel(): Flow<AiModel>
    suspend fun setAiModel(model: AiModel)
    suspend fun transcribeAudio(audioFilePath: String): String
    suspend fun generateAiNotes(transcript: String): AiNotes
    suspend fun getCachedResult(noteId: Long): Pair<String, AiNotes>?
    suspend fun cacheResult(noteId: Long, transcript: String, notes: AiNotes)
}
