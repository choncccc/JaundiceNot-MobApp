package com.example.jaundicednot

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class OnboardingFragment1: Fragment() {
    private lateinit var txtFAQ : TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.onboardingscreen1, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtFAQ = view.findViewById(R.id.txtFAQ)
        txtFAQ.setOnClickListener{
            val dialog = Dialog(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.dialog_faq, null)
            dialog.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.border)
            )
            dialog.setContentView(dialogView)
            val dialogClose = dialogView.findViewById<ImageButton>(R.id.closeIcon)

            dialogClose.setOnClickListener{
                dialog.dismiss()
            }

            val readMoreText = dialogView.findViewById<TextView>(R.id.TVReadMore)
            readMoreText.setOnClickListener {
                val url = "https://www.webmd.com/hepatitis/jaundice-why-happens-adults" // Replace with your actual link
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            dialog.show()
        }
    }
}