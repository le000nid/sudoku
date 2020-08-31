package com.example.sudoku.view

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toast.*
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
    private var cellSt: String = ""
    private var cellStart: String = ""
    private var intendDif: String = ""
    private var won = false
    private val timerHint = object: CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            buttonAdd.isClickable = true
            buttonAdd.alpha = 1F
            makeText(this@GenActivity, "Hint is enabled", LENGTH_SHORT).show()
        }
    }
    private var start = Date(0)
    private var timesec: Int = 0

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
                intendDif = "1"
                makeText(this, "Easy sudoku genearated", LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="2" -> {
                viewModel.sudokuGame.generate()
                viewModel.sudokuGame.cleanmid()
                intendDif = "2"
                makeText(this, "Normal sudoku genearated", LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="3" -> {
                viewModel.sudokuGame.generate()
                viewModel.sudokuGame.cleanhard()
                intendDif = "3"
                makeText(this, "Hard sudoku genearated", LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="4" -> {
                loadGame()
                if (cellSt != "") {
                    viewModel.sudokuGame.vivodSt(cellSt)
                }
                if (cellStart != ""){
                    viewModel.sudokuGame.vivodStart(cellStart)
                }
                if (intendDif == "5"){
                    viewModel.sudokuGame.easyMod()
                }
                viewModel.sudokuGame.check()
                makeText(this, "Last sudoku loaded", LENGTH_SHORT).show()
            }
            intent.getStringExtra("dif")=="5" -> {
                viewModel.sudokuGame.generate()
                viewModel.sudokuGame.cleanmid()
                viewModel.sudokuGame.easyMod()
                viewModel.sudokuGame.vivod()
                intendDif = "5"
                makeText(this, "Super easy sudoku genearated", LENGTH_SHORT).show()
            }
        }
        viewModel.sudokuGame.vivod()

        val buttons = listOf(button1, button2, button3, button4, button5, button6, button7, button8, button9)

        start = Date(System.currentTimeMillis())

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.sudokuGame.handleInput(index + 1)
                if (intendDif == "5"){
                    viewModel.sudokuGame.easyMod()
                }
                if (!won && viewModel.sudokuGame.isWin()){
                    recordCheck()
                }
                viewModel.sudokuGame.check()
            }
        }

        buttonDel.setOnClickListener{
            viewModel.sudokuGame.delete()
            if (intendDif == "5") {
                viewModel.sudokuGame.easyMod()
            }
            viewModel.sudokuGame.check()
        }
    }

    override fun onPause() {
        super.onPause()
        saveGame()
    }

    public override fun onDestroy() {
        super.onDestroy()
        saveGame()
        timerHint.cancel()
    }

    fun recordCheck(){
        val sPref: SharedPreferences = getSharedPreferences("genPrefs", MODE_PRIVATE)
        timesec = if (timesec == 0) {
            ((Date(System.currentTimeMillis()).time - start.time).toInt())/1000
        } else {
            ((Date(System.currentTimeMillis()).time - start.time).toInt())/1000+sPref.getInt("time", 0)
        }
        var record = false
        val min = timesec/60
        val sec = timesec%60
        val ed: SharedPreferences.Editor = sPref.edit()
        if (intendDif == "1") {
            val rec = sPref.getInt("rectime1", 0)
            val wins = sPref.getInt("wins1", 0)
            ed.putInt("wins1", wins+1)
            if (rec > timesec || rec == 0) {
                ed.putInt("rectime1", timesec)
                record = true
            }
        } else if (intendDif == "2") {
            val rec = sPref.getInt("rectime2", 0)
            val wins = sPref.getInt("wins2", 0)
            ed.putInt("wins2", wins+1)
            if (rec > timesec || rec == 0) {
                ed.putInt("rectime2", timesec)
                record = true
            }
        } else if (intendDif == "3") {
            val rec = sPref.getInt("rectime3", 0)
            val wins = sPref.getInt("wins3", 0)
            ed.putInt("wins3", wins+1)
            if (rec > timesec || rec == 0) {
                ed.putInt("rectime3", timesec)
                record = true
            }
        } else if (intendDif == "5") {
            val rec = sPref.getInt("rectime5", 0)
            val wins = sPref.getInt("wins5", 0)
            ed.putInt("wins5", wins+1)
            if (rec > timesec || rec == 0) {
                ed.putInt("rectime5", timesec)
                record = true
            }
        }
        if (record){
            makeText(this, "You won and broke your record time with $min.$sec", LENGTH_SHORT).show()
        } else {
            makeText(this, "You won with time $min.$sec", LENGTH_SHORT).show()
        }

        won = true
        ed.putInt("time", 0)
        ed.putString("genCells", "")
        ed.putString("genStart", "")
        ed.apply()
    }

    fun addDialog(v: View){
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Hint")
        alert.setMessage("Are you sure you want a hint?")
        alert.setPositiveButton("Yes") { dialog, id ->
            if (!viewModel.sudokuGame.noerr()) {
                makeText(this, "There are mistakes in sudoku", LENGTH_SHORT).show()
            } else if (viewModel.sudokuGame.selectedRow<0 || viewModel.sudokuGame.board.getCell(viewModel.sudokuGame.selectedRow, viewModel.sudokuGame.selectedCol).value != 0){
                makeText(this, "Select empty cell", LENGTH_SHORT).show()
            } else {
                viewModel.sudokuGame.SolveOne(this)
                if(viewModel.sudokuGame.selectedRow<0 || viewModel.sudokuGame.board.getCell(viewModel.sudokuGame.selectedRow, viewModel.sudokuGame.selectedCol).value != 0) {
                    viewModel.sudokuGame.vivod()
                    timerHint.start()
                    buttonAdd.isClickable = false
                    buttonAdd.alpha = 0.3F
                    if (intendDif == "5") {
                        viewModel.sudokuGame.easyMod()
                    }
                    if (!won && viewModel.sudokuGame.isWin()) {
                        recordCheck()
                    } else {
                        makeText(this, "Hint is disabled for 30 seconds", LENGTH_SHORT).show()
                    }
                }
            }
        }
        alert.setNegativeButton("Cancel") { dialog, id -> }
        alert.create().show()
    }

    private fun saveGame(){
        if(!won) {
            val sPref: SharedPreferences = getSharedPreferences("genPrefs", MODE_PRIVATE)
            val ed: SharedPreferences.Editor = sPref.edit()
            val toput: String = viewModel.sudokuGame.vvodSt()
            ed.putString("genCells", toput)
            ed.putString("genStart", viewModel.sudokuGame.vvodStart())
            ed.putString("genDif", intendDif)
            timesec = if (timesec == 0) {
                ((Date(System.currentTimeMillis()).time - start.time).toInt())/1000
            } else {
                ((Date(System.currentTimeMillis()).time - start.time).toInt())/1000+sPref.getInt("time", 0)
            }
            ed.putInt("time", timesec)
            ed.apply()
        }
    }

    private fun loadGame(){
        val sPref: SharedPreferences = getSharedPreferences("genPrefs", MODE_PRIVATE)
        cellSt = sPref.getString("genCells", "").toString()
        cellStart = sPref.getString("genStart", "").toString()
        intendDif = sPref.getString("genDif", "").toString()
        timesec = sPref.getInt("time", 0)
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
                makeText(this, "Sudoku saved", LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}