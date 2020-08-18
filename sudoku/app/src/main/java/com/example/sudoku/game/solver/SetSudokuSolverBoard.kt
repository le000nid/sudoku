import java.util.ArrayList

class SudokuSolverBoard {
    val _values = Array(9) {IntArray(9) {0} }
    val _restrictions = Array(9) { arrayOfNulls<IRestriction>(9) }
    private val _columnRestrictions = arrayOfNulls<BitRestriction>(9)
    private val _rawRestrictions = arrayOfNulls<BitRestriction>(9)
    private val _squareRestrictions = arrayOfNulls<BitRestriction>(9)
    init{
        for (i in 0..8) {
            _columnRestrictions[i] = BitRestriction()
            _rawRestrictions[i] = BitRestriction()
            _squareRestrictions[i] = BitRestriction()
        }
        for (r in 0..8) {
            for (c in 0..8) {
                val cellRestrictions = ArrayList<BitRestriction>(listOf(_rawRestrictions[r], _columnRestrictions[c], _squareRestrictions[r / 3 * 3 + c / 3]))
                _restrictions[r][c] = BitListRestriction(cellRestrictions)
            }
        }
    }
    fun Solve(): Boolean {
        for (i in 0..8) {
            _columnRestrictions[i]!!.Clear()
            _rawRestrictions[i]!!.Clear()
            _squareRestrictions[i]!!.Clear()
        }
        var remain = 0
        for (r in 0..8) {
            for (c in 0..8)
            {
                if (_values[r][c] == 0){
                    remain++
                } else {
                    _restrictions[r][c]!!.Remove(_values[r][c])
                }
            }
        }
        return solve(remain)
    }
    private fun solve(remain: Int): Boolean {
        var restriction: IRestriction? = null
        var bestR = 0
        var bestC = 0
        var freeCountMin = 10
        for (r in 0..8) {
            for (c in 0..8) {
                val v = _values[r][c]
                if (v == 0) {
                    val curRestriction = _restrictions[r][c]
                    val freeCount = curRestriction!!.CountFreeDigits()
                    if (freeCount < freeCountMin) {
                        freeCountMin = freeCount
                        restriction = curRestriction
                        bestR = r
                        bestC = c
                    }
                }
            }
        }
        val freeDigits = restriction?.GetFreeDigits()
        freeDigits!!.forEach {
            _values[bestR][bestC] = it
            restriction?.Remove(it)
            if (remain == 1) {
                return true
            }
            val res: Boolean = solve(remain-1)
            if (!res) {
                restriction?.Add(it)
            } else {
                return true
            }
        }
        _values[bestR][bestC] = 0
        return false
    }
    fun setValue(r: Int, c: Int, Digit: Int){
        _values[r][c]=Digit
    }
    fun fillRestr(){
        for (i in 0..8) {
            _columnRestrictions[i]!!.Clear()
            _rawRestrictions[i]!!.Clear()
            _squareRestrictions[i]!!.Clear()
        }
        for (r in 0..8) {
            for (c in 0..8)
            {
                if (_values[r][c] != 0){
                    _restrictions[r][c]!!.Remove(_values[r][c])
                }
            }
        }
    }
}