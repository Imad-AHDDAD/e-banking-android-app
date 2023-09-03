package com.a7dev.bk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a7dev.bk.data.User
import com.a7dev.bk.utils.Constants.USER_COLLECTION
import com.a7dev.bk.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
): ViewModel() {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    init {
        getUser()
    }

    fun getUser() {
        viewModelScope.launch {
            _user.emit(Resource.Loading())
        }
        db.collection(USER_COLLECTION)
            .document(firebaseAuth.uid!!)
            .addSnapshotListener { value, error ->
                if(error != null) {
                    viewModelScope.launch {
                        _user.emit(Resource.Failure(error.message.toString()))
                    }
                }else {
                    val user = value?.toObject(User::class.java)
                    user?.let {
                        viewModelScope.launch {
                            _user.emit(Resource.Success(user))                        }
                    }
                }
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }


}