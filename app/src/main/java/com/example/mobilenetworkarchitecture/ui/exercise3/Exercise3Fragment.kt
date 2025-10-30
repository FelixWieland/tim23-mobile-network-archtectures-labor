package com.example.mobilenetworkarchitecture.ui.exercise3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobilenetworkarchitecture.databinding.FragmentExercise3Binding

class Exercise3Fragment : Fragment() {

    private var _binding: FragmentExercise3Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val exercise3ViewModel =
            ViewModelProvider(this).get(Exercise3ViewModel::class.java)

        _binding = FragmentExercise3Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textExercise3
        exercise3ViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

