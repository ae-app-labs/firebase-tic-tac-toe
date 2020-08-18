package com.ae.apps.tictactoe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    // create member variables for FirebaseAuth, DatabaseReference, and FirebaseDatabase
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseDatabase: DatabaseReference? = null
    private var firebaseDatabaseInstance : FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // FirebaseApp.initializeApp(this)

        // Initialize firebase member variables, get Firebase instances
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabaseInstance = FirebaseDatabase.getInstance()

        // if already logged in go to choose player screen
        if(firebaseAuth!!.currentUser != null){
            startActivity(Intent(this, ChoosePlayerActivity::class.java))
            finish()
        }
    }

    fun onRegisterClicked(view: View) {

        if (TextUtils.isEmpty(email.text.toString())) {
            Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(password.text.toString())) {
            Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(username.text.toString())) {
            Toast.makeText(applicationContext, "Enter username!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.text.toString().length < 6) {
            Toast.makeText(applicationContext, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar!!.visibility = View.VISIBLE

        //  create user in firebase, add use to realtime database (username, email, uid)
        firebaseAuth!!.createUserWithEmailAndPassword( email.text.toString(), password.text.toString())
                .addOnCompleteListener { task ->
                    progressBar!!.visibility = View.GONE

                    if(!task.isSuccessful){
                        Toast.makeText(this,"Registration Failed", Toast.LENGTH_SHORT).show()
                    } else {
                        firebaseDatabase = firebaseDatabaseInstance!!.getReference("users")

                        val user = FirebaseAuth.getInstance().currentUser
                        val myUser = User(username.text.toString(), user?.email!!, user.uid)

                        firebaseDatabase!!.child(user.uid).setValue(myUser)

                        startActivity(Intent(this, ChoosePlayerActivity::class.java))
                        finish()
                    }
                }
    }

    fun onLoginClicked(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
