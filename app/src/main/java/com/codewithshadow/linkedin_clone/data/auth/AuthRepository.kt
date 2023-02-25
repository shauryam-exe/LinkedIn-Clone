package com.codewithshadow.linkedin_clone.data.auth

import com.codewithshadow.linkedin_clone.data.model.State
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class AuthRepository {

    val auth = FirebaseAuth.getInstance()

    var currentUser: String? = null

        suspend fun googleSignIn(credential: AuthCredential): State {
        return try {
            val result = auth.signInWithCredential(credential).await()
            currentUser = auth.currentUser!!.uid
            State.Success
        } catch (e: Exception) {
            State.Failure(e)
        }
    }


}




suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            if (it.exception != null) {
                cont.resumeWithException(it.exception!!)
            } else {
                cont.resume(it.result, null)
            }
        }
    }
}
