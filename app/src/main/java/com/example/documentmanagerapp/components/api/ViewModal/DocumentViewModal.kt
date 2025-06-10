package com.example.documentmanagerapp.components.api.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.documentmanagerapp.components.api.Data.Document
import com.example.documentmanagerapp.components.api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentViewModel : ViewModel() {
    val documents = mutableStateOf<List<Document>>(emptyList())
    private val TAG = "DocumentVM"

    fun fetchAllDocuments() {
        Log.d(TAG, "fetchAllDocuments called")
        RetrofitInstance.api.getAllDocuments().enqueue(object : Callback<List<Document>> {
            override fun onResponse(call: Call<List<Document>>, response: Response<List<Document>>) {
                if (response.isSuccessful) {
                    documents.value = response.body() ?: emptyList()
                    Log.d(TAG, "Documents: ${documents.value}")
                } else {
                    Log.e(TAG, "API Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Document>>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
            }
        })
    }

}
