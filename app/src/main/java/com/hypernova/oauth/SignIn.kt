package com.hypernova.oauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class SignIn : AppCompatActivity() {

    lateinit var register: RelativeLayout
    lateinit var google: ImageView
    lateinit var facebook: ImageView
    lateinit var twitter: ImageView
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var login: Button
    lateinit var forgotPassword: TextView

    // Firebase Authentication
    lateinit var mAuth: FirebaseAuth

    // Google Authentication
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var gso: GoogleSignInOptions

    // Facebook Authentication
    lateinit var callbackManager: CallbackManager

    // checks if user is already logged in
    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if(currentUser!=null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // assigning all views to variables
        register = findViewById(R.id.register)
        google = findViewById(R.id.google)
        facebook = findViewById(R.id.facebook)
        twitter = findViewById(R.id.twitter)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        forgotPassword = findViewById(R.id.forgotPassword)
        login = findViewById(R.id.login)

        // Sign Up Screen
        register.setOnClickListener {
            startActivity(Intent(this@SignIn, SignUp::class.java))
            finish()
        }

        // Initialize google authentication
        google.setOnClickListener{ signIn() }

        // Initialize Email Authentication
        login.setOnClickListener {
            val emailID: String = email.text.toString().trim()
            val pass: String = password.text.toString().trim()
            if(!fieldsValidation(emailID, pass)) return@setOnClickListener
            logIn(emailID, pass)
        }

        // Initialize Facebook Login
        callbackManager = CallbackManager.Factory.create()
        facebook.setOnClickListener{
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager, object: FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d("TAG", "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult!!.accessToken)
                }

                override fun onCancel() {
                    Log.d("TAG", "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d("TAG", "facebook:onError", error)
                }
            })
        }

    }

    // calls the facebook login page
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // Facebook-Firebase Integration
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("facebookLogin", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    startActivity(Intent(this@SignIn, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Email Sign In
    private fun logIn(emailId: String, password: String) {
        mAuth.signInWithEmailAndPassword(emailId, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@SignIn, "You are logged in", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignIn, MainActivity::class.java))
                finish()
            } else {
                Log.w("SignIn", "signInUserWithEmail:failure", task.exception)
                if (task.exception.toString().contains("The password is invalid"))
                    Toast.makeText(this@SignIn, "Wrong Password", Toast.LENGTH_SHORT).show()
                else if (task.exception.toString().contains("There is no user record corresponding to this identifier"))
                    Toast.makeText(this@SignIn, "User does not exist. Please create new account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Google OAuth
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signingInWithGoogle", "signInWithCredential:success")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("signingInWithGoogle", "signInWithCredential:failure", task.exception)
                }
            }
    }

    // get Activity result for email screen
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("signingInWithGoogle", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("signingInWithGoogle", "Google sign in failed", e)
            }
        }
    }

    // Displays the email selection Screen
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    // validates email and password field
    private fun fieldsValidation(emailId: String, pass: String): Boolean {
        val regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(emailId)

        // checks if email field is empty
        if (TextUtils.isEmpty(emailId)) {
            email.error = "Email Required"
            email.requestFocus()
            return false
        }

        // checks if valid email is entered
        if (!matcher.matches()) {
            email.error = "Please enter valid email"
            email.requestFocus()
            return false
        }

        // checks if password field is empty
        if (TextUtils.isEmpty(pass)) {
            password.error = "Please enter password"
            password.requestFocus()
            return false
        }
        return true
    }
}

