package com.a7dev.bk.ui.banking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.a7dev.bk.MainActivity
import com.a7dev.bk.activities.BankingActivity
import com.a7dev.bk.databinding.FragmentLoginBinding
import com.a7dev.bk.databinding.FragmentProfileBinding
import com.a7dev.bk.utils.Resource
import com.a7dev.bk.viewmodel.LoginViewModel
import com.a7dev.bk.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileFragment: Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvLogout.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        lifecycleScope.launch {
            viewModel.user.collect {
                when(it) {
                    is Resource.Success -> {
                        binding.progressbarSettings.visibility = GONE
                        val name = "${it.data!!.firstName} ${it.data.lastName}"
                        binding.tvUserName.text = name
                    }
                    is Resource.Loading -> {
                        binding.progressbarSettings.visibility = VISIBLE
                    }
                    is Resource.Failure -> {
                        binding.progressbarSettings.visibility = GONE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}