package com.tadiwanashe.tutictlabs.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchUserRole(user.uid)
            } else {
                _isLoggedIn.value = false
                _userRole.value = null
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid
                if (uid != null) {
                    fetchUserRole(uid)
                } else {
                    _errorMessage.value = "User ID not found."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Login failed"
            }
        }
    }

    private fun fetchUserRole(uid: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val role = doc.getString("role")
                    _userRole.value = role
                    _isLoggedIn.value = true
                } else {
                    _errorMessage.value = "Could not fetch user role."
                    _userRole.value = null
                    _isLoggedIn.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to fetch user role"
                _userRole.value = null
                _isLoggedIn.value = false
            }
        }
    }

    fun logout() {
        try {
            auth.signOut()
            _isLoggedIn.value = false
            _userRole.value = null
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage ?: "Logout failed"
        }
    }
}
