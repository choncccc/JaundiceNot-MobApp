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

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        if (hasCompletedOnboarding()) {
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
    private fun hasCompletedOnboarding(): Boolean {
        return sharedPreferences.getBoolean("onboarding_completed", false)
    }

    private fun markOnboardingCompleted() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("onboarding_completed", true)
        editor.apply()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
