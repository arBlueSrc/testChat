package com.applica.testchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.applica.testchat.databinding.ActivityMainBinding
import com.applica.testchat.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class SignInActivity : AppCompatActivity() {

    private val TAG = "SignInActivity"
    private val RC_SIGN_IN = 9001

    private lateinit var mBinding: ActivitySignInBinding
    private lateinit var mSignInClient: GoogleSignInClient
    private var gso : GoogleSignInOptions? = null


    // Firebase instance variables
    private var mFirebaseAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance()

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mSignInClient = GoogleSignIn.getClient(this, gso!!)

        mBinding.signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent in signIn()
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mFirebaseAuth?.signInWithCredential(credential)
            ?.addOnSuccessListener(
                this,
                OnSuccessListener<AuthResult?> { // If sign in succeeds the auth state listener will be notified and logic to
                    // handle the signed in user can be handled in the listener.
                    Log.d(TAG, "signInWithCredential:success")
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    finish()
                })
            ?.addOnFailureListener(
                this,
                OnFailureListener { e -> // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential", e)
                    Toast.makeText(
                        this@SignInActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                })
    }
}