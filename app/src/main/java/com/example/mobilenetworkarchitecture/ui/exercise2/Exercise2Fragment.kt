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
    private val binding get() = _binding!!
    private lateinit var exercise2ViewModel: Exercise2ViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        exercise2ViewModel =
            ViewModelProvider(requireActivity()).get(Exercise2ViewModel::class.java)

        _binding = FragmentExercise2Binding.inflate(inflater, container, false)

        // Beobachte Text (Tag oder Status)
        exercise2ViewModel.nfcMessage.observe(viewLifecycleOwner) { msg ->
            binding.textExercise2.text = msg
        }

        exercise2ViewModel.statusMessage.observe(viewLifecycleOwner) { status ->
            binding.textExercise2.text = status
        }

        // Startstatus
        exercise2ViewModel.onNfcSearchStarted()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

