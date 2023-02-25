package com.codewithshadow.linkedin_clone.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class UserModel(
    var username: String? = null,
    val emailAddress: String? = null,
    val imageUrl: String? = null,
    val key: String? = null,
    val token: String? = null,
    val headline: String? = null
) : Parcelable {
}