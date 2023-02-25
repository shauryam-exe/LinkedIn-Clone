package com.codewithshadow.linkedin_clone.ui.share_post

import com.codewithshadow.linkedin_clone.base.BaseActivity
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.storage.StorageReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.codewithshadow.linkedin_clone.utils.AppSharedPreferences
import com.codewithshadow.linkedin_clone.utils.LoadingDialog
import android.os.Bundle
import com.codewithshadow.linkedin_clone.R
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide
import android.text.TextWatcher
import android.text.Editable
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.tasks.OnCompleteListener
import android.content.Intent
import com.codewithshadow.linkedin_clone.ui.home.HomeActivity
import com.codewithshadow.linkedin_clone.ui.share_post.SharePostActivity
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.theartofdev.edmodo.cropper.CropImage
import android.widget.Toast
import com.codewithshadow.linkedin_clone.constants.Constants
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.UploadTask
import java.lang.Exception
import java.util.HashMap

class SharePostActivity() : BaseActivity() {
    lateinit var edit_text: EditText
    lateinit var post_img: ImageView
    lateinit var btn_select_img: ImageView
    lateinit var profileImg: ImageView
    lateinit var closeImg: ImageView
    lateinit var userName: TextView
    private var mImageUri: Uri? = null
    private lateinit var mStorageRef: StorageReference
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var btn_post: TextView
    lateinit var appSharedPreferences: AppSharedPreferences
    lateinit var loadingDialog: LoadingDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_post)
        appSharedPreferences = AppSharedPreferences(this)
        loadingDialog = LoadingDialog(this)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        mStorageRef = FirebaseStorage.getInstance().reference
        edit_text = findViewById(R.id.edit_text)
        post_img = findViewById(R.id.post_img)
        btn_select_img = findViewById(R.id.img3)
        btn_post = findViewById(R.id.btn_post)
        userName = findViewById(R.id.user_name)
        profileImg = findViewById(R.id.user_img)
        closeImg = findViewById(R.id.close_img)
        userName.setText(appSharedPreferences.userName)
        Glide.with(this).load(appSharedPreferences.imgUrl).into(profileImg)

        // Close Activity
        closeImg.setOnClickListener(View.OnClickListener { v: View? -> finish() })

        // Select Image
        btn_select_img.setOnClickListener(View.OnClickListener { v: View? -> openFileChooser() })
        edit_text.requestFocus()
        edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btn_post.setTextColor(Color.BLACK)
            }

            override fun afterTextChanged(s: Editable) {}
        })

        // Post Button
        btn_post.setOnClickListener(View.OnClickListener { v: View? ->
            if (mImageUri != null) {
                loadingDialog.startLoadingDialog()
                uploadFile(mImageUri)
            } else {
                if (!edit_text.text.toString().isEmpty()) loadingDialog.startLoadingDialog()
                uploadData(edit_text.text.toString())
            }
        })
    }

    private fun uploadData(toString: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("AllPosts")
        val key = ref.push().key
        val map = HashMap<String, Any?>()
        map["description"] = toString
        map["imgUrl"] = ""
        map["username"] = appSharedPreferences.userName
        map["user_profile"] = appSharedPreferences.imgUrl
        map["key"] = key
        Log.d("Share Post","uploading")
        ref.child((key)!!).child(Constants.INFO).setValue(map).addOnCompleteListener {
            loadingDialog.dismissDialog()
            val intent = Intent(this@SharePostActivity, HomeActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //makesure user cant go back
            startActivity(intent)
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == PICK_IMAGE_REQUEST) && (resultCode == RESULT_OK
                    ) && (data != null)
        ) {
            mImageUri = data.data!!
            CropImage.activity(mImageUri)
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                mImageUri = result.uri
                Glide.with(this).load(mImageUri)
                    .into(post_img)
                btn_post.setTextColor(Color.BLACK)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this@SharePostActivity, "" + error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    //-------------------------------Upload User Image-------------------------------//
    private fun uploadFile(mImageUri: Uri?) {
        if (mImageUri != null) {
            val reference =
                mStorageRef.child(user.uid).child("Files/" + System.currentTimeMillis())
            reference.putFile(mImageUri)
                .addOnSuccessListener {
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("Share Post","$uri")

                        val ref = FirebaseDatabase.getInstance().reference.child("AllPosts")
                        val key = ref.push().key
                        val map = HashMap<String, Any?>()
                        val imageUrl = uri.toString()
                        map["imgUrl"] = imageUrl
                        map["description"] = edit_text.text.toString()
                        map["username"] = appSharedPreferences.userName
                        map["user_profile"] = appSharedPreferences.imgUrl
                        map["key"] = key


                        ref.child((key)!!).child(Constants.INFO).setValue(map)
                            .addOnCompleteListener {
                                loadingDialog.dismissDialog()
                                val intent = Intent(
                                    this@SharePostActivity,
                                    HomeActivity::class.java
                                )
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //makesure user cant go back
                                startActivity(intent)
                            }
                    }
                }
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        Toast.makeText(this@SharePostActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    companion object {
        private val PICK_IMAGE_REQUEST = 1
    }
}