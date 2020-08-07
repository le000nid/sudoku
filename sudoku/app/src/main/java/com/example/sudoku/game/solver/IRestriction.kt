interface IRestriction {
    fun GetFreeDigits(): Set<Int>
    fun Check(Digit: Int): Boolean
    fun Remove(Digit: Int)
    fun Add(Digit: Int)
    fun CountFreeDigits(): Int
    fun Clear()
}