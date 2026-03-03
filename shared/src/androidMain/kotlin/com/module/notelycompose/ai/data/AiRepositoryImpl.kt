package com.module.notelycompose.ai.data

import com.module.notelycompose.ai.domain.AiRepository
import com.module.notelycompose.ai.domain.model.AiModel
import com.module.notelycompose.ai.domain.model.AiNotes
import com.module.notelycompose.onboarding.data.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

class AiRepositoryImpl(
    private val encryptedKeyStore: EncryptedKeyStore,
    private val openAiClient: OpenAiHttpClient,
    private val preferencesRepository: PreferencesRepository
) : AiRepository {

    override suspend fun getApiKey(): String? = withContext(Dispatchers.IO) {
        encryptedKeyStore.getApiKey()
    }

    override suspend fun setApiKey(key: String) = withContext(Dispatchers.IO) {
        encryptedKeyStore.setApiKey(key)
    }

    override suspend fun getOrgId(): String? = withContext(Dispatchers.IO) {
        encryptedKeyStore.getOrgId()
    }

    override suspend fun setOrgId(orgId: String) = withContext(Dispatchers.IO) {
        encryptedKeyStore.setOrgId(orgId)
    }

    override suspend fun clearKeys() = withContext(Dispatchers.IO) {
        encryptedKeyStore.clearAll()
    }

    override fun getAiModel(): Flow<AiModel> =
        preferencesRepository.getAiModel().map { name ->
            AiModel.entries.firstOrNull { it.name == name } ?: AiModel.CHEAP
        }

    override suspend fun setAiModel(model: AiModel) {
        preferencesRepository.setAiModel(model.name)
    }

    override suspend fun transcribeAudio(audioFilePath: String): String = withContext(Dispatchers.IO) {
        openAiClient.transcribeAudio(audioFilePath)
    }

    override suspend fun generateAiNotes(transcript: String): AiNotes = withContext(Dispatchers.IO) {
        val modelName = preferencesRepository.getAiModelSync()
        val model = AiModel.entries.firstOrNull { it.name == modelName } ?: AiModel.CHEAP
        val notesJson = openAiClient.generateNotes(transcript, model.apiName)
        json.decodeFromString<AiNotes>(notesJson)
    }

    override suspend fun getCachedResult(noteId: Long): Pair<String, AiNotes>? {
        return preferencesRepository.getAiNotesCache(noteId)?.let { (transcript, notesJson) ->
            try {
                transcript to json.decodeFromString<AiNotes>(notesJson)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun cacheResult(noteId: Long, transcript: String, notes: AiNotes) {
        val notesJson = json.encodeToString(AiNotes.serializer(), notes)
        preferencesRepository.setAiNotesCache(noteId, transcript, notesJson)
    }
}
