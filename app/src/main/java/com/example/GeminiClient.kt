package com.example

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>)

// --- AI Chat Command Output ---

@JsonClass(generateAdapter = true)
data class AiNoteReq(
    val title: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class AiTaskReq(
    val title: String,
    val description: String = "",
    val category: String = "OTHER", // WORK, PERSONAL, HEALTH, STUDY, OTHER
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val folderName: String = "Inbox",
    val dueDate: String = "",
    val subtasks: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AiEventReq(
    val title: String,
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val tags: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeminiAiCommandResponse(
    val chatResponse: String,
    val folders: List<String>? = emptyList(),
    val notes: List<AiNoteReq>? = emptyList(),
    val tasks: List<AiTaskReq>? = emptyList(),
    val events: List<AiEventReq>? = emptyList()
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshiBuild = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshiBuild))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    fun parseAiResponse(rawJson: String): GeminiAiCommandResponse? {
        val cleaned = rawJson
            .trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val adapter = moshiBuild.adapter(GeminiAiCommandResponse::class.java)
            adapter.fromJson(cleaned)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
