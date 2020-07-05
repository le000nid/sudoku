package com.example.sudoku.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.sudoku.R
import com.example.sudoku.game.Cell
import com.example.sudoku.view.custom.BoardView
import com.example.sudoku.viewmodel.playSudokuViewmodel
import kotlinx.android.synthetic.main.activity_main.*

class GenActivity : AppCompatActivity(), BoardView.OnTouchListener {

    private lateinit var viewModel: playSudokuViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BoardView.registerListener(this)

        viewModel = ViewModelProviders.of(this).get(playSudokuViewmodel::class.java)
        viewModel.sudokuGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it) })
        viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it) })

        viewModel.sudokuGame.cleaner()
        viewModel.sudokuGame.generate()
        val intent = intent
        when {
            intent.getStringExtra("dif")=="1" -> {
                viewModel.sudokuGame.cleaneasy()
                Toast.makeText(this, "Easy sudoku genearated", Toast.LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="2" -> {
                viewModel.sudokuGame.cleanmid()
                Toast.makeText(this, "Normal sudoku genearated", Toast.LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="3" -> {
                viewModel.sudokuGame.cleanhard()
                Toast.makeText(this, "Hard sudoku genearated", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.sudokuGame.vivod()

        val buttons = listOf(button1, button2, button3, button4, button5, button6, button7, button8, button9)

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.sudokuGame.handleInput(index + 1)
                viewModel.sudokuGame.check()
            }
        }

        buttonDel.setOnClickListener{viewModel.sudokuGame.delete()
            viewModel.sudokuGame.check()}
    }

    fun addDialog(v: View){
        var alert = AlertDialog.Builder(this)
        alert.setTitle("Hint")
        alert.setMessage("Are you sure you want a hint?")
        alert.setPositiveButton("Yes"
        ) { dialog, id ->
            val cell = viewModel.sudokuGame.board.getCell(viewModel.sudokuGame.selectedRow, viewModel.sudokuGame.selectedCol)
            if (viewModel.sudokuGame.noerr()){
                viewModel.sudokuGame.solveOne()
                viewModel.sudokuGame.vivod()
            }
            if (!viewModel.sudokuGame.noerr()){
                Toast.makeText(this, "there are mistakes in sudoku", Toast.LENGTH_SHORT).show()
            } else if (cell.value==0){
                Toast.makeText(this, "there are no solution for this sudoku", Toast.LENGTH_SHORT).show()
            }
        }
        alert.setNegativeButton("Cancel"
        ) { dialog, id ->
            Toast.makeText(this, "Hint was canceled", Toast.LENGTH_SHORT).show()
        }
        alert.create().show()
    }

    private fun updateCells(cells: List<Cell>?) = cells?.let {
        BoardView.updateCells(cells)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        BoardView.updateSelectedCellUI(cell.first, cell.second)
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.sudokuGame.updateSelectedCell(row,col)
    }
}