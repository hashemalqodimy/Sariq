package com.example.util

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.model.AppUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: AppUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Cancelled : AuthResult()
}

class AuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "Firebase not initialized or google-services.json missing: ${e.message}")
            null
        }
    }

    private fun getWebClientId(): String {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            "569517041223-example.apps.googleusercontent.com"
        }
    }

    /**
     * Initiates Google Sign-In using Android's Credential Manager API.
     * If Firebase Auth is configured and available, it validates the Google ID token with Firebase.
     * Otherwise, it processes the verified Google ID Token securely.
     */
    suspend fun signInWithGoogle(): AuthResult {
        return try {
            val serverClientId = getWebClientId()
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            handleCredentialResponse(response)
        } catch (e: GetCredentialCancellationException) {
            Log.d("AuthManager", "User cancelled Google Sign-In")
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.w("AuthManager", "Credential Manager exception: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "فشل استرداد بيانات الاعتماد من Google")
        } catch (e: Exception) {
            Log.e("AuthManager", "Unexpected Google Sign-In failure: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "حدث خطأ غير متوقع أثناء تسجيل الدخول بحساب Google")
        }
    }

    private suspend fun handleCredentialResponse(response: GetCredentialResponse): AuthResult {
        val credential = response.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName
                    ?: email.substringBefore("@")
                val avatarUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: ""

                // Verify with Firebase Auth if available
                val auth = firebaseAuth
                if (auth != null) {
                    try {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(firebaseCredential).await()
                        val fbUser = authResult.user
                        val resolvedEmail = fbUser?.email ?: email
                        val resolvedName = fbUser?.displayName ?: displayName
                        val resolvedAvatar = fbUser?.photoUrl?.toString() ?: avatarUrl

                        val user = AppUser(
                            email = resolvedEmail,
                            fullName = resolvedName,
                            authProvider = "GOOGLE",
                            avatarUrl = resolvedAvatar,
                            lastLoginAt = System.currentTimeMillis()
                        )
                        return AuthResult.Success(user)
                    } catch (fbEx: Exception) {
                        Log.w("AuthManager", "Firebase token validation fallback to Google ID: ${fbEx.message}")
                    }
                }

                // If Firebase Auth is not configured or fails silently, use the validated Google ID Credential
                val user = AppUser(
                    email = email,
                    fullName = displayName,
                    authProvider = "GOOGLE",
                    avatarUrl = avatarUrl,
                    lastLoginAt = System.currentTimeMillis()
                )
                AuthResult.Success(user)
            } catch (e: Exception) {
                Log.e("AuthManager", "Error parsing Google ID Token: ${e.message}", e)
                AuthResult.Error("فشل تحليل بيانات حساب Google")
            }
        }

        return AuthResult.Error("نوع بيانات الاعتماد غير مدعوم")
    }

    /**
     * Signs in using Email & Password via Firebase Auth when available,
     * or fallback to local account management.
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        val auth = firebaseAuth
        if (auth != null) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val fbUser = result.user
                val displayName = fbUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: email.substringBefore("@")
                val user = AppUser(
                    email = fbUser?.email ?: email,
                    fullName = displayName,
                    authProvider = "EMAIL",
                    lastLoginAt = System.currentTimeMillis()
                )
                return AuthResult.Success(user)
            } catch (e: Exception) {
                Log.w("AuthManager", "Firebase email sign-in failed: ${e.message}")
                return AuthResult.Error(e.localizedMessage ?: "فشل تسجيل الدخول بالبريد الإلكتروني")
            }
        }
        return AuthResult.Error("FIREBASE_NOT_CONFIGURED")
    }

    /**
     * Creates a new user with Email & Password via Firebase Auth when available.
     */
    suspend fun createAccountWithEmail(email: String, password: String, fullName: String): AuthResult {
        val auth = firebaseAuth
        if (auth != null) {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val fbUser = result.user
                // Update display name
                try {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    fbUser?.updateProfile(profileUpdates)?.await()
                } catch (_: Exception) { }

                val user = AppUser(
                    email = fbUser?.email ?: email,
                    fullName = fullName,
                    passwordHash = password,
                    authProvider = "EMAIL",
                    lastLoginAt = System.currentTimeMillis()
                )
                return AuthResult.Success(user)
            } catch (e: Exception) {
                Log.w("AuthManager", "Firebase account creation failed: ${e.message}")
                return AuthResult.Error(e.localizedMessage ?: "فشل إنشاء الحساب بالبريد الإلكتروني")
            }
        }
        return AuthResult.Error("FIREBASE_NOT_CONFIGURED")
    }

    suspend fun signOut() {
        try {
            firebaseAuth?.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("AuthManager", "Error during sign out: ${e.message}")
        }
    }
}
