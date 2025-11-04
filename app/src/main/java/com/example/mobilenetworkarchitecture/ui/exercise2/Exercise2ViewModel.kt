package com.example.mobilenetworkarchitecture.ui.exercise2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Exercise2ViewModel : ViewModel() {

    private val _nfcMessage = MutableLiveData<String>()
    val nfcMessage: LiveData<String> = _nfcMessage

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    fun onNfcSearchStarted() {
        _statusMessage.postValue("Bitte halten Sie ein NFC-Tag an das Gerät …")
    }

    fun onNfcTagRead(message: String) {
        _nfcMessage.postValue(message)
        _statusMessage.postValue("Tag erfolgreich gelesen ✅")
    }

    fun onNfcError() {
        _statusMessage.postValue("Fehler beim Lesen des NFC-Tags ❌")
    }
}
