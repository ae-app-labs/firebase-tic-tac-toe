package com.ae.apps.tictactoe

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    // Create a member variable to store FirebaseAuth
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set the view now
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()

        // if user logged in, go to choose player screen
        if(firebaseAuth!!.currentUser != null){
            startActivity(Intent(this,ChoosePlayerActivity::class.java))
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.GONE
    }

    fun loginButtonClicked(view: View) {
        if (TextUtils.isEmpty(email.text.toString())) {
            Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(password.text.toString())) {
            Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        //  use firebase to authenticate user
        firebaseAuth!!.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener {
                    progressBar.visibility = View.GONE
                    if(!it.isSuccessful){
                        if( password.text.toString().length < 6 ){
                            password.error =getString(R.string.minimum_password)
                        } else {
                            Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        startActivity( Intent(this, ChoosePlayerActivity::class.java))
                        finish()
                    }
                }
    }
}
