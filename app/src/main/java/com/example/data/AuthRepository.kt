package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String = "",
    val isDemo: Boolean = false
)

class AuthRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null

    init {
        try {
            // Intenta obtener la instancia de Firebase. Si falla, es probable que google-services.json falte.
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
                val currentFbUser = firebaseAuth?.currentUser
                if (currentFbUser != null) {
                    _currentUser.value = AuthUser(
                        uid = currentFbUser.uid,
                        email = currentFbUser.email ?: "",
                        displayName = currentFbUser.displayName ?: "Agente Turístico"
                    )
                }
            } else {
                android.util.Log.e("AuthRepository", "Firebase no está inicializado. Verifica google-services.json")
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error al inicializar Firebase: ${e.message}")
            firebaseAuth = null
        }

        // If no Firebase user, check local saved session
        if (_currentUser.value == null) {
            val savedUid = prefs.getString("saved_uid", null)
            val savedEmail = prefs.getString("saved_email", null)
            if (savedUid != null && savedEmail != null) {
                _currentUser.value = AuthUser(
                    uid = savedUid,
                    email = savedEmail,
                    displayName = prefs.getString("saved_name", "Agente Turístico") ?: "Agente",
                    isDemo = prefs.getBoolean("is_demo", false)
                )
            }
        }
    }

    fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && firebaseAuth != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(email: String, pass: String): Result<AuthUser> {
        val auth = firebaseAuth
        return if (auth != null) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                val loggedUser = AuthUser(
                    uid = user?.uid ?: "fb_user",
                    email = user?.email ?: email,
                    displayName = user?.displayName ?: "Agente Turístico"
                )
                saveSession(loggedUser)
                Result.success(loggedUser)
            } catch (e: Exception) {
                // If Firebase fails due to network or missing config, check local credential or return failure
                Result.failure(e)
            }
        } else {
            // Local mode fallback
            val user = AuthUser(
                uid = "agent_" + email.hashCode().toString(),
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                isDemo = false
            )
            saveSession(user)
            Result.success(user)
        }
    }

    suspend fun register(email: String, pass: String): Result<AuthUser> {
        val auth = firebaseAuth
        return if (auth != null) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                val newUser = AuthUser(
                    uid = user?.uid ?: "fb_user",
                    email = user?.email ?: email,
                    displayName = "Agente " + email.substringBefore("@")
                )
                saveSession(newUser)
                Result.success(newUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Local mode register
            val user = AuthUser(
                uid = "agent_" + email.hashCode().toString(),
                email = email,
                displayName = "Agente " + email.substringBefore("@"),
                isDemo = false
            )
            saveSession(user)
            Result.success(user)
        }
    }

    /**
     * Inicia sesión con una cuenta de Google usando Credential Manager,
     * y la vincula con Firebase Auth mediante GoogleAuthProvider.
     * @param activityContext debe ser el Context de la Activity (no el de la aplicación),
     * ya que Credential Manager necesita mostrar el selector de cuentas sobre la pantalla actual.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<AuthUser> {
        val auth = firebaseAuth
            ?: return Result.failure(IllegalStateException("Firebase no está configurado. Asegúrate de añadir google-services.json a la carpeta /app y sincronizar el proyecto."))

        return try {
            val webClientId = activityContext.getString(R.string.default_web_client_id)
            if (webClientId.contains("YOUR_WEB_CLIENT_ID")) {
                return Result.failure(IllegalStateException("Debes configurar tu Web Client ID real en strings.xml"))
            }

            val credentialManager = CredentialManager.create(activityContext)

            // Nonce aleatorio para proteger la solicitud contra ataques de repetición
            val rawNonce = UUID.randomUUID().toString()
            val hashedNonce = MessageDigest.getInstance("SHA-256")
                .digest(rawNonce.toByteArray())
                .joinToString("") { "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(activityContext.getString(R.string.default_web_client_id))
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val fbUser = authResult.user

                val loggedUser = AuthUser(
                    uid = fbUser?.uid ?: "google_user",
                    email = fbUser?.email ?: googleIdTokenCredential.id,
                    displayName = fbUser?.displayName
                        ?: googleIdTokenCredential.displayName
                        ?: "Agente Turístico"
                )
                saveSession(loggedUser)
                Result.success(loggedUser)
            } else {
                Result.failure(IllegalStateException("Tipo de credencial de Google inesperado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveSession(user: AuthUser) {
        _currentUser.value = user
        prefs.edit()
            .putString("saved_uid", user.uid)
            .putString("saved_email", user.email)
            .putString("saved_name", user.displayName)
            .putBoolean("is_demo", user.isDemo)
            .apply()
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        prefs.edit().clear().apply()
        _currentUser.value = null
    }
}