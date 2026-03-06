package com.carevalojesus.eventosya.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = ROLE_USER
) {
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_STAFF = "staff"
        const val ROLE_USER = "usuario"
    }
}
