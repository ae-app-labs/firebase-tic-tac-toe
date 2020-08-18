package com.ae.apps.tictactoe

class User {
    lateinit var myID: String
    lateinit var opponentID: String
    lateinit var name: String
    lateinit var email: String
    lateinit var opponentEmail: String
    var request: Boolean = false
    lateinit var accepted: String
    var currentlyPlaying: Boolean = false
    var myGame: Game? = null

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    constructor() {}

    constructor(name: String, email: String, id: String) {
        this.name = name
        this.email = email
        this.myID = id
        opponentID = ""
        opponentEmail = ""
        accepted = "none"
        request = false
        myGame = null
        currentlyPlaying = false
    }
}
