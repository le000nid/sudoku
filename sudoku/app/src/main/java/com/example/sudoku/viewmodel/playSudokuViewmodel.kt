package com.example.sudoku.viewmodel

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.example.sudoku.game.SudokuGame

class playSudokuViewmodel : ViewModel() {
    val sudokuGame = SudokuGame()
}