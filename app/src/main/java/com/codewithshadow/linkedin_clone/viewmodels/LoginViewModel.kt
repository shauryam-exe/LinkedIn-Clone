package com.codewithshadow.linkedin_clone.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithshadow.linkedin_clone.data.auth.AuthRepository
import com.codewithshadow.linkedin_clone.data.model.State
import com.codewithshadow.linkedin_clone.models.UserModel
import com.codewithshadow.linkedin_clone.data.repository.DatabaseRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val authRepository = AuthRepository()
    val databaseRepository = DatabaseRepository()


    val authState = MutableLiveData<State>()

    fun googleSignIn(account: GoogleSignInAccount) = viewModelScope.launch {
        authState.postValue(State.Loading)
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val result = authRepository.googleSignIn(credential)
        authState.postValue(result)

        if (result == State.Success) {
            val email = account.email
            val userName = account.displayName
            var imageUrl = account.photoUrl.toString()
            imageUrl = imageUrl.substring(0, imageUrl.length - 5) + "s400-c"

            val user = UserModel()

            user.username = userName
            user.emailAddress = email
            user.imageUrl = imageUrl
            user.key = authRepository.currentUser

            databaseRepository.createUser(user)
        }
    }

}