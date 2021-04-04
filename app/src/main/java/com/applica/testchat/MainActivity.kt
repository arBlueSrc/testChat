package com.applica.testchat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.applica.testchat.databinding.ActivityMainBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    val MESSAGES_CHILD = "messages"
    val ANONYMOUS = "anonymous"
    val DEFAULT_MSG_LENGTH_LIMIT = 10

    private val REQUEST_INVITE = 1
    private val REQUEST_IMAGE = 2
    private val MESSAGE_URL = "http://friendlychat.firebase.google.com/message/"
    private val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    private val MESSAGE_SENT_EVENT = "message_sent"

    private val mSharedPreferences: SharedPreferences? = null
    private val mSignInClient: GoogleSignInClient? = null

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    // Firebase instance variables
    private var mDatabase: FirebaseDatabase? = null
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>

    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Initialize Firebase Auth and check if the user is signed in
        mFirebaseAuth = FirebaseAuth.getInstance()
        if (mFirebaseAuth!!.getCurrentUser() == null) {
            // Not signed in, launch the Sign In activity
            Log.i(TAG, "onCreate: went to sign in activity")
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        getMessage()

    }

    private fun getMessage() {
        // Initialize Realtime Database
        // Initialize Realtime Database
        mDatabase = FirebaseDatabase.getInstance()
        val messagesRef = mDatabase!!.getReference().child(MESSAGES_CHILD)

        // The FirebaseRecyclerAdapter class comes from the FirebaseUI library
        // See: https://github.com/firebase/FirebaseUI-Android
        val options = FirebaseRecyclerOptions.Builder<FriendlyMessage>()
            .setQuery(messagesRef, FriendlyMessage::class.java)
            .build()

        mFirebaseAdapter =
            object : FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {
                override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MessageViewHolder {
                    val inflater = LayoutInflater.from(viewGroup.context)
                    return MessageViewHolder(
                        inflater.inflate(
                            R.layout.item_message,
                            viewGroup,
                            false
                        )
                    )
                }

                override fun onBindViewHolder(
                    holder: MessageViewHolder,
                    position: Int,
                    model: FriendlyMessage
                ) {
                    mBinding.progressBar.visibility = ProgressBar.INVISIBLE
                    holder.bindMessage(model)
                }
            }

        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager.stackFromEnd = true
        mBinding.messageRecyclerView.layoutManager = mLinearLayoutManager
        mBinding.messageRecyclerView.setAdapter(mFirebaseAdapter)

        // See MyScrollToBottomObserver.java for details
        // See MyScrollToBottomObserver.java for details
        mFirebaseAdapter.registerAdapterDataObserver(
            MyScrollToBottomObserver(
                mBinding.messageRecyclerView,
                mFirebaseAdapter,
                mLinearLayoutManager
            )
        )

    }

    @Nullable
    private fun getUserPhotoUrl(): String? {
        val user = mFirebaseAuth!!.currentUser
        return if (user != null && user.photoUrl != null) {
            user.photoUrl.toString()
        } else null
    }

    private fun getUserName(): String? {
        val user = mFirebaseAuth!!.currentUser
        return if (user != null) {
            user.displayName
        } else ANONYMOUS
    }

    private fun signOut() {
        mFirebaseAuth!!.signOut()
        mSignInClient?.signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    override fun onPause() {
        mFirebaseAdapter.stopListening()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAdapter.startListening()
    }
}