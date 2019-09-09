class Cnp(val name: String) {
    private val S: Int get() = name[0].toString().toInt()
    private val AA: Int get() = sAA.toInt()
    private val sAA: String get() = name.substring(1, 3)
    private val LL: Int get() = sLL.toInt()
    private val sLL: String get() = name.substring(3, 5)
    private val ZZ: Int get() = sZZ.toInt()
    private val sZZ: String get() = name.substring(5, 7)
    private val JJ: Int get() = sJJ.toInt()
    private val sJJ: String get() = name.substring(7, 9)
    private val NNN: Int get() = sNNN.toInt(10) // To ensure no unintended octal happens here
    private val sNNN: String get() = name.substring(9, 12)
    private val C: Int get() = name[12].toString().toInt()

    private val correctC: Int get() {
        var result = 0
        result += 2 * S
        result += 7 * (AA/10) + 9 * (AA%10)
        result += (LL/10) + 4 * (LL%10)
        result += 6 * (ZZ/10) + 3 * (ZZ%10)
        result += 5 * (JJ/10) + 8 * (JJ%10)
        result += 2 * (NNN/100) + 7 * (NNN/10%10) + 9 * (NNN%10)
        result %= 11
        if (result == 10) result = 1
        return result
    }

    private fun setS(newS: Int): Cnp {
        // Compose with the new S
        if (newS !in 0..9)
            return this
        val tmp = Cnp("$newS$sAA$sLL$sZZ$sJJ$sNNN$C")
        // Get the proper check digit
        val correctC = tmp.correctC
        return Cnp("$newS$sAA$sLL$sZZ$sJJ$sNNN$correctC")
    }
    private fun setAA(newAA: Int): Cnp {
        val sAA = newAA.toString().padStart(2, '0')
        val tmp = Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$C")
        val correctC = tmp.correctC
        return Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$correctC")
    }
    private fun setLL(newLL: Int): Cnp {
        val sLL = newLL.toString().padStart(2, '0')
        val tmp = Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$C")
        val correctC = tmp.correctC
        return Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$correctC")
    }
    private fun setZZ(newZZ: Int): Cnp {
        val sZZ = newZZ.toString().padStart(2, '0')
        val tmp = Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$C")
        val correctC = tmp.correctC
        return Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$correctC")
    }
    private fun setJJ(newJJ: Int): Cnp {
        val sJJ = newJJ.toString().padStart(2, '0')
        val tmp = Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$C")
        val correctC = tmp.correctC
        return Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$correctC")
    }
    private fun setNNN(newNNN: Int): Cnp {
        val sNNN = newNNN.toString().padStart(3, '0')
        val tmp = Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$C")
        val correctC = tmp.correctC
        return Cnp("$S$sAA$sLL$sZZ$sJJ$sNNN$correctC")
    }

    val valid: Boolean get() {
        if (name.length != 13)
            return false
        if (!name.all(Char::isDigit))
            return false
        // Checkdigit
        if (C != correctC)
            return false
        // Valid S
        if (S !in 1..9)
            return false
        // Valid month
        if (LL !in 1..12)
            return false
        if (ZZ !in zileValide)
            return false
        // Valid county
        if (JJ !in 1..46 && JJ !in 51..52)
            return false
        return true
    }
    val nationalitate: String get() {
        return when (S) {
            in 1..6 -> "Romana"
            in 7..8 -> "Rezidenta"
            9 -> "Straina"
            else -> "?!?!"
        }
    }
    fun altaNationalitate(target: String): Cnp {
        return when (target) {
            "Romana" -> when (S) {
                in 1..6 -> this
                7, 9 -> this.setS(1)
                8 -> this.setS(2)
                else -> this.setS(1)
            }
            "Rezidenta" -> when (S) {
                7, 8 -> this
                1, 9 -> this.setS(7)
                2 -> this.setS(8)
                else -> this.setS(7)
            }
            "Straina" -> this.setS(9)
            else -> this
        }
    }
    val nationalitatiValide: List<String> get() {
        return when (S) {
            1, 2, 7, 8, 9 -> listOf("Romana", "Rezidenta", "Straina")
            else -> listOf("Romana")
        }
    }
    val sex: String get() {
        return when (S) {
            1, 3, 5, 7 -> "Masculin"
            2, 4, 6, 8 -> "Feminin"
            9 -> "Necunoscut"
            else -> "??!!"
        }
    }
    val sexeValide: List<String> get() = if (S == 9) listOf("Necunoscut") else listOf("Masculin", "Feminin")
    fun altSex(target: String): Cnp {
        return when (target) {
            "Masculin" -> when (S) {
                1, 3, 5, 7 -> this
                2 -> setS(1)
                4 -> setS(3)
                6 -> setS(5)
                8 -> setS(7)
                else -> setS(1)
            }
            "Feminin" -> when (S) {
                2, 4, 6, 8 -> this
                1 -> setS(2)
                3 -> setS(4)
                5 -> setS(6)
                7 -> setS(8)
                else -> setS(2)
            }
            "Necunoscut" -> if (S == 9) this else setS(9)
            else -> this
        }
    }
    val an: Int get() {
        return when (S) {
            1, 2, 7, 8, 9 -> 1900 + AA
            3, 4 -> 1800 + AA
            5, 6 -> 2000 + AA
            else -> 0
        }
    }
    val aniValizi: List<Int> get() {
        return when (S) {
            in 1..6 -> (1800..2099).toList()
            in 7..9 -> (1900..1999).toList()
            else -> listOf()
        }
    }
    fun altAn(target: Int): Cnp {
        // We also need to update S depending on year
        if (an / 100 == target / 100)
            return setAA(target % 100)
        val newS = when (S) {
            1, 3, 5 -> when (target / 100) {
                18 -> 3
                19 -> 1
                20 -> 5
                else -> S
            }
            2, 4, 6 -> when (target / 100) {
                18 -> 4
                19 -> 2
                20 -> 6
                else -> S
            }
            else -> S
        }
        return setS(newS).setAA(target % 100)
    }
    val luna: Int get() = LL
    val numeLuna: String get() = listOf(
        "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie",
        "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"
    )[LL - 1]
    fun altaLuna(target: Int): Cnp = setLL(target)
    val zi: Int get() = ZZ
    val zileValide: List<Int> get() {
        // Valid day, more complex
        val bisect = AA % 4 == 0 && AA != 0 || (AA == 0 && S in listOf(5, 6))
        val days = when(LL) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (bisect) 29 else 28
            else -> -1
        }
        return if (days > 0) (1..days).toList() else listOf()
    }
    fun altaZi(target: Int): Cnp = setZZ(target)
    val judet: String get() = judete[JJ]!!
    val judeteValide: List<String> get() = judete.map {it.value}
    fun altJudet(target: String): Cnp = altJudet(judeteInv[target]!!)
    val judetNumeric: Int get() = JJ
    val judeteNumericeValide: List<Int> get() = (1..46).toList() + listOf(51, 52)
    fun altJudet(target: Int): Cnp = setJJ(target)
    val judeteGeneraleValide: List<String> get() = judeteNumericeValide.map(Int::toString) + judeteValide
    fun altJudetGeneral(target: String): Cnp {
        val judetNumeric: Int? = target.toIntOrNull()
        return if (judetNumeric == null)
            altJudet(target)
        else
            altJudet(judetNumeric)
    }
    val nnn: Int get() = NNN
    fun altNnn(target: Int) = setNNN(target)

    override fun toString(): String = name
}

