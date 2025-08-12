package com.tadiwanashe.tutictlabs.Models

import java.util.*

// Equivalent to Swift's `var` for mutable properties
data class LabStatus(
    var id: String = UUID.randomUUID().toString(),
    var labName: String = "",
    var isOpen: Boolean = false,
    var note: String = "",
    var updatedBy: String = "",
    var updatedById: String = "",
    var timestamp: Date = Date()
)

data class LabShift(
    var id: String = UUID.randomUUID().toString(),
    var tutorId: String = "",
    var day: String = "",
    var time: String = "",
    var labName: String = ""
)

data class LabSession(
    val id: String,
    val labName: String,
    val openedById: String,
    val openedByName: String,
    val openedAt: Date,
    val closedById: String?,
    val closedByName: String?,
    val closedAt: Date?,
    val note: String
)