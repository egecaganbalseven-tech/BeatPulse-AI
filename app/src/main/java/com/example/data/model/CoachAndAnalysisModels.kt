package com.example.data.model

data class AiCoachMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val thinkingProcess: String? = null,
    val imageUrl: String? = null,
    val suggestedBpm: Int? = null,
    val suggestedPattern: String? = null
)

enum class MessageSender {
    USER,
    AI
}

data class DailyExercise(
    val title: String,
    val instrument: String,
    val level: String,
    val patternName: String,
    val targetBpm: Int,
    val timeSignature: String,
    val subdivision: String,
    val rhythmNotation: String,
    val aiAdvice: String,
    val speedBuildingSteps: List<String> = emptyList()
)

data class AnalysisResult(
    val detectedBpm: Int,
    val confidence: Float,
    val timeSignature: String = "4/4",
    val estimatedKey: String = "A Minor",
    val rhythmDescription: String = "Straight 16th Funk Beat",
    val syncopationLevel: String = "Medium (Aksak 3+3+2)",
    val tempoStability: String = "98% Kararlı"
)
