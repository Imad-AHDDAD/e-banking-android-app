package com.a7dev.bk.ui.banking

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.a7dev.bk.adpaters.TransactionsAdapter
import com.a7dev.bk.data.Account
import com.a7dev.bk.data.Transaction
import com.a7dev.bk.databinding.FragmentTransactionBinding
import com.a7dev.bk.utils.Resource
import com.a7dev.bk.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TransactionFragment: Fragment()  {

    private lateinit var binding: FragmentTransactionBinding
    private val viewModel: TransactionViewModel by activityViewModels()
    private lateinit var account: Account

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.account.collect {
                when(it) {
                    is Resource.Success -> {
                        binding.progressbarSettings.visibility = View.GONE
                        val status = "${it.data!!.user.firstName} ${it.data.user.lastName} | ${it.data.balance} DH"
                        binding.status.text = status
                        account = it.data
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
        lifecycleScope.launch {
            viewModel.transactionFlow.collect {
                when(it) {
                    is Resource.Success -> {
                        binding.send.revertAnimation()
                        unfillFields()
                        Log.d("transaction", it.message.toString())
                        Toast.makeText(requireContext(), "Transaction has been successfully completed", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        binding.send.startAnimation()
                    }
                    is Resource.Failure -> {
                        binding.send.revertAnimation()
                        Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                        Log.d("transaction", it.message.toString())
                    }
                    else -> Unit
                }
            }
        }
        binding.send.setOnClickListener {
            val accountToCredit = binding.accountNumber.text.toString().trim()
            val amount = binding.amount.text.toString().trim()
            val motif = binding.motif.text.toString().trim()
            if(accountToCredit.isEmpty()){
                binding.accountNumber.error = "invalid account number"
            } else if(amount.isEmpty()) {
                binding.amount.error = "invalid amount"
            } else if(motif.isEmpty()) {
                binding.motif.error = "invalid motif"
            }else if(account.balance < amount.toFloat()) {
                Toast.makeText(requireContext(), "Failed: Balance insufficient", Toast.LENGTH_SHORT).show()
            } else {
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val date = currentDateTime.format(formatter)
                val transaction = Transaction(account, accountToCredit, amount, motif, date.toString())
                viewModel.saveTransaction(transaction)
            }
        }
    }

    private fun unfillFields() {
        binding.apply {
            accountNumber.text.clear()
            amount.text.clear()
            motif.text.clear()
        }
    }
}