package com.a7dev.bk.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.a7dev.bk.R
import com.a7dev.bk.databinding.ActivityBankingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BankingActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityBankingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController = findNavController(R.id.fragment)
        binding.bottomNavigation.setupWithNavController(navController)

    }
}