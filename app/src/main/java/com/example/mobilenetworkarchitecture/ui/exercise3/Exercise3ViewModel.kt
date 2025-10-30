package com.example.mobilenetworkarchitecture.ui.exercise3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Exercise3ViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Exercise 3 Fragment"
    }
    val text: LiveData<String> = _text
}

