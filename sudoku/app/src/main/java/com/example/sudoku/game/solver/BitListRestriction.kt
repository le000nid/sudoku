class BitListRestriction(private var _restrictions: List<BitRestriction>) : IRestriction {

    override fun GetFreeDigits(): Set<Int> {
        var freeDigits = BitRestrictionHelper.AllDigits
        _restrictions.forEach {
            freeDigits = freeDigits and it.GetFreeDigits()
        }
        return BitRestrictionHelper.MaskToSet(freeDigits)
    }

    override fun Check(Digit: Int): Boolean {
        val u: UShort = 0u
        val DigitMask: UShort = (1 shl Digit).toUShort()
        _restrictions.forEach {
            if ((it.GetFreeDigits() and DigitMask) == u) {
                return false
            }
        }
        return true
    }

    override fun Remove(Digit: Int) {
        val DigitMask: UShort = (1 shl Digit).toUShort()
        _restrictions.forEach {
            it.Remove(DigitMask)
        }
    }

    override fun Add(Digit: Int) {
        val DigitMask: UShort = (1 shl Digit).toUShort()
        _restrictions.forEach {
            it.Add(DigitMask)
        }
    }

    override fun CountFreeDigits(): Int {
        var freeDigits = BitRestrictionHelper.AllDigits
        _restrictions.forEach {
            freeDigits = freeDigits and it.GetFreeDigits()
        }
        return BitRestrictionHelper.GetFreeDigitsCount(freeDigits)
    }

    override fun Clear() {
        _restrictions.forEach {
            it.Clear()
        }
    }
}