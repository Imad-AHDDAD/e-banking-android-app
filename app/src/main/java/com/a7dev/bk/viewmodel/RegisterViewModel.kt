package com.a7dev.bk.viewmodel

import androidx.lifecycle.ViewModel
import com.a7dev.bk.data.Account
import com.a7dev.bk.data.User
import com.a7dev.bk.utils.*
import com.a7dev.bk.utils.Constants.ACCOUNT_COLLECTION
import com.a7dev.bk.utils.Constants.LOWER_BOUND
import com.a7dev.bk.utils.Constants.UPPER_BOUND
import com.a7dev.bk.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _register = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> = _register

    private val _validation = Channel<RegisterFieldState>()
    val validation = _validation.receiveAsFlow()

    fun createAccountWithEmailAndPassword(user: User, password: String) {
        if (checkValidation(user, password)) {
            runBlocking {
                _register.emit(Resource.Loading())
            }
            firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener {
                    it.user?.let {
                        saveUserInfo(it.uid, user)
                    }
                }
                .addOnFailureListener {
                    _register.value = Resource.Failure(it.message.toString())
                }
        }else {
            val registerFieldState = RegisterFieldState(
                validateEmail(user.email),
                validatePassword(password)
            )
            runBlocking {
                _validation.send(registerFieldState)
            }
        }
    }

    private fun saveUserInfo(userUid: String, user: User) {
        db.collection(USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                val accountNumber = Random.nextLong(LOWER_BOUND, UPPER_BOUND + 1)
                val account = Account(accountNumber, user, 5000f)
                db.collection(ACCOUNT_COLLECTION)
                    .document(userUid)
                    .set(account)
                    .addOnSuccessListener {
                        _register.value = Resource.Success(user)
                    }.addOnFailureListener {
                        _register.value = Resource.Failure(it.message.toString())
                    }
            }
            .addOnFailureListener {
                _register.value = Resource.Failure(it.message.toString())
            }
    }

    private fun checkValidation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)

        return (emailValidation is RegisterValidation.Success
                && passwordValidation is RegisterValidation.Success)
    }

}