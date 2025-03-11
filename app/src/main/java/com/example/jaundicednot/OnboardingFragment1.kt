package com.example.jaundicednot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

        }
    }
}