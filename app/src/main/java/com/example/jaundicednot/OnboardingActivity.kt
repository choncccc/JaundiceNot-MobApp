package com.example.jaundicednot

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator


class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingAdapter
    private lateinit var btnNext: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if onboarding has been completed before
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        if (hasCompletedOnboarding()) {
            navigateToMainActivity()  // Skip onboarding if completed
            return
        }

        setContentView(R.layout.activity_onboarding)

        // Initialize Views
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        val dotsIndicator = findViewById<WormDotsIndicator>(R.id.dotsIndicator)

        // Setup ViewPager2 with Adapter
        adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        // Attach dotsIndicator to ViewPager2
        dotsIndicator.attachTo(viewPager)

        // Handle Next Button Click
        btnNext.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem += 1  // Move to next page
            } else {
                // Last page - Mark onboarding as completed and navigate
                markOnboardingCompleted()
                navigateToMainActivity()  // Navigate to the Main Activity
            }
        }

        // Update the button text based on the current page
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == adapter.itemCount - 1) {
                    btnNext.text = "Finish"  // Change text when on the last page
                } else {
                    btnNext.text = "Next"  // Default text for other pages
                }
            }
        })
    }

    // Check if the user has already completed onboarding
    private fun hasCompletedOnboarding(): Boolean {
        return sharedPreferences.getBoolean("onboarding_completed", false)
    }

    // Mark the onboarding as completed
    private fun markOnboardingCompleted() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("onboarding_completed", true)
        editor.apply()
    }

    // Function to navigate to the MainActivity
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Close the onboarding activity to prevent going back
    }
}
