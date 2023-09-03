package com.a7dev.bk.adpaters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a7dev.bk.R
import com.a7dev.bk.data.Account
import com.a7dev.bk.data.Transaction
import com.a7dev.bk.databinding.ListItemBinding
import java.util.*
import kotlin.collections.ArrayList

class TransactionsAdapter(private val context: Context, private val currentAccountNumber: String) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder>() {

    private var transactions = ArrayList<Transaction>()
    fun fillList(list: ArrayList<Transaction>) {
        transactions.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return TransactionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.binding.idMotif.text = transaction.motif.uppercase()
        holder.binding.idDate.text = transaction.date
        Log.d("FROM_ADAPTER", "account to debit = ${transaction.accountToDebit.accountNumber}")
        Log.d("FROM_ADAPTER", "account to credit = ${transaction.accountToCreditNumber}")
        Log.d("FROM_ADAPTER", "current account = $currentAccountNumber")
        if(transaction.accountToDebit.accountNumber.toString() == currentAccountNumber){
            holder.binding.idAmount.text = "- ${transaction.amount} DH"
        } else {
            holder.binding.idAmount.text = "+ ${transaction.amount} DH"
        }
    }

    inner class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ListItemBinding = ListItemBinding.bind(itemView)
    }
}