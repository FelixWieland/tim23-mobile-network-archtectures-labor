package com.example.mobilenetworkarchitecture.ui.exercise1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Exercise1ViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Loading telephony information..."
    }
    val text: LiveData<String> = _text

    fun updateTelephonyInfo(info: String) {
        _text.value = info
    }
}