private val judete = mapOf(
    1 to "Alba", 2 to "Arad", 3 to "Arges", 4 to "Bacau", 5 to "Bihor", 6 to "Bistrita-Nasaud", 7 to "Botosani",
    8 to "Brasov", 9 to "Braila", 10 to "Buzau", 11 to "Caras-Severin", 12 to "Cluj", 13 to "Constanta",
    14 to "Covasna",15 to "Dambovita", 16 to "Dolj", 17 to "Galati", 18 to "Gorj", 19 to "Harghita", 20 to "Hunedoara",
    21 to "Ialomita", 22 to "Iasi", 23 to "Ilfov", 24 to "Maramures", 25 to "Mehedinti", 26 to "Mures", 27 to "Neamt",
    28 to "Olt", 29 to "Prahova", 30 to "Satu Mare", 31 to "Salaj", 32 to "Sibiu", 33 to "Suceava", 34 to "Teleorman",
    35 to "Timis", 36 to "Tulcea", 37 to "Vaslui", 38 to "Valcea", 39 to "Vrancea", 40 to "Bucuresti",
    41 to "Bucuresti, sector 1", 42 to "Bucuresti, sector 2", 43 to "Bucuresti, sector 3", 44 to "Bucuresti, sector 4",
    45 to "Bucuresti, sector 5", 46 to "Bucuresti, sector 6", 51 to "Calarasi", 52 to "Giurgiu"
)
private val judeteInv = judete.map {it.value to it.key}.toMap()