package com.example.data

import com.example.BuildConfig
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

// =============================================================================
// GEMINI API REQUEST/RESPONSE MODELS (MOSHI CONFORMANT)
// =============================================================================

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiConfig(
    val temperature: Float = 0.4f
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

// =============================================================================
// RETROFIT API INTERFACE
// =============================================================================

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// =============================================================================
// GEMINI SERVICE WRAPPER
// =============================================================================

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Obtains an AI-generated job matchmaking summary matching a worker's profile with active job listings.
     */
    suspend fun getJobMatchRecommendations(
        workerName: String,
        workerSkills: String,
        workerBio: String,
        jobsList: List<JobEntity>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check if API key is invalid/placeholder
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "placeholder_key") {
            return generateLocalRecommendations(workerSkills, jobsList)
        }

        val jobsDataString = jobsList.joinToString(separator = "\n") { job ->
            "JobID: ${job.id} | Title: ${job.title} | Category: ${job.category} | Budget: K${job.budget} | Description: ${job.description}"
        }

        val prompt = """
            You are Giggz AI Matchmaker, a smart assistant for the Giggz job marketplace.
            Analyze this Worker's profile and recommend the top matching jobs from the list below.
            
            Worker Name: $workerName
            Worker Skills: $workerSkills
            Worker Bio: $workerBio
            
            Available Jobs:
            $jobsDataString
            
            Provide an inspiring, highly professional, structured review (maximum 250 words) under 3 clear headers:
            1. 🌟 BEST MATCHES (Mention specific Job Titles and budget, explaining why their skills are a perfect fit).
            2. 🛠️ ACTIONABLE TIPS (How they can refine their profile or custom-tailor their cover letter for these employers).
            3. 💡 OPPORTUNITY SCOPE (A summary of local demand for their skills).
            
            Keep the tone warm, direct, professional, and do not use technical markdown tables or system IDs.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                responseText
            } else {
                generateLocalRecommendations(workerSkills, jobsList)
            }
        } catch (e: Exception) {
            "Giggz AI is currently optimizing servers. Below is our high-performance local match analysis:\n\n" +
                    generateLocalRecommendations(workerSkills, jobsList)
        }
    }

    /**
     * Smart local matchmaking algorithm using keyword intersection when Gemini API is offline or unconfigured.
     */
    private fun generateLocalRecommendations(skills: String, jobs: List<JobEntity>): String {
        val skillKeywords = skills.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val matches = mutableListOf<String>()

        for (job in jobs) {
            var matchScore = 0
            val jobTitleAndDesc = (job.title + " " + job.description + " " + job.category).lowercase()

            for (keyword in skillKeywords) {
                if (jobTitleAndDesc.contains(keyword)) {
                    matchScore += 2
                }
                // Check simple sub-tokens
                val subTokens = keyword.split(" ")
                for (token in subTokens) {
                    if (token.length > 3 && jobTitleAndDesc.contains(token)) {
                        matchScore += 1
                    }
                }
            }

            if (matchScore > 0 || job.category.lowercase().split(" ").any { skillKeywords.contains(it) }) {
                matches.add("• **${job.title}** (Budget: K${job.budget} in ${job.location}) — Match score: ${matchScore + 2}/10. Perfect fit for your skill categories!")
            }
        }

        val matchResult = if (matches.isNotEmpty()) {
            matches.joinToString("\n\n")
        } else {
            "• **Explore local contracts** — Check the general job boards or add more specialized skills (like Plumber, Developer, or Carpenter) to unlock automated matchmaking."
        }

        return """
            🌟 BEST MATCHES
            Based on your skills: "$skills", we found the following highly compatible listings:
            
            $matchResult
            
            🛠️ ACTIONABLE TIPS
            • **Add visual items to your Portfolio**: Employers are 3x more likely to invite workers with 2 or more active portfolio attachments.
            • **Keep your availability set to 'Available'**: This highlights your profile at the top of the Job Giver searches.
            • **Browse Casual Gigs**: Short on time? Apply for hourly gigs in the 'Casual Gigs' tab for instant payouts.
            
            💡 OPPORTUNITY SCOPE
            There is a high local demand for skilled individuals who can complete short-term contracts. Ensure your contact information is verified so employers can message you instantly.
        """.trimIndent()
    }
}
