package com.example.sudoku.game

import BitListRestriction
import SudokuSolverBoard
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import kotlin.collections.MutableSet as MutableSet

class SudokuGame {
    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()

    var selectedRow = -1
    var selectedCol = -1

    val board: Board

    val sudokuSolverBoard = SudokuSolverBoard()

    init {
        val cells = List(9 * 9) { i -> Cell(i / 9, i % 9, 0) }
        board = Board(9, cells)

        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.cells)
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        val cell = board.getCell(selectedRow,selectedCol)
        if (cell.isStartingCell) return

        cell.value = number
        cellsLiveData.postValue(board.cells)
    }

    fun updateSelectedCell(row: Int, col: Int) {
        if (!board.getCell(row, col).isStartingCell) {
            selectedRow = row
            selectedCol = col
            selectedCellLiveData.postValue(Pair(row, col))
        }
    }

    fun delete() {
        val cell = board.getCell(selectedRow, selectedCol)
        if (cell.isStartingCell) return
        cell.value = 0
        cellsLiveData.postValue(board.cells)
    }

    fun check(){
        for(i in 0..8){
            for(j in 0..8){
                val cell = board.getCell(i, j)
                cell.isErrCell = false
                if (cell.value != 0 && !valid(cell.value, cell.row, cell.col)){
                    cell.isErrCell = true
                }
            }
        }
    }

    fun noerr(): Boolean{
        for(i in 0..8){
            for(j in 0..8) {
                val cell = board.getCell(i, j)
                if (cell.isErrCell){
                    return false
                }
            }
        }
        return true
    }

    fun vvod(temp: IntArray){
        for(i in 0..80){
            val cell = board.getCell(i/9, i%9)
            cell.value = temp[i]
        }
    }

    fun vvodSt():String{
        var bor = ""
        for(i in 0..80){
            val cell = board.getCell(i/9, i%9)
            bor += cell.value
        }
        return bor
    }

    fun vvodStart():String{
        var bor = ""
        for (i in 0..80){
            val cell = board.getCell(i/9, i%9)
            if (cell.isStartingCell){
                bor += 1
            } else {
                bor += 0
            }
        }
        return bor
    }

    fun vivodStart(cellStart: String){
        for (i in 0..80){
            if (cellStart[i].toString().toInt() == 1){
                val cell = board.getCell(i/9, i%9)
                cell.isStartingCell = true
            }
        }
        cellsLiveData.postValue(board.cells)
    }

    fun vivodSt(cellSt: String){
        for (i in 0..80){
            val cell = board.getCell(i/9, i%9)
            cell.value = cellSt[i].toString().toInt()
        }
        cellsLiveData.postValue(board.cells)
    }

    fun valid(n: Int, r: Int, c: Int): Boolean{
        for(i in 0..8){
            val cell = board.getCell(r, i)
            if(cell.value == n && i != c){
                return false
            }
        }
        for(i in 0..8){
            val cell = board.getCell(i, c)
            if(cell.value == n && i != r){
                return false
            }
        }
        val x = c / 3
        val y = r / 3
        for(i in y*3..y*3+2){
            for (j in x*3..x*3+2){
                val cell = board.getCell(i,j)
                if (cell.value == n && i != r && j != c){
                    return false
                }
            }
        }
        return true
    }

    fun Solver(context: Context){
        for (r in 0..8){
            for (c in 0..8){
                val cell = board.getCell(r,c)
                sudokuSolverBoard._values[r][c] = cell.value
            }
        }
        if (sudokuSolverBoard.Solve()){
            for (r in 0..8){
                for (c in 0..8){
                    val cell = board.getCell(r,c)
                    cell.value = sudokuSolverBoard._values[r][c]
                }
            }
            Toast.makeText(context, "Sudoku solved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "There are no solution for this sudoku", Toast.LENGTH_SHORT).show()
        }
    }

    fun easyModOn(){
        for (r in 0..8) {
            for (c in 0..8) {
                val cell = board.getCell(r, c)
                sudokuSolverBoard._values[r][c] = cell.value
            }
        }
        sudokuSolverBoard.fillRestr()
        for (r in 0..8){
            for (c in 0..8){
                val cell = board.getCell(r,c)
                if (cell.value == 0){
                    val curRestriction = sudokuSolverBoard._restrictions[r][c]
                    val freeCount = curRestriction!!.GetFreeDigits()
                    cell.notes = freeCount.toMutableSet()
                }
            }
        }
    }

    fun easyModOff(){
        for (r in 0..8){
            for (c in 0..8){
                val cell = board.getCell(r,c)
                if (cell.value == 0){
                    cell.notes = mutableSetOf()
                }
            }
        }
    }

    fun vivod(){
        cellsLiveData.postValue(board.cells)
    }

    fun SolveOne(context: Context){
        for (r in 0..8){
            for (c in 0..8){
                val cell = board.getCell(r,c)
                sudokuSolverBoard._values[r][c] = cell.value
            }
        }
        if (sudokuSolverBoard.Solve()){
            val cell = board.getCell(selectedRow,selectedCol)
            cell.value = sudokuSolverBoard._values[selectedRow][selectedCol]
        } else {
            Toast.makeText(context, "There are no solution for this sudoku", Toast.LENGTH_SHORT).show()
        }
    }

    fun cleaner(){
        for(i in 0..80){
            val cell = board.getCell(i/9,i%9)
            cell.value=0
            board.cells[i].isStartingCell=false
            board.cells[i].isErrCell=false
        }
        vivod()
    }

    fun done(context: Context) {
        for (r in 0..8){
            for (c in 0..8){
                val cell = board.getCell(r,c)
                sudokuSolverBoard._values[r][c] = cell.value
            }
        }
        if (sudokuSolverBoard.Solve()){
            for (i in 0..80) {
                val cell = board.getCell(i / 9, i % 9)
                if (cell.value != 0) {
                    cell.isStartingCell = true
                }
            }
        } else {
            Toast.makeText(context, "There are no solution for this sudoku", Toast.LENGTH_SHORT).show()
        }
    }

    fun undone(){
        for(i in 0..80){
            val cell = board.getCell(i/9,i%9)
            if (cell.isStartingCell){
                cell.isStartingCell=false
            }
        }
        vivod()
    }

    fun generate(): Boolean{
        var empty: Boolean = false
        var emptyR: Int = 0
        var emptyC: Int = 0
        for(r in 0..8){
            for (c in 0..8){
                val cell = board.getCell(r, c)
                if (cell.value == 0){
                    empty = true
                    emptyR = cell.row
                    emptyC = cell.col
                    break
                }
            }
            if (empty){
                break
            }
        }
        if (!empty){
            return true
        }
        else {
            var l: MutableList<Int> = mutableListOf(1,2,3,4,5,6,7,8,9)
            l.shuffle()
            for(i in 0..8){
                if (valid(l[i], emptyR ,emptyC)){
                    val cell = board.getCell(emptyR, emptyC)
                    cell.value=l[i]

                    if(generate()){
                        return true
                    }
                    else{
                        cell.value = 0
                    }
                }

            }
        }
        return false
    }

    fun cleaneasy(){
        var l: MutableList<Int> = mutableListOf()
        for (i in 0..80){
            l.add(i)
            board.cells[i].isStartingCell=true
        }
        l.shuffle()
        for (i in 0..40){
            val cell = board.getCell(l[i]/9, l[i]%9)
            cell.value=0
            board.cells[l[i]].isStartingCell=false
        }
    }

    fun cleanmid(){
        var l: MutableList<Int> = mutableListOf()
        for (i in 0..80){
            l.add(i)
            board.cells[i].isStartingCell=true
        }
        l.shuffle()
        for (i in 0..50){
            val cell = board.getCell(l[i]/9, l[i]%9)
            cell.value=0
            board.cells[l[i]].isStartingCell=false
        }
    }

    fun cleanhard(){
        var l: MutableList<Int> = mutableListOf()
        for (i in 0..80){
            l.add(i)
            board.cells[i].isStartingCell=true
        }
        l.shuffle()
        for (i in 0..55){
            val cell = board.getCell(l[i]/9, l[i]%9)
            cell.value=0
            board.cells[l[i]].isStartingCell=false
        }
    }
    fun isWin(): Boolean{
        for (r in 0..8){
            for (c in 0..8){
                val cell = board.getCell(r, c)
                if (cell.value == 0 || cell.isErrCell){
                    return false
                }
            }
        }
        return true
    }
}