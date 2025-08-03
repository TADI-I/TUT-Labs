package com.tadiwanashe.tutictlabs.Models

data class User(
    var id: String = "",     // e.g., Firebase UID
    var name: String = "",
    var email: String = "",
    var role: String = ""    // "admin" or "tutor"
)
