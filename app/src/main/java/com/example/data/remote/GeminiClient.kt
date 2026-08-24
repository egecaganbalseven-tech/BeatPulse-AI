package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.DailyExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class GeminiCoachResponse(
    val answer: String,
    val thinkingProcess: String? = null,
    val suggestedBpm: Int? = null,
    val suggestedPattern: String? = null
)

class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun askCoach(
        prompt: String,
        instrument: String,
        level: String,
        useHighThinking: Boolean = false,
        bitmapImage: Bitmap? = null
    ): GeminiCoachResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiCoachResponse(
                answer = "Merhaba! Ben BeatPulse AI Ritim ve Müzik Koçunuzum. $instrument ($level seviyesi) için ritim, metronom çalışmaları, aksak tartımlar veya hızlanma teknikleri hakkında bana istediğinizi sorabilirsiniz.\n\nİpucu: Ayarlar panelinden Gemini API anahtarınızı tanımlayarak gerçek zamanlı derinlemesine AI koçluğundan yararlanabilirsiniz.",
                thinkingProcess = if (useHighThinking) "Model: gemini-3.1-pro-preview\nThinking Level: HIGH\nEnstrüman: $instrument, Seviye: $level analiz edildi." else null,
                suggestedBpm = 120,
                suggestedPattern = "Metronom eşliğinde 8'lik ve 16'lık vuruş geçişleri"
            )
        }

        val modelName = if (useHighThinking || bitmapImage != null) {
            "gemini-3.1-pro-preview"
        } else {
            "gemini-3.5-flash"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val systemInstruction = """
            Sen 'BeatPulse AI' adında profesyonel, motive edici ve uzman bir Müzik & Ritim Pratik Koçusun.
            Kullanıcının seçtiği enstrüman: '$instrument', seviyesi: '$level'.
            Ritim doğruluğu (timing), poliritimler, aksak tartımlar, hız artırma (speed ladder), el-ayak koordinasyonu ve groove dinamikleri konularında pratik, uygulanabilir, profesyonel tavsiyeler ver.
            Yanıtların Türkçe, samimi ve stüdyo disiplinine uygun olsun.
            Gerektiğinde ritim sayımı (ör. 1 e & a 2 e & a veya 1-ki-2-ki) formatında örnekler sun.
        """.trimIndent()

        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", prompt))

        if (bitmapImage != null) {
            val base64 = bitmapToBase64(bitmapImage)
            val inlineData = JSONObject()
                .put("mimeType", "image/jpeg")
                .put("data", base64)
            partsArray.put(JSONObject().put("inlineData", inlineData))
        }

        val contentsArray = JSONArray().put(JSONObject().put("parts", partsArray))

        val rootJson = JSONObject()
        rootJson.put("contents", contentsArray)

        val systemContent = JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
        rootJson.put("systemInstruction", systemContent)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)

        if (useHighThinking && modelName == "gemini-3.1-pro-preview") {
            val thinkingConfig = JSONObject().put("thinkingLevel", "HIGH")
            genConfig.put("thinkingConfig", thinkingConfig)
        }
        rootJson.put("generationConfig", genConfig)

        try {
            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext GeminiCoachResponse(
                    answer = "Koç yanıt veremedi: (${response.code}) ${extractErrorMessage(responseBody)}"
                )
            }

            val respObj = JSONObject(responseBody)
            val candidates = respObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val candidateParts = contentObj?.optJSONArray("parts")

            var responseText = ""
            var thinkingText: String? = null

            if (candidateParts != null) {
                for (i in 0 until candidateParts.length()) {
                    val p = candidateParts.getJSONObject(i)
                    if (p.optBoolean("thought", false)) {
                        thinkingText = p.optString("text")
                    } else {
                        responseText += p.optString("text")
                    }
                }
            }

            if (responseText.isBlank() && candidateParts != null && candidateParts.length() > 0) {
                responseText = candidateParts.getJSONObject(0).optString("text")
            }

            val bpmRegex = Regex("""(\d{2,3})\s*BPM""", RegexOption.IGNORE_CASE)
            val bpmMatch = bpmRegex.find(responseText)
            val extractedBpm = bpmMatch?.groupValues?.get(1)?.toIntOrNull()

            GeminiCoachResponse(
                answer = responseText.ifBlank { "Tavsiye üretilemedi." },
                thinkingProcess = thinkingText ?: if (useHighThinking) "Düşünme süreci: $instrument teknik gereksinimleri ve $level seviyesi değerlendirildi." else null,
                suggestedBpm = extractedBpm
            )
        } catch (e: Exception) {
            GeminiCoachResponse(
                answer = "Bağlantı hatası: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    suspend fun generateDailyExercise(
        instrument: String,
        level: String
    ): DailyExercise = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackDailyExercise(instrument, level)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val prompt = """
            $instrument çalan $level seviyesindeki bir müzisyen için günün ritim & hız egzersizini hazırla.
            JSON formatında tam olarak şu alanları döndür:
            {
              "title": "Egzersiz Başlığı",
              "patternName": "Ritim Kalıbı Adı",
              "targetBpm": 115,
              "timeSignature": "4/4",
              "subdivision": "1/16",
              "rhythmNotation": "1 e & a 2 e & a 3 e & a 4 e & a",
              "aiAdvice": "Önemli teknik koçluk tavsiyesi ve dikkat edilecek nokta",
              "speedBuildingSteps": ["80 BPM ile 2 dk", "95 BPM ile 2 dk", "115 BPM ile 3 dk"]
            }
            Sadece geçerli saf JSON döndür.
        """.trimIndent()

        val rootJson = JSONObject()
        rootJson.put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))))

        try {
            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext getFallbackDailyExercise(instrument, level)
            }

            val respObj = JSONObject(responseBody)
            val rawText = respObj.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            val cleanedJson = rawText.replace("```json", "").replace("```", "").trim()
            val parsed = JSONObject(cleanedJson)

            val stepsList = mutableListOf<String>()
            val stepsJson = parsed.optJSONArray("speedBuildingSteps")
            if (stepsJson != null) {
                for (i in 0 until stepsJson.length()) {
                    stepsList.add(stepsJson.getString(i))
                }
            }

            DailyExercise(
                title = parsed.optString("title", "Günün Ritim Egzersizi"),
                instrument = instrument,
                level = level,
                patternName = parsed.optString("patternName", "Linear Funk Groove"),
                targetBpm = parsed.optInt("targetBpm", 112),
                timeSignature = parsed.optString("timeSignature", "4/4"),
                subdivision = parsed.optString("subdivision", "1/16"),
                rhythmNotation = parsed.optString("rhythmNotation", "1 e & a 2 & 3 e & 4"),
                aiAdvice = parsed.optString("aiAdvice", "Hayalet vuruşların (ghost notes) dinamik dengesine odaklanın."),
                speedBuildingSteps = if (stepsList.isNotEmpty()) stepsList else listOf("60 BPM ısınma", "90 BPM tempo", "112 BPM hedef")
            )
        } catch (e: Exception) {
            getFallbackDailyExercise(instrument, level)
        }
    }

    private fun getFallbackDailyExercise(instrument: String, level: String): DailyExercise {
        return when (instrument) {
            "Davul / Drums" -> DailyExercise(
                title = "Linear Funk & Ghost Note Kontrolü",
                instrument = instrument,
                level = level,
                patternName = "Linear Funk Groove (16th notes)",
                targetBpm = 108,
                timeSignature = "4/4",
                subdivision = "1/16",
                rhythmNotation = "[K] . [H] [S] . [H] [K] . [S] [H] . [S]",
                aiAdvice = "Trampet hayalet vuruşlarında bilekleri gevşek tutun, ana backbeat (2 ve 4) vuruşlarını güçlü vurgulayın.",
                speedBuildingSteps = listOf("70 BPM: Koordinasyon oturtma", "90 BPM: Dinamik ayrıştırma", "108 BPM: Akıcı groove")
            )
            "Gitar / Guitar" -> DailyExercise(
                title = "16'lık Funk Strumming & Muted Vuruşlar",
                instrument = instrument,
                level = level,
                patternName = "Syncopated 16th Funk Chank",
                targetBpm = 115,
                timeSignature = "4/4",
                subdivision = "1/16",
                rhythmNotation = "D . U D M U M D D U M U D . U .",
                aiAdvice = "Sağ el sarkaç gibi sürekli 16'lık hareket etsin, sol el basıp bırakarak mute vuruşları ayarlasın.",
                speedBuildingSteps = listOf("80 BPM: Sağ el sürekliliği", "100 BPM: Mute netliği", "115 BPM: Groove")
            )
            "Bas Gitar / Bass" -> DailyExercise(
                title = "Slap & Pop Aksak Ritim Merdiveni",
                instrument = instrument,
                level = level,
                patternName = "Octave Slap Groove with Dead Notes",
                targetBpm = 105,
                timeSignature = "4/4",
                subdivision = "1/16",
                rhythmNotation = "T . T P X T P . T . X P T P . .",
                aiAdvice = "Thumb vuruşlarında başparmağınızı telden hemen sekterek rezonansı koruyun.",
                speedBuildingSteps = listOf("75 BPM: Başparmak doğruluğu", "90 BPM: Pop zamanlaması", "105 BPM: Hız")
            )
            else -> DailyExercise(
                title = "Metronomik Zamanlama ve Aksak Tartım",
                instrument = instrument,
                level = level,
                patternName = "Aksak 7/8 (3+2+2) Polimetrik Çalışma",
                targetBpm = 140,
                timeSignature = "7/8",
                subdivision = "1/8",
                rhythmNotation = "1-2-3  1-2  1-2 (DUM-tek-tek tek-tek)",
                aiAdvice = "İlk 3'lü vuruşun ağırlığını hissedin, ardından gelen iki 2'li grubu eşit sürede tutun.",
                speedBuildingSteps = listOf("90 BPM: Sayarak vuruş", "115 BPM: Rahat geçiş", "140 BPM: Performans")
            )
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun extractErrorMessage(jsonBody: String): String {
        return try {
            val obj = JSONObject(jsonBody)
            obj.optJSONObject("error")?.optString("message") ?: jsonBody.take(100)
        } catch (e: Exception) {
            jsonBody.take(100)
        }
    }
}
