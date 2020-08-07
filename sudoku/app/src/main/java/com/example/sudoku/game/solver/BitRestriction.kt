object BitRestrictionHelper {
    const val AllDigits: UShort = 0x3FEu; // 9 единиц и 0, единицы начинаем 1го бита

    fun GetFreeDigitsCount(bitMask: UShort) : Int
    {
        var c:UInt = bitMask.toUInt()
        c -= ((c shr 1) and 0x5555u)
        c = ((c shr 2) and 0x3333u) + (c and 0x3333u)
        c = ((c shr 4) + c) and 0xF0F0Fu
        c = ((c shr 8) + c) and 0x00FFu
        return c.toInt()
    }

    fun MaskToSet(bitMask: UShort): Set<Int>
    {
        val s = mutableSetOf<Int>()
        val v:UInt = bitMask.toUInt()
        for (i in 1..9)
        {
            val m = 1u shl i
            if ((m and v) != 0u)
                s.add(i)
        }
        return s
    }
}

class BitRestriction {


    private var freeDigits = BitRestrictionHelper.AllDigits

    init {
        Clear()
    }

    fun GetFreeDigits(): UShort {
        return freeDigits
    }

    fun Remove(DigitMask: UShort) {
        freeDigits = freeDigits and DigitMask.inv()
    }

    fun Add(DigitMask: UShort) {
        freeDigits = freeDigits or DigitMask;
    }

    fun CountFreeDigits(): Int {
        return BitRestrictionHelper.GetFreeDigitsCount(freeDigits)
    }

    fun Clear() {
        freeDigits = BitRestrictionHelper.AllDigits
    }
}

