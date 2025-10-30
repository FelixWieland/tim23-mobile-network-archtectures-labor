package com.example.mobilenetworkarchitecture.ui.exercise2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Exercise2ViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Exercise 2 Fragment"
    }
    val text: LiveData<String> = _text
}

