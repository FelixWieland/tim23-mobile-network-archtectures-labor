package com.example.mobilenetworkarchitecture.ui.exercise2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobilenetworkarchitecture.databinding.FragmentExercise2Binding

class Exercise2Fragment : Fragment() {

    private var _binding: FragmentExercise2Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val exercise2ViewModel =
            ViewModelProvider(this).get(Exercise2ViewModel::class.java)

        _binding = FragmentExercise2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textExercise2
        exercise2ViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

