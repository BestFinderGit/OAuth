package com.hypernova.oauth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Matcher
import java.util.regex.Pattern


class SignUp : AppCompatActivity() {

    lateinit var name: EditText
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var signUp: Button
    lateinit var signIn: RelativeLayout

    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        signUp = findViewById(R.id.register)
        signIn = findViewById(R.id.signIn)

        signIn.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }

        signUp.setOnClickListener {
            val Name = name.text.toString().trim()
            val Email = email.text.toString().trim()
            val Pass = password.text.toString().trim()

            if(!fieldsValidation(Name, Email, Pass)) return@setOnClickListener
            register(Email, Pass)
        }

    }

    private fun register(emailId: String, pass: String) {
        mAuth.createUserWithEmailAndPassword(emailId, pass).addOnCompleteListener(this) { task: Task<AuthResult?> ->
            if (task.isSuccessful)
                Toast.makeText(this@SignUp, "Account Created Successfully", Toast.LENGTH_SHORT).show()
            else {
                Log.w("SignUp", "createUserWithEmail:failure", task.exception)
                if (task.exception.toString().contains("The email address is already in use by another account"))
                    Toast.makeText(this@SignUp, "User already exists please log in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // validates email and password field
    private fun fieldsValidation(Name: String, emailId: String, pass: String): Boolean {
        val regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(emailId)

        if(TextUtils.isEmpty(Name)) {
            name.error = "Name required"
            name.requestFocus()
            return false
        }

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