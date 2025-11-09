package com.example.mobilenetworkarchitecture.ui.exercise3optional

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobilenetworkarchitecture.R
import com.example.mobilenetworkarchitecture.chat.ChatMessage
import com.example.mobilenetworkarchitecture.databinding.FragmentExercise3optionalBinding
import com.google.android.material.card.MaterialCardView

class Exercise3OptionalFragment : Fragment() {

    private var _binding: FragmentExercise3optionalBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: Exercise3OptionalViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[Exercise3OptionalViewModel::class.java]

        _binding = FragmentExercise3optionalBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        setupUI()
        observeViewModel()
        
        return root
    }
    
    private fun setupUI() {
        // Setup send button
        binding.fabSend.setOnClickListener {
            sendMessage()
        }
        
        // Setup enter key on keyboard
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
        
        // Initial welcome message
        showWelcomeMessage()
    }
    
    private fun observeViewModel() {
        // Observe messages
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            renderMessages(messages)
        }
        
        // Observe errors
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        
        // Observe chat status
        viewModel.isChatActive.observe(viewLifecycleOwner) { isActive ->
            binding.fabSend.isEnabled = isActive
            binding.etMessage.isEnabled = isActive
            
            if (!isActive) {
                binding.etMessage.hint = "Chat is not active"
            }
        }
    }
    
    private fun sendMessage() {
        val content = binding.etMessage.text.toString()
        if (content.isNotBlank()) {
            viewModel.sendMessage(content)
            binding.etMessage.text?.clear()
        }
    }
    
    private fun showWelcomeMessage() {
        binding.textExercise3optional.text = buildString {
            append("Welcome to WLAN-Chat!\n\n")
            append("Your username: ${viewModel.getUsername()}\n\n")
            append("Messages will appear below as they are sent and received.\n")
            append("All devices on the same WiFi network can participate in this chat.")
        }
    }
    
    private fun renderMessages(messages: List<ChatMessage>) {
        binding.containerMessages.removeAllViews()
        
        if (messages.isEmpty()) {
            // Show empty state
            val emptyView = TextView(requireContext()).apply {
                text = "No messages yet. Start the conversation!"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(16, 32, 16, 32)
            }
            binding.containerMessages.addView(emptyView)
        } else {
            messages.forEach { message ->
                addMessageView(message)
            }
            
            // Auto-scroll to bottom after rendering
            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }
    
    private fun addMessageView(message: ChatMessage) {
        val isOwnMessage = message.sender == viewModel.getUsername()
        
        // Create message card
        val messageCard = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    if (isOwnMessage) 48 else 0,
                    0,
                    if (isOwnMessage) 0 else 48,
                    12
                )
            }
            radius = 16f
            cardElevation = 1f
            setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isOwnMessage) R.color.primary_blue_light else R.color.card_background
                )
            )
        }
        
        // Create message content layout
        val contentLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
        }
        
        // Sender name (only for other users' messages)
        if (!isOwnMessage) {
            val senderView = TextView(requireContext()).apply {
                text = message.sender
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 4)
            }
            contentLayout.addView(senderView)
        }
        
        // Message content
        val contentView = TextView(requireContext()).apply {
            text = message.content
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            textSize = 15f
            setPadding(0, 0, 0, 4)
        }
        contentLayout.addView(contentView)
        
        // Timestamp
        val timestampView = TextView(requireContext()).apply {
            text = message.getFormattedTime()
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 11f
            gravity = if (isOwnMessage) Gravity.END else Gravity.START
        }
        contentLayout.addView(timestampView)
        
        messageCard.addView(contentLayout)
        binding.containerMessages.addView(messageCard)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
