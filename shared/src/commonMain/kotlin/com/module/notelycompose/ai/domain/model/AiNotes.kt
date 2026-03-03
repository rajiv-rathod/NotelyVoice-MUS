package com.module.notelycompose.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiNotes(
    val title: String = "",
    val tldr: List<String> = emptyList(),
    val outline: List<OutlineSection> = emptyList(),
    @SerialName("key_terms") val keyTerms: List<KeyTerm> = emptyList(),
    @SerialName("action_items") val actionItems: List<ActionItem> = emptyList(),
    @SerialName("study_questions") val studyQuestions: List<StudyQuestion> = emptyList()
)

@Serializable
data class OutlineSection(
    val heading: String,
    val bullets: List<String>
)

@Serializable
data class KeyTerm(
    val term: String,
    val definition: String
)

@Serializable
data class ActionItem(
    val task: String,
    val owner: String? = null,
    val due: String? = null,
    val confidence: Double = 1.0
)

@Serializable
data class StudyQuestion(
    val q: String,
    val a: String
)

enum class AiModel(val apiName: String, val displayName: String) {
    CHEAP("gpt-4o-mini", "Fast (gpt-4o-mini)"),
    BEST("gpt-4o", "Best (gpt-4o)")
}

sealed class AiNotesState {
    object Idle : AiNotesState()
    object Transcribing : AiNotesState()
    object GeneratingNotes : AiNotesState()
    data class Ready(val notes: AiNotes, val transcript: String) : AiNotesState()
    data class Error(val message: String, val isApiKeyMissing: Boolean = false) : AiNotesState()
}
