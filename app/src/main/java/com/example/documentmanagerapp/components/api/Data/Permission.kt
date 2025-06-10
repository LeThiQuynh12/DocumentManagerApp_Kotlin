package com.example.documentmanagerapp.components.api.Data
import com.example.documentmanagerapp.components.api.ultil.PermissionType
import java.sql.Timestamp

data class Permission(
    val id: Long? = null,
    val user: User,
    val document: Document,
    val permissionType: PermissionType,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)