package com.example.sudoku.game

import android.graphics.Canvas
import androidx.lifecycle.MutableLiveData
import com.example.sudoku.view.custom.BoardView

class SudokuGame {
    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()

    private var selectedRow = -1
    private var selectedCol = -1

    private val board: Board

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

    fun solver(): Boolean{
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
        else{
            for(i in 1..9){
                if (valid(i, emptyR ,emptyC)){
                    val cell = board.getCell(emptyR, emptyC)
                    cell.value=i
                    if(solver()){
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

    fun vivod(){
        cellsLiveData.postValue(board.cells)
    }

    fun solveOne(){
        val l: List<Int> = emptyList()
        var ml = l.toMutableList()
        for(i in 0..8){
            for(j in 0..8){
                val cell = board.getCell(i, j)
                ml.add(cell.value)
            }
        }
        solver()
        for(i in 0..80){
            val cell = board.getCell(i/9, i%9)
            if (i/9 == selectedRow && i%9 == selectedCol){
                println("good")
            } else {
                cell.value = ml[i]
            }
        }
    }

    fun cleaner(){
        for(i in 0..80){
            val cell = board.getCell(i/9,i%9)
            cell.value=0
            board.cells[i].isStartingCell=false
        }
        cellsLiveData.postValue(board.cells)
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
}