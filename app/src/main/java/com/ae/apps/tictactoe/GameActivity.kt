package com.ae.apps.tictactoe

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    //  create the DatabaseReference and FirebaseDatabase references.
    private var firebaseDatabase: DatabaseReference? = null
    private var firebaseInistance: FirebaseDatabase? = null

    internal var loggedInUser: User? = null
    private var userId: String? = null

    // represents buttons clicked
    lateinit var gameBoard: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // draws the game board
        drawGameBoard()

        // initializes the game board array to 0
        gameBoard = IntArray(9)

        for (i in gameBoard.indices)
            gameBoard[i] = 0

        // sets the on click listener for all of the game buttons
        GameButton1.setOnClickListener { gameTurn(0) }
        GameButton2.setOnClickListener { gameTurn(1) }
        GameButton3.setOnClickListener { gameTurn(2) }
        GameButton4.setOnClickListener { gameTurn(3) }
        GameButton5.setOnClickListener { gameTurn(4) }
        GameButton6.setOnClickListener { gameTurn(5) }
        GameButton7.setOnClickListener { gameTurn(6) }
        GameButton8.setOnClickListener { gameTurn(7) }
        GameButton9.setOnClickListener { gameTurn(8) }

        // sends the user back to the mainActivity
        choosePlayerMenu.setOnClickListener {
            val intent = Intent(this@GameActivity, ChoosePlayerActivity::class.java)
            startActivity(intent)
        }

        //  initialize FirebaseDatabase instance
        firebaseInistance = FirebaseDatabase.getInstance()
        // get reference to 'users' node and set DatabaseReference
        firebaseDatabase = firebaseInistance!!.getReference("users")
        //  get currently logged in user
        val currentUserLoggedIn = FirebaseAuth.getInstance().currentUser
        // set the userId to the currentUserLoggedIn.uid
        userId = currentUserLoggedIn!!.uid

        addUserChangeListener()
    }

    /**
     * User data change listener
     */
    private fun addUserChangeListener() {

        // implement actions when data changes for a particular user
        // set the currently logged in user
        // set the playerTextView
        // check status of game and update gameBoard array and display the move by calling printMove
        // need to implement all game logic and User Interface updates, in addition to updating Firebase database
        firebaseDatabase!!.child(userId!!).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                loggedInUser = user

                playerTextView.text = loggedInUser!!.email + " vs " + loggedInUser!!.opponentEmail

                val myGame = user?.myGame!!
                if(myGame.gameInProgress == false && user.currentlyPlaying == true){
                    if(myGame.currentLetter == "X") {
                        gameBoard[myGame.currentMove] = 2
                        printMove(myGame.currentMove, "O")
                    } else {
                        gameBoard[myGame.currentMove] = 1
                        printMove(myGame.currentMove, "X")
                    }

                    removeAllButtons()

                    if(checkGameWon() == 0);

                    checkTie()

                    firebaseDatabase!!.child(loggedInUser!!.myID).child("myGame").child("gameInProgress").setValue(false)
                    firebaseDatabase!!.child(loggedInUser!!.myID).child("currentlyPlaying").setValue(false)

                    firebaseDatabase!!.child(loggedInUser!!.opponentID).child("myGame").child("gameInProgress").setValue(false)
                    firebaseDatabase!!.child(loggedInUser!!.opponentID).child("currentlyPlaying").setValue(false)

                    firebaseDatabase!!.child(loggedInUser!!.myID).child("opponentEmail").setValue("")
                    firebaseDatabase!!.child(loggedInUser!!.myID).child("opponentID").setValue("")

                    firebaseDatabase!!.child(loggedInUser!!.opponentID).child("opponentEmail").setValue("")
                    firebaseDatabase!!.child(loggedInUser!!.opponentID).child("opponentID").setValue("")

                    Toast.makeText(this@GameActivity, "Game is Over", Toast.LENGTH_SHORT).show()
                } else if(user.myGame!!.currentTurn == user.myID){
                    turnTextView.text = "Your turn to make a move"

                    val myGame = user!!.myGame!!

                    if(myGame.currentMove != -1) {
                        if (loggedInUser!!.myGame!!.currentLetter == "X") {
                            gameBoard[myGame.currentMove] = 2
                            printMove(myGame.currentMove, "O")
                        } else {
                            gameBoard[myGame.currentMove] = 1
                            printMove(myGame.currentMove, "X")
                        }

                        updateButtons()
                    }
                } else {
                    removeAllButtons()

                    turnTextView.text = "Waiting for the other player"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    // handles the players turn
    internal fun gameTurn(move: Int) {
        var gameWon : Int
        var gameTie : Boolean

        // if the user tried to select a button after the game is over, just hide the buttons and return
        if (loggedInUser!!.currentlyPlaying == false)
        {
            removeAllButtons()
            return
        }

        // set the board to a move and print it
        if (loggedInUser!!.myGame!!.currentLetter == "X") {
            gameBoard[move] = 1
            printMove(move, "X")
        } else {
            gameBoard[move] = 2
            printMove(move, "O")
        }

        // update the status of the buttons, graying out the ones that were played
        updateButtons()

        // checks if the game has been won or not
        gameWon = checkGameWon()

        // checks of the game has been tied
        gameTie = checkTie()

        // TODO: create game, set the database values, update the UI, based on game logic
        val game = Game(loggedInUser!!.opponentID)
        game.currentMove = move

        if(loggedInUser!!.myGame!!.currentLetter == "X" )
            game.currentLetter = "O"
        else
            game.currentLetter = "X"

        if(gameWon == 0 && !gameTie){
            game.gameInProgress = true
        } else if(gameWon != 0) {
            removeAllButtons()
            game.gameInProgress = false
        } else {
            playerTextView.text = "The game is a tie"
            turnTextView.text = "No one Wins"
            game.gameInProgress = false
        }

        firebaseDatabase!!.child(loggedInUser!!.myID).child("myGame").setValue(game)
        firebaseDatabase!!.child(loggedInUser!!.opponentID).child("myGame").setValue(game)

    }

    // update status of buttons
    internal fun updateButtons() {
        if (gameBoard[0] != 0) {
            GameButton1.visibility = View.INVISIBLE
        } else
            GameButton1.visibility = View.VISIBLE

        if (gameBoard[1] != 0) {
            GameButton2.visibility = View.INVISIBLE
        } else
            GameButton2.visibility = View.VISIBLE

        if (gameBoard[2] != 0) {
            GameButton3.visibility = View.INVISIBLE
        } else
            GameButton3.visibility = View.VISIBLE

        if (gameBoard[3] != 0) {
            GameButton4.visibility = View.INVISIBLE
        } else
            GameButton4.visibility = View.VISIBLE

        if (gameBoard[4] != 0) {
            GameButton5.visibility = View.INVISIBLE
        } else
            GameButton5.visibility = View.VISIBLE

        if (gameBoard[5] != 0) {
            GameButton6.visibility = View.INVISIBLE
        } else
            GameButton6.visibility = View.VISIBLE

        if (gameBoard[6] != 0) {
            GameButton7.visibility = View.INVISIBLE
        } else
            GameButton7.visibility = View.VISIBLE

        if (gameBoard[7] != 0) {
            GameButton8.visibility = View.INVISIBLE
        } else
            GameButton8.visibility = View.VISIBLE

        if (gameBoard[8] != 0) {
            GameButton9.visibility = View.INVISIBLE
        } else
            GameButton9.visibility = View.VISIBLE
    }

    // removes all of the buttons if it is not a players turn or the game is over
    internal fun removeAllButtons() {
        GameButton1.visibility = View.INVISIBLE
        GameButton2.visibility = View.INVISIBLE
        GameButton3.visibility = View.INVISIBLE
        GameButton4.visibility = View.INVISIBLE
        GameButton5.visibility = View.INVISIBLE
        GameButton6.visibility = View.INVISIBLE
        GameButton7.visibility = View.INVISIBLE
        GameButton8.visibility = View.INVISIBLE
        GameButton9.visibility = View.INVISIBLE
    }


    /*  DRAW CODE */

    // draws an X at a specified location
    internal fun drawX(startx1: Int, starty1: Int, endx1: Int, endy1: Int, startx2: Int, starty2: Int, endx2: Int, endy2: Int, imageView: ImageView) {
        val bitmap = Bitmap.createBitmap(windowManager.defaultDisplay.width, windowManager.defaultDisplay.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.setImageBitmap(bitmap)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.strokeWidth = 10f
        canvas.drawLine(startx1.toFloat(), starty1.toFloat(), endx1.toFloat(), endy1.toFloat(), paint)
        canvas.drawLine(startx2.toFloat(), starty2.toFloat(), endx2.toFloat(), endy2.toFloat(), paint)
        imageView.visibility = View.VISIBLE

        // animates the drawing to fade in
        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 2000
        imageView.startAnimation(animation)
    }

    // draws an O at a specified location
    internal fun drawO(x: Float, y: Int, imageView: ImageView) {
        val bitmap = Bitmap.createBitmap(windowManager.defaultDisplay.width, windowManager.defaultDisplay.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.setImageBitmap(bitmap)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(x, y.toFloat(), 100f, paint)
        imageView.visibility = View.VISIBLE

        // animates the drawing to fade in
        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 2000
        imageView.startAnimation(animation)
    }

    // draws a line at a specified location, when the game has won
    internal fun drawLine(startx: Int, starty: Int, endx: Int, endy: Int, imageView: ImageView) {
        imageView.visibility = View.VISIBLE
        val bitmap = Bitmap.createBitmap(windowManager.defaultDisplay.width, windowManager.defaultDisplay.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.setImageBitmap(bitmap)

        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 20f
        paint.style = Paint.Style.STROKE

        canvas.drawLine(startx.toFloat(), starty.toFloat(), endx.toFloat(), endy.toFloat(), paint)

        // animates the drawing to fade in
        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 4000
        imageView.startAnimation(animation)
    }

    // checks to see if the game has been won, if it has it draws a line over the winning squares
    internal fun checkGameWon(): Int {
        val sx: Int
        val sy: Int
        val ex: Int
        val ey: Int
        val size = Point()
        // gets the size of the current display window
        val display = windowManager.defaultDisplay
        display.getSize(size)
        val width = size.x
        val height = size.y

        // win first row
        if (gameBoard[0] != 0 && gameBoard[0] == gameBoard[1] && gameBoard[0] == gameBoard[2]) {
            sx = 0
            sy = (height * .30).toInt()

            ex = width
            ey = (height * .30).toInt()

            drawLine(sx, sy, ex, ey, imageView10)
            return gameBoard[0]
        } else if (gameBoard[3] != 0 && gameBoard[3] == gameBoard[4] && gameBoard[3] == gameBoard[5]) {
            sx = 0
            sy = (height * .54).toInt()

            ex = width
            ey = (height * .54).toInt()

            drawLine(sx, sy, ex, ey, imageView10)
            return gameBoard[3]
        } else if (gameBoard[6] != 0 && gameBoard[6] == gameBoard[7] && gameBoard[6] == gameBoard[8]) {
            sx = 0
            sy = (height * .77).toInt()

            ex = width
            ey = (height * .77).toInt()

            drawLine(sx, sy, ex, ey, imageView10)
            return gameBoard[6]
        } else if (gameBoard[0] != 0 && gameBoard[0] == gameBoard[3] && gameBoard[0] == gameBoard[6]) {
            sx = (width * .15).toInt()
            sy = (height * .18).toInt()

            ex = (width * .15).toInt()
            ey = (height * .89).toInt()

            drawLine(sx, sy, ex, ey, imageView10)
            return gameBoard[0]
        } else if (gameBoard[1] != 0 && gameBoard[1] == gameBoard[4] && gameBoard[1] == gameBoard[7]) {
            sx = (width * .50).toInt()
            sy = (height * .18).toInt()

            ex = (width * .50).toInt()
            ey = (height * .89).toInt()

            drawLine(sx, sy, ex, ey, imageView10)
            return gameBoard[1]
        } else if (gameBoard[2] != 0 && gameBoard[2] == gameBoard[5] && gameBoard[2] == gameBoard[8]) {
            sx = (width * .85).toInt()
            sy = (height * .18).toInt()

            ex = (width * .85).toInt()
            ey = (height * .89).toInt()

            drawLine(sx, sy, ex, ey, imageView10)
            return gameBoard[2]
        } else if (gameBoard[0] != 0 && gameBoard[0] == gameBoard[4] && gameBoard[0] == gameBoard[8]) {
            sx = (width * .01).toInt()
            sy = (height * .21).toInt()

            ex = (width * .99).toInt()
            ey = (height * .86).toInt()

            drawLine(sx, sy, ex, ey, imageView10)
            return gameBoard[0]
        } else if (gameBoard[2] != 0 && gameBoard[2] == gameBoard[4] && gameBoard[2] == gameBoard[6]) {
            sx = (width * .99).toInt()
            sy = (height * .21).toInt()

            ex = (width * .01).toInt()
            ey = (height * .86).toInt()

            drawLine(sx, sy, ex, ey, imageView10)
            return gameBoard[2]
        } else
            // game not won
            return 0
    }

    // check to see if the game is a tie
    internal fun checkTie(): Boolean {
        for (aGameBoard in gameBoard) {
            if (aGameBoard == 0)
                return false
        }

        return true
    }

    // prints the move that was played
    internal fun printMove(move: Int, character: String) {
        val x: Int
        val y: Int
        // gets the size of the display window
        val size = Point()
        val display = windowManager.defaultDisplay

        display.getSize(size)
        val width = size.x
        val height = size.y
        // switch statement for each move
        when (move) {
            0 -> {
                x = (width * .15).toInt()
                y = (height * .30).toInt()
                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView1)
                else
                    drawO(x.toFloat(), y, imageView1)
            }
            1 -> {
                x = (width * .50).toInt()
                y = (height * .30).toInt()

                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView2)
                else
                    drawO(x.toFloat(), y, imageView2)
            }
            2 -> {
                x = (width * .85).toInt()
                y = (height * .30).toInt()

                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView3)
                else
                    drawO(x.toFloat(), y, imageView3)
            }
            3 -> {
                x = (width * .15).toInt()
                y = (height * .54).toInt()

                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView4)
                else
                    drawO(x.toFloat(), y, imageView4)
            }
            4 -> {
                x = (width * .50).toInt()
                y = (height * .54).toInt()

                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView5)
                else
                    drawO(x.toFloat(), y, imageView5)
            }

            5 -> {
                x = (width * .85).toInt()
                y = (height * .54).toInt()

                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView6)
                else
                    drawO(x.toFloat(), y, imageView6)
            }

            6 -> {
                x = (width * .15).toInt()
                y = (height * .77).toInt()

                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView7)
                else
                    drawO(x.toFloat(), y, imageView7)
            }

            7 -> {
                x = (width * .50).toInt()
                y = (height * .77).toInt()

                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView8)
                else
                    drawO(x.toFloat(), y, imageView8)
            }

            8 -> {
                x = (width * .85).toInt()
                y = (height * .77).toInt()

                if (character == "X")
                    drawX(x - 100, y - 100, x + 100, y + 100, x + 100, y - 100, x - 100, y + 100, imageView9)
                else
                    drawO(x.toFloat(), y, imageView9)
            }
        }
    }

    // draws the game board on the screen
    private fun drawGameBoard() {
        tictactoeView.visibility = View.VISIBLE
        val bitmap = Bitmap.createBitmap(windowManager.defaultDisplay.width, windowManager.defaultDisplay.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        tictactoeView.setImageBitmap(bitmap)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.strokeWidth = 20f
        paint.style = Paint.Style.STROKE

        var sx: Int
        var sy: Int
        var ex: Int
        var ey: Int
        var x: Int
        var y: Int

        val size = Point()
        val display = windowManager.defaultDisplay

        display.getSize(size)
        val width = size.x
        val height = size.y

        sx = 0
        sy = (height * .42).toInt()
        ex = width
        ey = (height * .42).toInt()

        canvas.drawLine(sx.toFloat(), sy.toFloat(), ex.toFloat(), ey.toFloat(), paint)

        sx = 0
        sy = (height * .65).toInt()
        ex = width
        ey = (height * .65).toInt()

        canvas.drawLine(sx.toFloat(), sy.toFloat(), ex.toFloat(), ey.toFloat(), paint)

        sx = (width * .32).toInt()
        sy = (height * .18).toInt()
        ex = (width * .32).toInt()
        ey = (height * .89).toInt()

        canvas.drawLine(sx.toFloat(), sy.toFloat(), ex.toFloat(), ey.toFloat(), paint)

        sx = (width * .68).toInt()
        sy = (height * .18).toInt()
        ex = (width * .68).toInt()
        ey = (height * .89).toInt()

        canvas.drawLine(sx.toFloat(), sy.toFloat(), ex.toFloat(), ey.toFloat(), paint)

        x = (width * .07).toInt()
        y = (height * .22).toInt()

        GameButton1.x = x.toFloat()
        GameButton1.y = y.toFloat()

        x = (width * .38).toInt()
        y = (height * .22).toInt()

        GameButton2.x = x.toFloat()
        GameButton2.y = y.toFloat()

        x = (width * .69).toInt()
        y = (height * .22).toInt()

        GameButton3.x = x.toFloat()
        GameButton3.y = y.toFloat()

        x = (width * .07).toInt()
        y = (height * .43).toInt()

        GameButton4.x = x.toFloat()
        GameButton4.y = y.toFloat()

        x = (width * .38).toInt()
        y = (height * .43).toInt()

        GameButton5.x = x.toFloat()
        GameButton5.y = y.toFloat()

        x = (width * .69).toInt()
        y = (height * .43).toInt()

        GameButton6.x = x.toFloat()
        GameButton6.y = y.toFloat()

        x = (width * .07).toInt()
        y = (height * .64).toInt()

        GameButton7.x = x.toFloat()
        GameButton7.y = y.toFloat()

        x = (width * .38).toInt()
        y = (height * .64).toInt()

        GameButton8.x = x.toFloat()
        GameButton8.y = y.toFloat()

        x = (width * .69).toInt()
        y = (height * .64).toInt()

        GameButton9.x = x.toFloat()
        GameButton9.y = y.toFloat()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.logout -> {
                Toast.makeText(this, "Clicked Logoff", Toast.LENGTH_SHORT).show()

                //  sign out of Firebase
                FirebaseAuth.getInstance().signOut()

                startActivity(Intent(this@GameActivity, RegisterActivity::class.java))
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private val TAG = GameActivity::class.java.simpleName
    }

}
