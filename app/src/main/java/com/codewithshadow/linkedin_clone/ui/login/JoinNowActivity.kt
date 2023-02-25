package com.codewithshadow.linkedin_clone.ui.login

import android.content.Intent
import com.codewithshadow.linkedin_clone.base.BaseActivity
import android.widget.RelativeLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.codewithshadow.linkedin_clone.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.codewithshadow.linkedin_clone.data.model.State
import com.codewithshadow.linkedin_clone.ui.home.HomeActivity
import com.codewithshadow.linkedin_clone.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class JoinNowActivity : BaseActivity() {
    lateinit var googleBtn: RelativeLayout
    var auth: FirebaseAuth? = null
    var client: GoogleSignInClient? = null
    lateinit var firestore: FirebaseFirestore
    lateinit var viewModel: LoginViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_now)
        progressBar = findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        googleBtn = findViewById(R.id.card_google_btn)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        initGoogleSignIn()
        initLoading()
    }

    private fun initLoading() {
        viewModel.authState.observe(this) {
            when(it) {
                is State.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is State.Success -> {
                    progressBar.visibility = View.GONE
                    startActivity(Intent(this@JoinNowActivity,HomeActivity::class.java))
                }
                is State.Failure -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@JoinNowActivity, "Error Login", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        client = GoogleSignIn.getClient(this, gso)

        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleResult(task)
                }
            }

        googleBtn.setOnClickListener {
            val intent = client!!.signInIntent
            resultLauncher.launch(intent)
            client!!.signOut()
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                viewModel.googleSignIn(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}