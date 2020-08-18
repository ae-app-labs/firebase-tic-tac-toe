package com.ae.apps.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_choose_player.*
import java.util.*

class ChoosePlayerActivity : AppCompatActivity() {

    internal var currentOpponent: User? = null
    internal var loggedInUser: User? = null

    // create the DatabaseReference and FirebaseDatabase references.
    private var firebaseReference: DatabaseReference? = null
    private var firebaseInstance : FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_player)

        val arrayOfUsers = ArrayList<User>()
        val adapter = UserAdapter(this, arrayOfUsers)

        val listView = findViewById<View>(R.id.myListView) as ListView
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            currentOpponent = listView.adapter.getItem(position) as User
            etInviteEmal.setText(currentOpponent!!.email)
        }

        //  initialize FirebaseDatabase instance
        firebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node and set DatabaseReference
        firebaseReference = firebaseInstance!!.getReference("users")
        //  get currently logged in user
        val currentUser = FirebaseAuth.getInstance().currentUser
        val allUsers = firebaseReference!!.orderByChild("name")

        allUsers.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(User::class.java)

                if(currentUser!!.email == user!!.email) {
                    loggedInUser = user

                    if (user.currentlyPlaying) {
                        startActivity(Intent(this@ChoosePlayerActivity, GameActivity::class.java))
                        finish()
                    }
                } else if(!user.currentlyPlaying && user.opponentID.isEmpty()){
                    adapter.add(user)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                if(currentUser!!.email != user!!.email){
                    adapter.remove(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(User::class.java)
                val userId = snapshot.key

                if(currentUser!!.email != user!!.email){
                    if(user.currentlyPlaying || !user.opponentID.isEmpty()) {
                        adapter.remove(user)
                    }
                } else {
                    loggedInUser = user

                    if(user.request) {
                        showAcceptOrDenyInviteDialog()
                        user.request = false
                        firebaseReference!!.child(userId!!).setValue(user)
                    } else if(user.accepted == "true") {
                        progressBar.visibility = View.GONE
                        buInvite.isEnabled = true

                        firebaseReference!!.child(loggedInUser!!.myID).child("accepted").setValue("none")

                        showAcceptOrDenyStatusDialog(true)
                    } else if(user.accepted == "false") {
                        progressBar.visibility = View.GONE
                        firebaseReference!!.child(loggedInUser!!.myID).child("opponentID").setValue("")
                        firebaseReference!!.child(loggedInUser!!.myID).child("opponentEmail").setValue("")
                        firebaseReference!!.child(loggedInUser!!.myID).child("accepted").setValue("none")

                        showAcceptOrDenyStatusDialog(false)
                        buInvite.isEnabled = true
                    }
                }
            }


        })
    }

    private fun showAcceptOrDenyStatusDialog(status: Boolean) {
        val alertDialog = AlertDialog.Builder(this)

        // Setting Dialog Title
        alertDialog.setTitle("Game Invite Status...")

        // Setting Dialog Message
        if (status)
            alertDialog.setMessage("Your game with " + loggedInUser!!.opponentEmail + " has been accepted")
        else
            alertDialog.setMessage("Your game with " + loggedInUser!!.opponentEmail + " has been denied")


        // Setting Positive "Yes" Btn
        alertDialog.setPositiveButton("OK"
        ) { _, _ ->
            // navigate to game screen
            if (status) {
                startActivity(Intent(this@ChoosePlayerActivity, GameActivity::class.java))
            }
        }

        // Showing Alert Dialog
        alertDialog.show()
    }

    private fun showAcceptOrDenyInviteDialog() {
        val alertDialog = AlertDialog.Builder(this)

        // Setting Dialog Title
        alertDialog.setTitle("Accept Game Invite...")

        // Setting Dialog Message
        alertDialog.setMessage("Would you like to play tic tac toe against " + loggedInUser!!.opponentEmail + "?")

        // Setting Positive "Yes" Btn
        alertDialog.setPositiveButton("YES"
        ) { _, _ ->
            // create game and go there
            val game = Game(loggedInUser!!.opponentID)

            //  update data fields
            firebaseReference!!.child(loggedInUser!!.opponentID).child("myGame").setValue(game)
            firebaseReference!!.child(loggedInUser!!.myID).child("myGame").setValue(game)


            // set game status for both players (currently playing)
            firebaseReference!!.child(loggedInUser!!.opponentID).child("currentlyPlaying").setValue(true)
            firebaseReference!!.child(loggedInUser!!.myID).child("currentlyPlaying").setValue(true)

            firebaseReference!!.child(loggedInUser!!.opponentID).child("accepted").setValue("true")

            // navigate to game screen
            startActivity(Intent(this@ChoosePlayerActivity, GameActivity::class.java))
        }

        // Setting Negative "NO" Btn
        alertDialog.setNegativeButton("NO"
        ) { dialog, _ ->
            // update database fields with denial of playing
            firebaseReference!!.child(loggedInUser!!.myID).child("opponentID").setValue("")
            firebaseReference!!.child(loggedInUser!!.myID).child("opponentEmail").setValue("")
            firebaseReference!!.child(loggedInUser!!.opponentID).child("accepted").setValue("false")
            dialog.cancel()
        }

        // Showing Alert Dialog
        alertDialog.show()
    }

    fun onClickInvite(view: View) {
        if (currentOpponent != null) {
            // update database
            // set opponent id for selected user to invite and let them know they have an invite in database
            firebaseReference!!.child(currentOpponent!!.myID).child("opponentID").setValue(loggedInUser!!.myID)
            firebaseReference!!.child(currentOpponent!!.myID).child("opponentEmail").setValue(loggedInUser!!.email)
            firebaseReference!!.child(currentOpponent!!.myID).child("request").setValue(true)

            // set opponent id for current logged in user in database
            firebaseReference!!.child(loggedInUser!!.myID).child("opponentID").setValue(currentOpponent!!.myID)
            firebaseReference!!.child(loggedInUser!!.myID).child("opponentEmail").setValue(currentOpponent!!.email)
            firebaseReference!!.child(loggedInUser!!.myID).child("accepted").setValue("pending")

            progressBar!!.visibility = View.VISIBLE
            buInvite.isEnabled = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.logout -> {
                //  sign out of Firebase
                FirebaseAuth.getInstance().signOut()

                startActivity(Intent(this@ChoosePlayerActivity, RegisterActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private val TAG = ChoosePlayerActivity::class.java.simpleName
    }
}
