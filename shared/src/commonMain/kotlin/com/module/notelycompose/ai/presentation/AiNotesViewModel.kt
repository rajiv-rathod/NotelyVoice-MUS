package com.module.notelycompose.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.ai.domain.AiRepository
import com.module.notelycompose.ai.domain.model.AiModel
import com.module.notelycompose.ai.domain.model.AiNotes
import com.module.notelycompose.ai.domain.model.AiNotesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiNotesViewModel(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AiNotesState>(AiNotesState.Idle)
    val state: StateFlow<AiNotesState> = _state

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey

    private val _orgId = MutableStateFlow("")
    val orgId: StateFlow<String> = _orgId

    private val _aiModel = MutableStateFlow(AiModel.CHEAP)
    val aiModel: StateFlow<AiModel> = _aiModel

    init {
        viewModelScope.launch {
            _apiKey.value = aiRepository.getApiKey().orEmpty()
            _orgId.value = aiRepository.getOrgId().orEmpty()
            aiRepository.getAiModel().collect { _aiModel.value = it }
        }
    }

    fun loadCachedNotes(noteId: Long) {
        viewModelScope.launch {
            val cached = aiRepository.getCachedResult(noteId)
            if (cached != null) {
                _state.update { AiNotesState.Ready(cached.second, cached.first) }
            }
        }
    }

    fun generateNotes(noteId: Long, audioFilePath: String) {
        viewModelScope.launch {
            val key = aiRepository.getApiKey()
            if (key.isNullOrBlank()) {
                _state.update {
                    AiNotesState.Error(
                        "OpenAI API key is not set. Please add your key in Settings → AI Settings.",
                        isApiKeyMissing = true
                    )
                }
                return@launch
            }
            if (audioFilePath.isBlank()) {
                _state.update { AiNotesState.Error("No audio recording found for this note.") }
                return@launch
            }
            try {
                _state.update { AiNotesState.Transcribing }
                val transcript = aiRepository.transcribeAudio(audioFilePath)

                _state.update { AiNotesState.GeneratingNotes }
                val notes = aiRepository.generateAiNotes(transcript)

                aiRepository.cacheResult(noteId, transcript, notes)
                _state.update { AiNotesState.Ready(notes, transcript) }
            } catch (e: Exception) {
                val msg = e.message ?: "Unexpected error"
                _state.update { AiNotesState.Error(msg) }
            }
        }
    }

    fun retry(noteId: Long, audioFilePath: String) {
        _state.update { AiNotesState.Idle }
        generateNotes(noteId, audioFilePath)
    }

    fun resetState() {
        _state.update { AiNotesState.Idle }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            aiRepository.setApiKey(key.trim())
            _apiKey.value = key.trim()
        }
    }

    fun saveOrgId(orgId: String) {
        viewModelScope.launch {
            aiRepository.setOrgId(orgId.trim())
            _orgId.value = orgId.trim()
        }
    }

    fun clearKeys() {
        viewModelScope.launch {
            aiRepository.clearKeys()
            _apiKey.value = ""
            _orgId.value = ""
        }
    }

    fun setAiModel(model: AiModel) {
        viewModelScope.launch {
            aiRepository.setAiModel(model)
            _aiModel.value = model
        }
    }
}
