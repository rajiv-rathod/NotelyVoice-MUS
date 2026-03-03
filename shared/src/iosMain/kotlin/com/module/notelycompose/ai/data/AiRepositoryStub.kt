package com.module.notelycompose.ai.data

import com.module.notelycompose.ai.domain.AiRepository
import com.module.notelycompose.ai.domain.model.AiModel
import com.module.notelycompose.ai.domain.model.AiNotes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AiRepositoryStub : AiRepository {
    override suspend fun getApiKey(): String? = null
    override suspend fun setApiKey(key: String) {}
    override suspend fun getOrgId(): String? = null
    override suspend fun setOrgId(orgId: String) {}
    override suspend fun clearKeys() {}
    override fun getAiModel(): Flow<AiModel> = flowOf(AiModel.CHEAP)
    override suspend fun setAiModel(model: AiModel) {}
    override suspend fun transcribeAudio(audioFilePath: String): String =
        error("AI Notes via OpenAI is not available on iOS in this build.")
    override suspend fun generateAiNotes(transcript: String): AiNotes =
        error("AI Notes via OpenAI is not available on iOS in this build.")
    override suspend fun getCachedResult(noteId: Long): Pair<String, AiNotes>? = null
    override suspend fun cacheResult(noteId: Long, transcript: String, notes: AiNotes) {}
}
