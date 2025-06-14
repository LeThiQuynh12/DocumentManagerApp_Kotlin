package com.example.documentmanagerapp.utils.data

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class DocumentVersionData(

    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("document")
    val document: DocumentData, // Bạn cần tạo class DocumentData nếu chưa có

    @SerializedName("versionNumber")
    val versionNumber: Int,

    @SerializedName("s3Url")
    val s3Url: String,

    @SerializedName("fileSize")
    val fileSize: Long,

    @SerializedName("createdAt")
    val createdAt: Timestamp? = null,

    @SerializedName("updatedAt")
    val updatedAt: Timestamp? = null
)
