package com.example.jaundicednot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingAdapter
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val hasCompletedOnboarding = preferences.getBoolean("has_completed_onboarding", false)

        if (hasCompletedOnboarding) {
            navigateToMainActivity()
            return
        }

        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        val dotsIndicator = findViewById<WormDotsIndicator>(R.id.dotsIndicator)

        adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        dotsIndicator.attachTo(viewPager)

        btnNext.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem += 1
            } else {
                markOnboardingCompleted()
                navigateToMainActivity()
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == adapter.itemCount - 1) {
                    btnNext.text = "Finish"
                } else {
                    btnNext.text = "Next"
                }
            }
        })
    }

    private fun markOnboardingCompleted() {
        val preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("has_completed_onboarding", true)
        editor.apply()
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
