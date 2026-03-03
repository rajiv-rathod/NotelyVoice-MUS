package com.module.notelycompose.ai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api.openai.com/v1"
private const val MAX_RETRIES = 2
private const val TIMEOUT_SECONDS = 120L

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("response_format") val responseFormat: ResponseFormat = ResponseFormat()
)

@Serializable
private data class ChatMessage(val role: String, val content: String)

@Serializable
private data class ResponseFormat(val type: String = "json_object")

@Serializable
private data class ChatResponse(val choices: List<Choice>)

@Serializable
private data class Choice(val message: ChatMessage)

@Serializable
private data class TranscriptionResponse(val text: String)

class OpenAiHttpClient(
    private val keyStore: EncryptedKeyStore
) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    fun transcribeAudio(audioFilePath: String, model: String = "whisper-1"): String {
        val apiKey = keyStore.getApiKey()
            ?: throw IllegalStateException("OpenAI API key is not set")
        val orgId = keyStore.getOrgId()

        val audioFile = File(audioFilePath)
        if (!audioFile.exists()) throw IllegalArgumentException("Audio file not found: $audioFilePath")

        val mediaType = "audio/mpeg".toMediaType()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", audioFile.name, audioFile.asRequestBody(mediaType))
            .addFormDataPart("model", model)
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/audio/transcriptions")
            .addHeader("Authorization", "Bearer $apiKey")
            .apply { orgId?.let { addHeader("OpenAI-Organization", it) } }
            .post(requestBody)
            .build()

        return executeWithRetry(request) { responseBody ->
            json.decodeFromString<TranscriptionResponse>(responseBody).text
        }
    }

    fun generateNotes(transcript: String, chatModel: String): String {
        val apiKey = keyStore.getApiKey()
            ?: throw IllegalStateException("OpenAI API key is not set")
        val orgId = keyStore.getOrgId()

        val systemPrompt = """
            You are a structured note-taking assistant. Generate concise, student-friendly notes 
            based ONLY on the provided transcript. Output STRICT JSON (no markdown, no code fences).
            
            Required JSON schema:
            {
              "title": "string",
              "tldr": ["string"],
              "outline": [{"heading":"string","bullets":["string"]}],
              "key_terms": [{"term":"string","definition":"string"}],
              "action_items": [{"task":"string","owner":"string|null","due":"string|null","confidence":0.0}],
              "study_questions": [{"q":"string","a":"string"}]
            }
            
            Rules:
            - Ground notes ONLY in transcript content.
            - Reflect uncertainty; reduce confidence if unsure.
            - Keep concise and student-friendly.
            - Output JSON only. No markdown.
        """.trimIndent()

        val chatRequest = ChatRequest(
            model = chatModel,
            messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", "Transcript:\n\n$transcript")
            )
        )

        val body = json.encodeToString(ChatRequest.serializer(), chatRequest)
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .apply { orgId?.let { addHeader("OpenAI-Organization", it) } }
            .post(body)
            .build()

        return executeWithRetry(request) { responseBody ->
            json.decodeFromString<ChatResponse>(responseBody).choices.first().message.content
        }
    }

    private fun <T> executeWithRetry(request: Request, parser: (String) -> T): T {
        var lastException: Exception? = null
        repeat(MAX_RETRIES + 1) { attempt ->
            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string()
                    ?: throw IllegalStateException("Empty response from OpenAI")
                if (!response.isSuccessful) {
                    throw IllegalStateException("OpenAI error ${response.code}: $body")
                }
                return parser(body)
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRIES) {
                    Thread.sleep(1000L * (attempt + 1))
                }
            }
        }
        throw lastException ?: IllegalStateException("Unknown error")
    }
}
