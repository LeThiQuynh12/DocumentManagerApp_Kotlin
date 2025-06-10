package com.example.documentmanagerapp.components.api.Data
import java.sql.Timestamp

data class AccessLog(
    val id: Long? = null,
    val user: User,
    val document: Document,
    val accessTime: Timestamp = Timestamp(System.currentTimeMillis()),
    val action: ActionType
) {
    enum class ActionType {
        VIEW, EDIT, DOWNLOAD
    }
}