package com.a7dev.bk.ui.banking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.a7dev.bk.MainActivity
import com.a7dev.bk.adpaters.TransactionsAdapter
import com.a7dev.bk.databinding.FragmentHomeBinding
import com.a7dev.bk.databinding.FragmentProfileBinding
import com.a7dev.bk.utils.Resource
import com.a7dev.bk.viewmodel.HomeViewModel
import com.a7dev.bk.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class HomeFragment: Fragment()  {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.transactions.collect {
                it?.let {
                    adapter.fillList(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.account.collect {
                when(it) {
                    is Resource.Success -> {
                        binding.progressbarSettings.visibility = View.GONE
                        val name = "${it.data!!.user.firstName} ${it.data.user.lastName}"
                        val balance = "${it.data.balance} DH"
                        binding.tvName.text = name
                        binding.tvAccount.text = it.data.accountNumber.toString()
                        binding.tvBalance.text = balance
                        adapter = TransactionsAdapter(requireContext(), it.data.accountNumber.toString())
                        val layoutManager = LinearLayoutManager(requireContext())
                        binding.idTransactions.layoutManager = layoutManager
                        binding.idTransactions.adapter = adapter
                        viewModel.getTransactions(it.data.accountNumber.toString())

                    }
                    is Resource.Loading -> {
                        binding.progressbarSettings.visibility = View.VISIBLE
                    }
                    is Resource.Failure -> {
                        binding.progressbarSettings.visibility = View.GONE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

    }

}