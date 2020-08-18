package com.ae.apps.tictactoe


class Game {

    var gameInProgress: Boolean = false
    lateinit var currentTurn: String
    var currentMove: Int = 0
    lateinit var currentLetter: String

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    constructor() {}

    constructor(currentTurn: String) {
        this.currentTurn = currentTurn
        currentLetter = "X"
        gameInProgress = true
        currentMove = -1
    }
}
