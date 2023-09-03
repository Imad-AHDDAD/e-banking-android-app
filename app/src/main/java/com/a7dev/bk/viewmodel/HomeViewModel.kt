package com.a7dev.bk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a7dev.bk.data.Account
import com.a7dev.bk.data.Transaction
import com.a7dev.bk.data.User
import com.a7dev.bk.utils.Constants.ACCOUNT_COLLECTION
import com.a7dev.bk.utils.Constants.TRANSACTION_COLLECTION
import com.a7dev.bk.utils.Constants.USER_COLLECTION
import com.a7dev.bk.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
): ViewModel() {

    private val _account = MutableStateFlow<Resource<Account>>(Resource.Unspecified())
    val account = _account.asStateFlow()

    private val _transactions: MutableStateFlow<ArrayList<Transaction>?> = MutableStateFlow(null)
    val transactions : StateFlow<ArrayList<Transaction>?> = _transactions
    private var list : ArrayList<Transaction> = ArrayList()

    init {
        getAccount()
    }

    fun getTransactions(accountNumber: String) {
        db.collection(TRANSACTION_COLLECTION)
            .get()
            .addOnSuccessListener {
                val list : ArrayList<Transaction> = ArrayList()
                for (document in it) {
                    Log.d("TRANSACTION_FROM_DATABASE", "${document.id} ==> ${document.data}")
                    val transaction = document.toObject(Transaction::class.java)
                    if(accountNumber == transaction.accountToCreditNumber || accountNumber == transaction.accountToDebit.accountNumber.toString()){
                        list.add(transaction)
                    }
                }
                Log.d("TRANSACTION_FROM_DATABASE", "loop end")
                viewModelScope.launch {
                    _transactions.value = list
                }

            }
            .addOnFailureListener {
                Log.d("ACCOUNT_FROM_DATABASE", "error ${it.message.toString()}")
            }
    }

    fun getAccount() {
        viewModelScope.launch {
            _account.emit(Resource.Loading())
        }
        db.collection(ACCOUNT_COLLECTION)
            .document(firebaseAuth.uid!!)
            .addSnapshotListener { value, error ->
                if(error != null) {
                    viewModelScope.launch {
                        _account.emit(Resource.Failure(error.message.toString()))
                    }
                }else {
                    val account = value?.toObject(Account::class.java)
                    account?.let {
                        viewModelScope.launch {
                            _account.emit(Resource.Success(account))                        }
                    }
                }
            }
    }
}