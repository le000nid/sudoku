package com.example.sudoku.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.sudoku.R
import com.example.sudoku.game.Cell
import com.example.sudoku.view.custom.BoardView
import com.example.sudoku.viewmodel.playSudokuViewmodel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BoardView.OnTouchListener {

    private lateinit var viewModel: playSudokuViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BoardView.registerListener(this)

        viewModel = ViewModelProviders.of(this).get(playSudokuViewmodel::class.java)
        viewModel.sudokuGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it) })
        viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it) })

        val buttons = listOf(button1, button2, button3, button4, button5, button6, button7, button8, button9)

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.sudokuGame.handleInput(index + 1)
                viewModel.sudokuGame.check()
            }
        }

        buttonDel.setOnClickListener{viewModel.sudokuGame.delete()
            viewModel.sudokuGame.check()}

        buttonAll.setOnClickListener{viewModel.sudokuGame.solver()
        viewModel.sudokuGame.vivod()}

        buttonAdd.setOnClickListener{viewModel.sudokuGame.solveOne()
        viewModel.sudokuGame.vivod()}
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
