package com.tadiwanashe.tutictlabs.ViewModels

import android.R.attr.textSize
import android.graphics.Rect
import android.content.ContentValues.TAG
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.graphics.Paint
import android.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tadiwanashe.tutictlabs.Models.LabShift
import com.tadiwanashe.tutictlabs.Models.LabStatus
import com.tadiwanashe.tutictlabs.Models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import android.content.Context
import android.graphics.Canvas
import com.google.firebase.firestore.FieldValue
import com.tadiwanashe.tutictlabs.Models.LabSession
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil


class LabViewModel : ViewModel(){
    private val _tutors = MutableStateFlow<List<User>>(emptyList())
    val tutors: StateFlow<List<User>> = _tutors.asStateFlow()

    private val _labShifts = MutableStateFlow<List<LabShift>>(emptyList())
    val labShifts: StateFlow<List<LabShift>> = _labShifts.asStateFlow()

    private val _labStatuses = MutableStateFlow<List<LabStatus>>(emptyList())
    val labStatuses: StateFlow<List<LabStatus>> = _labStatuses.asStateFlow()

    val _shiftsForCurrentTutor = MutableStateFlow<List<LabShift>>(emptyList())
    val shiftsForCurrentTutor: StateFlow<List<LabShift>> = _shiftsForCurrentTutor.asStateFlow()

    private val db = FirebaseFirestore.getInstance()


    init {
        fetchTutors()
        fetchShifts()
        fetchLabStatuses()
    }

