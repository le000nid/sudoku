package com.example.sudoku.view

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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


class OwnActivity : AppCompatActivity(), BoardView.OnTouchListener {

    private lateinit var viewModel: playSudokuViewmodel
    var cellSt: String = ""
    var cellStart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BoardView.registerListener(this)

        viewModel = ViewModelProviders.of(this).get(playSudokuViewmodel::class.java)
        viewModel.sudokuGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it) })
        viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it) })



        val intent = intent
        if (intent.getIntArrayExtra("ar") != null){
            viewModel.sudokuGame.vvod(intent.getIntArrayExtra("ar"))
            viewModel.sudokuGame.vivod()
        } else {
            loadGame()
            if (cellSt != "") {
                viewModel.sudokuGame.vivodSt(cellSt)
            }
            if (cellStart != ""){
                viewModel.sudokuGame.vivodStart(cellStart)
            }
            viewModel.sudokuGame.check()
            viewModel.sudokuGame.vivod()
        }

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

    override fun onDestroy() {
        super.onDestroy()
        saveGame()
    }

    override fun onPause() {
        super.onPause()
        saveGame()
    }

    fun addDialog(v: View){
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Hint")
        alert.setMessage("Are you sure you want a hint?")
        alert.setPositiveButton("Yes"
        ) { dialog, id ->
            if (!viewModel.sudokuGame.noerr()) {
                Toast.makeText(this, "There are mistakes in sudoku", Toast.LENGTH_SHORT).show()
            } else if (viewModel.sudokuGame.selectedRow<0){
                Toast.makeText(this, "Select empty cell", Toast.LENGTH_SHORT).show()
            } else if (viewModel.sudokuGame.board.getCell(viewModel.sudokuGame.selectedRow, viewModel.sudokuGame.selectedCol).value != 0){
                Toast.makeText(this, "Select empty cell", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.sudokuGame.SolveOne(this)
                viewModel.sudokuGame.vivod()
            }
        }
        alert.setNegativeButton("Cancel"
        ) { dialog, id -> }
        alert.create().show()
    }

    private fun saveGame(){
        val sPref: SharedPreferences = getPreferences(MODE_PRIVATE)
        val ed: SharedPreferences.Editor = sPref.edit()
        ed.putString("cells", viewModel.sudokuGame.vvodSt())
        ed.putString("Start", viewModel.sudokuGame.vvodStart())
        ed.commit()
    }

    private fun loadGame(){
        var sPref: SharedPreferences = getPreferences(MODE_PRIVATE)
        cellSt = sPref.getString("cells", "").toString()
        cellStart = sPref.getString("Start", "").toString()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.own_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.clean -> {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Erase")
                alert.setMessage("Erase sudoku?")
                alert.setPositiveButton("Yes"
                ) { dialog, id ->
                    viewModel.sudokuGame.cleaner()
                    Toast.makeText(this, "Cleaned", Toast.LENGTH_SHORT).show()
                }
                alert.setNegativeButton("Cancel"
                ) { dialog, id -> }
                alert.create().show()
            }
            R.id.done -> {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Done")
                alert.setMessage("Do cells unchangeable?")
                alert.setPositiveButton("Yes") { dialog, id ->
                    if (viewModel.sudokuGame.noerr()) {
                        viewModel.sudokuGame.done(this)
                        viewModel.sudokuGame.selectedCellLiveData.postValue(Pair(-1, -1))
                        Toast.makeText(this, "Cells are unchangeable", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, "There are mistakes in sudoku", Toast.LENGTH_SHORT).show()
                    }
                }
                alert.setNeutralButton("Do changeable") { dialog, id ->
                    viewModel.sudokuGame.undone()
                    Toast.makeText(this, "Cells are changeable", Toast.LENGTH_SHORT).show()
                }
                alert.setNegativeButton("Cancel") { dialog, id -> }
                alert.create().show()
            }
            R.id.doo -> {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Solve")
                alert.setMessage("Solve sudoku?")
                alert.setPositiveButton("Yes") { dialog, id ->
                    if (viewModel.sudokuGame.noerr()){
                        viewModel.sudokuGame.Solver(this)
                        viewModel.sudokuGame.vivod()
                    } else {
                        Toast.makeText(this, "There are mistakes in sudoku", Toast.LENGTH_SHORT).show()
                    }
                }
                alert.setNegativeButton("Cancel"
                ) { dialog, id -> }
                alert.create().show()
            }
            R.id.save -> {
                saveGame()
                Toast.makeText(this, "Sudoku saved", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}