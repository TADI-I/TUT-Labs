package com.tadiwanashe.tutictlabs.ViewModels


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tadiwanashe.tutictlabs.Models.LabShift
import com.tadiwanashe.tutictlabs.Models.LabStatus
import com.tadiwanashe.tutictlabs.Models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LabViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _tutors = MutableStateFlow<List<User>>(emptyList())
    val tutors: StateFlow<List<User>> = _tutors

    private val _labShifts = MutableStateFlow<List<LabShift>>(emptyList())
    val labShifts: StateFlow<List<LabShift>> = _labShifts

    private val _labStatuses = MutableStateFlow<List<LabStatus>>(emptyList())
    val labStatuses: StateFlow<List<LabStatus>> = _labStatuses

    private val _shiftsForCurrentTutor = MutableStateFlow<List<LabShift>>(emptyList())
    val shiftsForCurrentTutor: StateFlow<List<LabShift>> = _shiftsForCurrentTutor

    init {
        fetchTutors()
        fetchShifts()
        fetchLabStatuses()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchLabStatuses() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("labStatus")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val statuses = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    LabStatus(
                        id = UUID.fromString(doc.id) ?: UUID.randomUUID(),
                        labName = data?.get("labName") as? String ?: return@mapNotNull null,
                        isOpen = data["labOpen"] as? Boolean ?: return@mapNotNull null,
                        note = data["note"] as? String ?: return@mapNotNull null,
                        updatedBy = data["updatedBy"] as? String ?: return@mapNotNull null,
                        updatedById = data["updatedById"] as? String ?: return@mapNotNull null,
                        timestamp = (data["timestamp"] as? Timestamp)?.toDate() ?: return@mapNotNull null
                    )
                }

                _labStatuses.value = statuses
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun fetchTutors() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .whereEqualTo("role", "tutor")
                    .get()
                    .await()

                val fetchedTutors = snapshot.documents.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        email = doc.getString("email") ?: return@mapNotNull null,
                        role = doc.getString("role") ?: return@mapNotNull null
                    )
                }

                _tutors.value = fetchedTutors
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    fun updateLabStatus(
        labName: String,
        isOpen: Boolean,
        note: String,
        isAdmin: Boolean
    ) {
        val user = Firebase.auth.currentUser ?: return
        val userId = user.uid
        val userName = user.displayName ?: "Unknown"

        val status = LabStatus(
            id = UUID.randomUUID(),
            labName = labName,
            isOpen = isOpen,
            note = note,
            timestamp = java.util.Date(),
            updatedById = userId,
            updatedBy = userName
        )

        db.collection("labStatus")
            .document(labName) // Use labName as ID if you want to override previous status
            .set(status)
            .addOnSuccessListener {
                fetchLabStatuses()
            }
    }


    fun addTutor(name: String, email: String) {
        viewModelScope.launch {
            try {
                val tempPassword = "Welcome123!"
                val result = auth.createUserWithEmailAndPassword(email, tempPassword).await()

                val uid = result.user?.uid ?: return@launch

                val tutorData = mapOf(
                    "name" to name,
                    "email" to email,
                    "role" to "tutor"
                )

                db.collection("users").document(uid).set(tutorData).await()
                fetchTutors() // Refresh list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeTutor(tutorId: String) {
        viewModelScope.launch {
            try {
                db.collection("users").document(tutorId).delete().await()
                fetchTutors() // Refresh list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun assignShift(tutorId: String, day: String, time: String, labName: String): Boolean {
        val duplicateExists = _labShifts.value.any { shift ->
            shift.labName == labName && shift.day == day && shift.time == time
        }

        if (duplicateExists) return false

        val newShift = LabShift(tutorId = tutorId, day = day, time = time, labName = labName)
        _labShifts.value = _labShifts.value + newShift
        saveShiftToFirestore(newShift)
        return true
    }

    fun deleteShift(shift: LabShift) {
        viewModelScope.launch {
            try {
                _labShifts.value = _labShifts.value.filter { it.id != shift.id }
                db.collection("labShifts").document(shift.id.toString()).delete().await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun saveShiftToFirestore(shift: LabShift) {
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "tutorId" to shift.tutorId,
                    "day" to shift.day,
                    "time" to shift.time,
                    "labName" to shift.labName
                )

                db.collection("labShifts").document(shift.id.toString()).set(data).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun fetchShifts() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("labShifts").get().await()

                val fetchedShifts = snapshot.documents.mapNotNull { doc ->
                    LabShift(
                        id = UUID.fromString(doc.id) ?: UUID.randomUUID(),
                        tutorId = doc.getString("tutorId") ?: return@mapNotNull null,
                        day = doc.getString("day") ?: return@mapNotNull null,
                        time = doc.getString("time") ?: return@mapNotNull null,
                        labName = doc.getString("labName") ?: return@mapNotNull null
                    )
                }

                _labShifts.value = fetchedShifts
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun fetchShiftsForCurrentTutor() {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: run {
                    _shiftsForCurrentTutor.value = emptyList()
                    return@launch
                }

                val snapshot = db.collection("labShifts")
                    .whereEqualTo("tutorId", uid)
                    .get()
                    .await()

                val shifts = snapshot.documents.mapNotNull { doc ->
                    LabShift(
                        id = UUID.fromString(doc.id) ?: UUID.randomUUID(),
                        tutorId = uid,
                        day = doc.getString("day") ?: return@mapNotNull null,
                        time = doc.getString("time") ?: return@mapNotNull null,
                        labName = doc.getString("labName") ?: return@mapNotNull null
                    )
                }

                _shiftsForCurrentTutor.value = shifts
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}