    fun fetchLabStatuses() {
        db.collection("labStatus")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val statuses = task.result?.documents?.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            LabStatus(
                                id = doc.id,
                                labName = data["labName"] as? String ?: "",
                                isOpen = data["labOpen"] as? Boolean ?: false,
                                note = data["note"] as? String ?: "",
                                updatedBy = data["updatedBy"] as? String ?: "",
                                updatedById = data["updatedById"] as? String ?: "",
                                timestamp = (data["timestamp"] as? Timestamp)?.toDate() ?: Date()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    _labStatuses.value = statuses
                    Log.d("LabViewModel", "Successfully loaded ${statuses.size} lab statuses")
                } else {
                    Log.e("LabViewModel", "Error fetching lab statuses", task.exception)
                }
            }
    }

    fun fetchTutors() {
        db.collection("users")
            .whereEqualTo("role", "tutor")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedTutors = documents.mapNotNull { doc ->
                    val data = doc.data
                    val name = data["name"] as? String
                    val email = data["email"] as? String
                    val role = data["role"] as? String

                    if (name != null && email != null && role != null) {
                        User(id = doc.id, name = name, email = email, role = role)
                    } else {
                        null
                    }
                }
                _tutors.value = fetchedTutors
            }
            .addOnFailureListener { exception ->
                println("Error fetching tutors: ${exception.localizedMessage}")
            }
    }

    fun fetchShifts() {
        db.collection("labShifts")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedShifts = documents.mapNotNull { doc ->
                    val data = doc.data
                    val tutorId = data["tutorId"] as? String
                    val day = data["day"] as? String
                    val time = data["time"] as? String
                    val labName = data["labName"] as? String

                    if (tutorId != null && day != null && time != null && labName != null) {
                        LabShift(
                            id = doc.id,
                            tutorId = tutorId,
                            day = day,
                            time = time,
                            labName = labName
                        )
                    } else {
                        null
                    }
                }
                _labShifts.value = fetchedShifts
            }
            .addOnFailureListener { exception ->
                println("Error fetching shifts: ${exception.localizedMessage}")
            }
    }

    fun fetchTutorWeeklyShifts(tutorId: String, completion: (List<LabShift>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Get start of week (Monday)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startOfWeek = calendar.time

        // Get end of week (Sunday)
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val endOfWeek = calendar.time

        db.collection("labShifts")
            .whereEqualTo("tutorId", tutorId)
            .get()
            .addOnSuccessListener { documents ->
                val shifts = documents.mapNotNull { doc ->
                    val data = doc.data
                    val day = data["day"] as? String
                    val labName = data["labName"] as? String
                    val time = data["time"] as? String
                    val tutorId = data["tutorId"] as? String

                    if (day != null && labName != null && time != null && tutorId != null) {
                        // Optional: filter by week range here if you have a Date field
                        LabShift(
                            tutorId = tutorId,
                            day = day,
                            time = time,
                            labName = labName
                        )
                    } else {
                        null
                    }
                }
                completion(shifts)
            }
            .addOnFailureListener { exception ->
                Log.e("ScheduleView", "Error fetching shifts: ${exception.localizedMessage}")
                completion(emptyList())
            }
    }
    fun generateTutorWeeklySchedulePDF(context: Context, weekRange: String, shifts: List<LabShift>): Uri? {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            Log.e("PDF", "No logged-in user")
            return null
        }

        val tutorName = currentUser.displayName ?: "Tutor"
        val safeTutorName = tutorName.replace(" ", "_")
        val safeFileName = "${safeTutorName}_Schedule_${weekRange.replace(" ", "_")}.pdf"

        try {
            // Create a file in cache directory
            val file = File(context.cacheDir, safeFileName)
            val outputStream = FileOutputStream(file)

            // Create PDF document
            val document = PdfDocument()

            // Page size (US Letter: 8.5x11 inches in points)
            val pageWidth = 8.5f * 72
            val pageHeight = 11f * 72
            val margin = 50f
            val contentWidth = pageWidth - 2 * margin

            // Create page
            val pageInfo = PdfDocument.PageInfo.Builder(
                pageWidth.toInt(),
                pageHeight.toInt(),
                1
            ).create()

            val page = document.startPage(pageInfo)
            var canvas = page.canvas

            // Draw title
            val title = "FoICT $tutorName Weekly Lab Schedule"
            val titlePaint = Paint().apply {
                textSize = 20f
                isFakeBoldText = true
                color = Color.BLACK
            }

            val titleWidth = titlePaint.measureText(title)
            canvas.drawText(
                title,
                (pageWidth - titleWidth) / 2,
                margin,
                titlePaint
            )

            // Draw week range
            val weekText = "Week: $weekRange"
            val weekPaint = Paint().apply {
                textSize = 14f
                color = Color.BLACK
            }

            val weekWidth = weekPaint.measureText(weekText)
            val weekY = margin + 30
            canvas.drawText(
                weekText,
                (pageWidth - weekWidth) / 2,
                weekY,
                weekPaint
            )

            // Table setup
            var yPosition = weekY + 40
            val headerPaint = Paint().apply {
                textSize = 14f
                isFakeBoldText = true
                color = Color.BLACK
            }

            val rowPaint = Paint().apply {
                textSize = 12f
                color = Color.BLACK
            }

            val columnWidths = listOf(
                contentWidth * 0.25f,
                contentWidth * 0.5f,
                contentWidth * 0.25f
            )

            // Draw headers
            val headers = listOf("Day", "Lab", "Time")
            var xPosition = margin
            headers.forEachIndexed { index, header ->
                canvas.drawText(header, xPosition, yPosition, headerPaint)
                xPosition += columnWidths[index]
            }

            // Draw line under headers
            yPosition += 20
            canvas.drawLine(
                margin,
                yPosition,
                pageWidth - margin,
                yPosition,
                Paint().apply {
                    color = Color.BLACK
                    strokeWidth = 1f
                }
            )

            yPosition += 10

            // Draw shifts
            for (shift in shifts) {
                xPosition = margin

                // Day
                canvas.drawText(shift.day, xPosition, yPosition, rowPaint)
                xPosition += columnWidths[0]

                // Lab
                canvas.drawText(shift.labName, xPosition, yPosition, rowPaint)
                xPosition += columnWidths[1]

                // Time
                canvas.drawText(shift.time, xPosition, yPosition, rowPaint)

                yPosition += 30

                // Check for page overflow
                if (yPosition > pageHeight - margin - 40) {
                    document.finishPage(page)
                    val newPage = document.startPage(pageInfo)
                    canvas = newPage.canvas
                    yPosition = margin
                }
            }

            // Draw footer
            val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val footer = "Generated: ${dateFormat.format(Date())}"
            val footerPaint = Paint().apply {
                textSize = 10f
                color = Color.GRAY
            }

            val footerWidth = footerPaint.measureText(footer)
            canvas.drawText(
                footer,
                (pageWidth - footerWidth) / 2,
                pageHeight - margin,
                footerPaint
            )

            document.finishPage(page)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()

            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("PDF", "Could not create PDF: ${e.message}")
            return null
        }
    }

    fun fetchShiftsForCurrentTutor() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.d("TAG", "No logged-in user")
            _shiftsForCurrentTutor.value = emptyList()
            return
        }
        Log.d("TAG", "Fetching shifts for tutorId: $uid")

        FirebaseFirestore.getInstance()
            .collection("labShifts")
            .whereEqualTo("tutorId", uid)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("TAG", "Firestore returned ${documents.size()} documents")

                val shifts = documents.mapNotNull { doc ->
                    val data = doc.data
                    val day = data["day"] as? String
                    val time = data["time"] as? String
                    val labName = data["labName"] as? String

                    if (day != null && time != null && labName != null) {
                        LabShift(
                            id = doc.id,
                            tutorId = uid,
                            day = day,
                            time = time,
                            labName = labName
                        )
                    } else {
                        null
                    }
                }

                _shiftsForCurrentTutor.value = shifts
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error fetching shifts:", exception)
                _shiftsForCurrentTutor.value = emptyList()
            }
    }

    fun openLabSession(labName: String, note: String = "") {
        println("openLabSession called")
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            println("No logged-in user")
            return
        }

        // Get name from Firestore instead of relying on displayName
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                val userName = doc.getString("name") ?: currentUser.displayName ?: "Unknown"
                val userId = currentUser.uid

                val sessionData = hashMapOf(
                    "labName" to labName,
                    "openedById" to userId,
                    "openedByName" to userName,
                    "openedAt" to FieldValue.serverTimestamp(),
                    "note" to note,
                    "closedAt" to null,       // So we can query for it later
                    "closedById" to null,
                    "closedByName" to null
                )

                db.collection("labSessions")
                    .add(sessionData)
                    .addOnSuccessListener {
                        println("Lab session opened successfully")
                    }
                    .addOnFailureListener { e ->
                        println("Error opening lab session: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener { e ->
                println("Error fetching user data: ${e.localizedMessage}")
            }
    }

    fun closeLabSession(labName: String, note: String = "") {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            println("No logged-in user")
            return
        }

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                val userName = doc.getString("name") ?: currentUser.displayName ?: "Unknown"
                val userId = currentUser.uid

                // Find the latest session where closedAt is null
                FirebaseFirestore.getInstance()
                    .collection("labSessions")
                    .whereEqualTo("labName", labName)
                    .whereEqualTo("closedAt", null)  // must exist as null from creation
                    .orderBy("openedAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.isEmpty) {
                            println("No open lab session found to close")
                            return@addOnSuccessListener
                        }

                        val doc = snapshot.documents[0]
                        doc.reference.update(
                            mapOf(
                                "closedAt" to FieldValue.serverTimestamp(),
                                "closedById" to userId,
                                "closedByName" to userName,
                                "note" to note
                            )
                        ).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("Lab session closed successfully")
                            } else {
                                task.exception?.let {
                                    println("Error closing lab session: ${it.localizedMessage}")
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching open lab session: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener { e ->
                println("Error fetching user data: ${e.localizedMessage}")
            }
    }

    fun fetchOpenSessions(completion: (List<LabSession>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("labSessions")
            .whereEqualTo("closedAt", null) // sessions with no close timestamp = open sessions
            .get()
            .addOnSuccessListener { snapshot ->
                val sessions = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    val labName = data?.get("labName") as? String
                    val openedById = data?.get("openedById") as? String
                    val openedByName = data?.get("openedByName") as? String
                    val openedAtTimestamp = data?.get("openedAt") as? com.google.firebase.Timestamp

                    if (labName != null && openedById != null && openedByName != null && openedAtTimestamp != null) {
                        LabSession(
                            id = doc.id,
                            labName = labName,
                            openedById = openedById,
                            openedByName = openedByName,
                            openedAt = openedAtTimestamp.toDate(),
                            closedById = null,
                            closedByName = null,
                            closedAt = null,
                            note = data?.get("note") as? String ?: ""
                        )
                    } else {
                        null
                    }
                }
                completion(sessions)
            }
            .addOnFailureListener { exception ->
                Log.e("LabViewModel", "Error fetching open sessions: ${exception.localizedMessage}")
                completion(emptyList())
            }
    }

    fun addTutor(name: String, email: String) {
        val tempPassword = "Welcome123!" // You can generate a stronger one too

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, tempPassword)
            .addOnCompleteListener { authTask ->
                if (!authTask.isSuccessful) {
                    authTask.exception?.let {
                        Log.e("TAG", "Failed to create auth account: ${it.localizedMessage}")
                    }
                    return@addOnCompleteListener
                }

                val uid = authTask.result?.user?.uid ?: return@addOnCompleteListener

                val tutorData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "role" to "tutor"
                )

                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .set(tutorData)
                    .addOnCompleteListener { firestoreTask ->
                        if (firestoreTask.isSuccessful) {
                            Log.d("TAG", "Tutor added successfully")
                            fetchTutors() // Refresh the local list
                        } else {
                            firestoreTask.exception?.let {
                                Log.e("TAG", "Failed to save tutor to Firestore: ${it.localizedMessage}")
                            }
                        }
                    }
            }
    }

    fun removeTutor(tutorId: String) {
        FirebaseFirestore.getInstance().collection("users").document(tutorId)
            .delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "Tutor removed successfully")
                    fetchTutors() // Refresh list after deleting
                } else {
                    task.exception?.let {
                        Log.e("TAG", "Error removing tutor: ${it.localizedMessage}")
                    }
                }
            }
    }

    fun assignShift(tutorId: String, day: String, time: String, labName: String): Boolean {
        // Check for duplicate
        val duplicateExists = _labShifts.value.any { shift ->
            shift.labName == labName &&
                    shift.day == day &&
                    shift.time == time
        }

        if (duplicateExists) {
            return false
        }

        // Create and save new shift
        val newShift = LabShift(
            id = UUID.randomUUID().toString(), // Using String ID instead of UUID
            tutorId = tutorId,
            day = day,
            time = time,
            labName = labName
        )

        _labShifts.value = _labShifts.value + newShift
        saveShiftToFirestore(newShift)
        return true
    }

    fun deleteShift(shift: LabShift) {
        // Remove locally
        _labShifts.value = _labShifts.value.filter { it.id != shift.id }

        // Remove from Firestore
        FirebaseFirestore.getInstance()
            .collection("labShifts")
            .document(shift.id)
            .delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Shift deleted successfully!")
                } else {
                    task.exception?.let {
                        Log.e(TAG, "Error deleting shift: ${it.localizedMessage}")
                    }
                }
            }
    }

    private fun saveShiftToFirestore(shift: LabShift) {
        val data = hashMapOf(
            "tutorId" to shift.tutorId,
            "day" to shift.day,
            "time" to shift.time,
            "labName" to shift.labName
        )

        FirebaseFirestore.getInstance()
            .collection("labShifts")
            .document(shift.id)
            .set(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Shift saved successfully!")
                } else {
                    task.exception?.let {
                        Log.e(TAG, "Error writing shift to Firestore: ${it.localizedMessage}")
                    }
                }
            }
    }

    fun fetchWeeklyLabSessions(
        startDate: Date,
        endDate: Date,
        completion: (List<LabSession>) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("labSessions")
            .whereGreaterThanOrEqualTo("openedAt", startDate)
            .whereLessThanOrEqualTo("openedAt", endDate)
            .orderBy("openedAt", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val sessions = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    val labName = data?.get("labName") as? String
                    val openedById = data?.get("openedById") as? String
                    val openedByName = data?.get("openedByName") as? String
                    val openedAt = data?.get("openedAt") as? Timestamp

                    if (labName != null && openedById != null && openedByName != null && openedAt != null) {
                        val closedAt = data?.get("closedAt") as? Timestamp
                        val closedById = data?.get("closedById") as? String
                        val closedByName = data?.get("closedByName") as? String
                        val note = data?.get("note") as? String ?: ""

                        LabSession(
                            id = doc.id,
                            labName = labName,
                            openedById = openedById,
                            openedByName = openedByName,
                            openedAt = openedAt.toDate(),
                            closedById = closedById,
                            closedByName = closedByName,
                            closedAt = closedAt?.toDate(),
                            note = note
                        )
                    } else {
                        null
                    }
                }
                completion(sessions)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error fetching lab sessions", exception)
                completion(emptyList())
            }
    }

    fun generateWeeklyLabReport(
        context: Context,
        sessions: List<LabSession>,
        weekRange: String
    ): Uri? {
        try {
            // Create PDF document
            val document = PdfDocument()

            // Page size (US Letter: 8.5x11 inches in points)
            val pageWidth = 8.5f * 72
            val pageHeight = 11f * 72
            val margin = 72f
            val contentWidth = pageWidth - 2 * margin

            // Create page info
            val pageInfo = PdfDocument.PageInfo.Builder(
                pageWidth.toInt(),
                pageHeight.toInt(),
                1
            ).create()

            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            // Title paint
            val titlePaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
            }

            // Draw title
            val title = "FoICT Weekly Lab Activity Report ($weekRange)"
            canvas.drawText(
                title,
                margin,
                60f,
                titlePaint
            )

            // Body text paint
            val textPaint = Paint().apply {
                textSize = 12f
            }

            var yPosition = 110f

            // Date formatters
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            for (session in sessions) {
                // Format session data
                val openedAt = dateFormat.format(session.openedAt) + " at " +
                        timeFormat.format(session.openedAt)
                val openedStr = "Opened: $openedAt by ${session.openedByName}"

                var closedStr = "Closed: N/A"
                session.closedAt?.let { closedAt ->
                    session.closedByName?.let { closedByName ->
                        val closedAtFormatted = dateFormat.format(closedAt) + " at " +
                                timeFormat.format(closedAt)
                        closedStr = "Closed: $closedAtFormatted by $closedByName"
                    }
                }

                val noteStr = if (session.note.isEmpty()) "" else "Note: ${session.note}"

                val entry = """
                Lab: ${session.labName}
                $openedStr
                $closedStr
                $noteStr
                
                """.trimIndent()

                // Check if we need a new page
                val textHeight = measureTextHeight(entry, textPaint, contentWidth)
                if (yPosition + textHeight > pageHeight - margin) {
                    drawFooter(canvas, pageWidth, pageHeight)
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = margin
                }

                // Draw the text
                drawMultilineText(canvas, entry, margin, yPosition, textPaint, contentWidth)
                yPosition += textHeight + 12f
            }

            // Draw footer on last page
            drawFooter(canvas, pageWidth, pageHeight)
            document.finishPage(page)

            // Save to file
            val fileName = "Lab_Activity_${weekRange.replace(" ", "_")}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()

            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("TAG", "Could not create PDF", e)
            return null
        }
    }

    private fun measureTextHeight(text: String, paint: Paint, width: Float): Int {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val textWidth = paint.measureText(text)
        val lines = ceil(textWidth / width).toInt()
        return lines * (bounds.height() + 2) // Add some padding
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        width: Float
    ) {
        var currentY = y
        var currentText = text

        while (currentText.isNotEmpty()) {
            val count = paint.breakText(currentText, true, width, null)
            val line = currentText.substring(0, count)
            canvas.drawText(line, x, currentY, paint)
            currentY += paint.textSize + 2 // Line spacing
            currentText = currentText.substring(count)
        }
    }

    private fun drawFooter(canvas: Canvas, pageWidth: Float, pageHeight: Float) {
        val footerPaint = Paint().apply {
            textSize = 10f
            color = Color.GRAY
        }

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
        val footerText = "Generated on ${dateFormat.format(Date())}"

        val textWidth = footerPaint.measureText(footerText)
        canvas.drawText(
            footerText,
            (pageWidth - textWidth) / 2,
            pageHeight - 40f,
            footerPaint
        )
    }
}