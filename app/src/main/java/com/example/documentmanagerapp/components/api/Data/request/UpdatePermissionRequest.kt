package com.example.documentmanagerapp.components.api.Data.request

import com.example.documentmanagerapp.components.api.ultil.PermissionType


data class UpdatePermissionRequest(
    var email: String,
    var documentId: Long,
    var permissionType: PermissionType
)