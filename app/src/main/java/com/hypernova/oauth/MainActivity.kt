package com.hypernova.oauth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var name: TextView
    private lateinit var logout: Button
    private lateinit var pfp: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user = FirebaseAuth.getInstance().currentUser

        name = findViewById(R.id.name)
        logout = findViewById(R.id.logout)
        pfp = findViewById(R.id.pfp)

        // setting user data
        for (profile in user!!.providerData) {
            val photoUrl: Uri? = profile.photoUrl
            // Increasing Image quality
            val originalPieceOfUrl = "s96-c"
            val newPieceOfUrlToAdd = "s600-c"

            if (photoUrl != null) {
                val photoPath: String = photoUrl.toString()
                var newString = " "
                if (profile.providerId == "google.com") {
                    newString = photoPath.replace(originalPieceOfUrl, newPieceOfUrlToAdd)
                } else if (profile.providerId == "facebook.com") {
                    newString = "$photoPath?height=500"
                }
                Log.i("TAG", newString)
                Glide.with(this).load(newString).into(pfp)
            }
            name.text = user.displayName
        }

        // Logout
        logout.setOnClickListener {
            disconnectFromFacebook()
            FirebaseAuth.getInstance().signOut()
            val gso: GoogleSignInOptions = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(this,gso)
            googleSignInClient.signOut()
            startActivity(Intent(this@MainActivity, SignIn::class.java))
            finish()
        }
    }

    // Logout of facebook
    private fun disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            Log.i("disconnectFromFacebook", "alreadyLoggedOut")
            return  /* already logged out */
        }
        GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE){
            LoginManager.getInstance().logOut()
        }.executeAsync()
    }



//        name.text = user?.displayName
//        Glide.with(this).load(user?.photoUrl).into(pfp)
//        val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
//        if(googleSignInAccount!=null) {
//            name.text =  googleSignInAccount.displayName
//            Glide.with(this).load(googleSignInAccount.photoUrl).into(pfp)
//        }

//    @SuppressLint("SetTextI18n")
//    private fun getFbInfo() { val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()) { `object`, response ->
//            try {
//                Log.d("LOG_TAG", "fb json object: $`object`")
//                Log.d("LOG_TAG", "fb graph response: $response")
//                val id = `object`.getString("id")
//                val first_name = `object`.getString("first_name")
//                val last_name = `object`.getString("last_name")
//                val gender = `object`.getString("gender")
//                val birthday = `object`.getString("birthday")
//                val image_url = "http://graph.facebook.com/$id/picture?type=large"
//                val email: String
//                if (`object`.has("email")) {
//                    email = `object`.getString("email")
//                }
//                Glide.with(this).load(image_url).into(pfp)
//                name.text = "$first_name $last_name"
//
//            } catch (e: JSONException) {
//                Log.d("requestfailed", e.message.toString())
//                e.printStackTrace()
//            }
//        }
//        val parameters = Bundle()
//        parameters.putString(
//            "fields",
//            "id,first_name,last_name,email,gender,birthday"
//        ) // id,first_name,last_name,email,gender,birthday,cover,picture.type(large)
//        request.parameters = parameters
//        request.executeAsync()
//    }
}