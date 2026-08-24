package com.example.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class UserProfileState(
    val isSignedIn: Boolean = true,
    val userId: String? = "local_musician_1",
    val displayName: String? = "BeatPulse Müzisyeni",
    val email: String? = "musician@beatpulse.studio",
    val photoUrl: String? = null,
    val currentStreakDays: Int = 5,
    val totalPracticeMinutes: Int = 180
)

class FirebaseManager(private val context: Context) {

    private val isFirebaseAvailable: Boolean by lazy {
        try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (t: Throwable) {
            Log.w("FirebaseManager", "FirebaseApp not initialized: ${t.message}")
            false
        }
    }

    private val auth: FirebaseAuth?
        get() = try {
            if (isFirebaseAvailable) FirebaseAuth.getInstance() else null
        } catch (t: Throwable) {
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            if (isFirebaseAvailable) FirebaseFirestore.getInstance() else null
        } catch (t: Throwable) {
            null
        }

    private val _userState = MutableStateFlow(UserProfileState())
    val userState: StateFlow<UserProfileState> = _userState.asStateFlow()

    init {
        try {
            checkCurrentUser()
        } catch (t: Throwable) {
            Log.w("FirebaseManager", "checkCurrentUser failed gracefully: ${t.message}")
        }
    }

    private fun checkCurrentUser() {
        val currentAuth = auth ?: return
        val user = currentAuth.currentUser
        if (user != null) {
            updateUserState(user)
        }
    }

    private fun updateUserState(user: FirebaseUser) {
        _userState.value = UserProfileState(
            isSignedIn = true,
            userId = user.uid,
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "BeatPulse Müzisyeni",
            email = user.email ?: "musician@beatpulse.studio",
            photoUrl = user.photoUrl?.toString(),
            currentStreakDays = 5,
            totalPracticeMinutes = 240
        )
    }

    suspend fun syncPracticeSessionToCloud(
        instrument: String,
        bpm: Int,
        durationSeconds: Int,
        accuracyScore: Int
    ) {
        val currentAuth = auth ?: return
        val currentFirestore = firestore ?: return
        val user = currentAuth.currentUser ?: return
        try {
            val sessionData = hashMapOf(
                "userId" to user.uid,
                "instrument" to instrument,
                "bpm" to bpm,
                "durationSeconds" to durationSeconds,
                "accuracyScore" to accuracyScore,
                "timestamp" to System.currentTimeMillis()
            )
            currentFirestore.collection("users")
                .document(user.uid)
                .collection("sessions")
                .add(sessionData)
                .await()
        } catch (_: Exception) {
            // Handled offline gracefully
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (_: Throwable) {}
        _userState.value = UserProfileState(
            isSignedIn = false,
            userId = null,
            displayName = "Misafir Müzisyen",
            email = null
        )
    }

    fun mockSignInSuccess(name: String, email: String) {
        _userState.value = UserProfileState(
            isSignedIn = true,
            userId = "user_demo_123",
            displayName = name,
            email = email,
            currentStreakDays = 7,
            totalPracticeMinutes = 320
        )
    }
}

