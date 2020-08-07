package com.example.sudoku.view

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
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
import java.util.*

class GenActivity : AppCompatActivity(), BoardView.OnTouchListener {

    private lateinit var viewModel: playSudokuViewmodel
    var cellSt: String = ""
    var cellStart: String = ""
    val timerHint = object: CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            buttonAdd.isClickable = true
            Toast.makeText(this@GenActivity, "Hint is enabled", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BoardView.registerListener(this)

        viewModel = ViewModelProviders.of(this).get(playSudokuViewmodel::class.java)
        viewModel.sudokuGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it) })
        viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it) })

        val intent = intent
        when {
            intent.getStringExtra("dif")=="1" -> {
                viewModel.sudokuGame.generate()
                viewModel.sudokuGame.cleaneasy()
                Toast.makeText(this, "Easy sudoku genearated", Toast.LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="2" -> {
                viewModel.sudokuGame.generate()
                viewModel.sudokuGame.cleanmid()
                Toast.makeText(this, "Normal sudoku genearated", Toast.LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="3" -> {
                viewModel.sudokuGame.generate()
                viewModel.sudokuGame.cleanhard()
                Toast.makeText(this, "Hard sudoku genearated", Toast.LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="4" -> {
                loadGame()
                if (cellSt != "") {
                    viewModel.sudokuGame.vivodSt(cellSt)
                }
                if (cellStart != ""){
                    viewModel.sudokuGame.vivodStart(cellStart)
                }
                viewModel.sudokuGame.check()
                Toast.makeText(this, "Last sudoku loaded", Toast.LENGTH_SHORT).show()
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

    override fun onPause() {
        super.onPause()
        saveGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveGame()
        timerHint.cancel()
    }

    fun addDialog(v: View){
        var alert = AlertDialog.Builder(this)
        alert.setTitle("Hint")
        alert.setMessage("Are you sure you want a hint?")
        alert.setPositiveButton("Yes") { dialog, id ->
            if (viewModel.sudokuGame.noerr()){
                viewModel.sudokuGame.SolveOne(this)
                viewModel.sudokuGame.vivod()
                timerHint.start()
                buttonAdd.isClickable = false
                Toast.makeText(this, "Hint is disabled for 30 seconds", Toast.LENGTH_SHORT).show()
            }
            if (!viewModel.sudokuGame.noerr()){
                Toast.makeText(this, "There are mistakes in sudoku", Toast.LENGTH_SHORT).show()
            }
        }
        alert.setNegativeButton("Cancel") { dialog, id -> }
        alert.create().show()
    }

    private fun saveGame(){
        var sPref: SharedPreferences = getPreferences(MODE_PRIVATE)
        val ed: SharedPreferences.Editor = sPref.edit()
        val toput: String = viewModel.sudokuGame.vvodSt()
        ed.putString("genCells", toput)
        ed.putString("genStart", viewModel.sudokuGame.vvodStart())
        ed.commit()
    }

    private fun loadGame(){
        var sPref: SharedPreferences = getPreferences(MODE_PRIVATE)
        cellSt = sPref.getString("genCells", "").toString()
        cellStart = sPref.getString("genStart", "").toString()
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
        menuInflater.inflate(R.menu.gen_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save -> {
                saveGame()
                Toast.makeText(this, "Sudoku saved", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}