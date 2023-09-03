package com.a7dev.bk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a7dev.bk.data.Account
import com.a7dev.bk.data.Transaction
import com.a7dev.bk.data.User
import com.a7dev.bk.utils.Constants
import com.a7dev.bk.utils.Constants.ACCOUNT_COLLECTION
import com.a7dev.bk.utils.Constants.TRANSACTION_COLLECTION
import com.a7dev.bk.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private var found: Boolean = false
    private val _account = MutableStateFlow<Resource<Account>>(Resource.Unspecified())
    val account = _account.asStateFlow()

    private val _transactionFlow = MutableStateFlow<Resource<Transaction>>(Resource.Unspecified())
    val transactionFlow: Flow<Resource<Transaction>> = _transactionFlow

    init {
        found = false
        viewModelScope.launch {
            _transactionFlow.emit(Resource.Unspecified())
        }
        getAccount()
    }

    private fun getAccount() {
        viewModelScope.launch {
            _account.emit(Resource.Loading())
        }
        db.collection(Constants.ACCOUNT_COLLECTION)
            .document(firebaseAuth.uid!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch {
                        _account.emit(Resource.Failure(error.message.toString()))
                    }
                } else {
                    val account = value?.toObject(Account::class.java)
                    account?.let {
                        viewModelScope.launch {
                            _account.emit(Resource.Success(account))
                        }
                    }
                }
            }
    }

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _transactionFlow.emit(Resource.Loading())
        }

        db.collection(ACCOUNT_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("ACCOUNT_FROM_DATABASE", "${document.id} => ${document.data}")
                    if(document.toObject(Account::class.java).accountNumber == transaction.accountToCreditNumber.toLong()) {
                        // 9431429695
                        found = true
                        Log.d("ACCOUNT_FROM_DATABASE", "Account found !")
                        updateAccounts(
                            transaction.accountToDebit,
                            document.id,
                            document.toObject(Account::class.java),
                            transaction
                        )
                    }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _transactionFlow.emit(Resource.Failure("error getting documents"))
                }
            }
    }

    private fun updateAccounts(
        currentAccount: Account,
        otherAccountDocumentId: String,
        otherAccount: Account,
        transaction: Transaction
    ) {

        db.collection(ACCOUNT_COLLECTION)
            .document(firebaseAuth.uid!!)
            .update("balance", currentAccount.balance - transaction.amount.toFloat())
            .addOnSuccessListener {
                db.collection(ACCOUNT_COLLECTION)
                    .document(otherAccountDocumentId)
                    .update("balance", otherAccount.balance + transaction.amount.toFloat())
                    .addOnSuccessListener {
                        db.collection(TRANSACTION_COLLECTION)
                            .document(firebaseAuth.uid!!)
                            .set(transaction)
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    _transactionFlow.emit(Resource.Success(transaction))
                                }
                            }.addOnFailureListener {
                                viewModelScope.launch {
                                    _transactionFlow.emit(Resource.Failure(it.message.toString()))
                                }
                            }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            _transactionFlow.emit(Resource.Failure(it.message.toString()))
                        }
                    }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _transactionFlow.emit(Resource.Failure(it.message.toString()))
                }
            }
    }

}