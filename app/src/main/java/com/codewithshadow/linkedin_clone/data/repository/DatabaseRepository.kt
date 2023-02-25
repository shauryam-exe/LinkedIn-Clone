package com.codewithshadow.linkedin_clone.data.repository

import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.codewithshadow.linkedin_clone.models.UserModel
import com.codewithshadow.linkedin_clone.ui.location.LocationActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


class DatabaseRepository {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

    fun createUser(user: UserModel) {

        databaseReference.child(auth.currentUser!!.uid).child("Info").setValue(user)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    Log.d("User Creating","User Created Successfully")
                else if (it.isComplete)
                    Log.d("User Creating","User Creation Failed : ${it.exception}")
            }
    }

}