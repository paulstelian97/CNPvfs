object CNPRootVFS : Directory {
    override val entries: List<String>
        get() {
            val result = mutableListOf<String>()
            // The root must support listing the normal objects
            for (i in 0..9)
                result.add(i.toString())
            return result
        }

    override fun get(name: String): VFSEntry? {
        // If the name is up to 12 characters and they're all digits, return a partialDirectory
        if (name.length in 1..12 && name.all (Char::isDigit)) {
            return PartialDirectory(name)
        }
        // If the name is exactly 13 characters and they're all digits, return a finalDirectory
        if (name.length == 13 && name.all (Char::isDigit))
            return FinalDirectory(name)
        // If it's neither, just bail
        return null
    }

    override fun toString(): String = "/"
}

class PartialDirectory(private val dirName: String): Directory {
    override fun get(name: String): VFSEntry? {
        if (name !in entries)
            return null
        if (name == "name")
            return SimpleFile(dirName)
        // We've got another partialDirectory, we shall make a symlink
        return SimpleLink("/$dirName$name")
    }

    override val entries: List<String>
        get() {
            val result = mutableListOf<String>()
            // Add the first result, "name"
            result.add("name")
            // Add symlinks to more-complete (by one digit) elements
            for (i in 0..9)
                result.add(i.toString())
            return result
        }

    override fun toString(): String = "/$dirName"
}

class FinalDirectory(cnp: String): Directory {
    private val cnp: Cnp = Cnp(cnp)
    override fun get(name: String): VFSEntry? {
        if (name !in entries)
            return null
        return when (name) {
            "valid" -> SimpleFile(if (cnp.valid) "da" else "nu")
            "nationalitate" -> SimpleFile(cnp.nationalitate)
            "sex" -> SimpleFile(cnp.sex)
            "an" -> SimpleFile(cnp.an.toString())
            "luna" -> SimpleFile(cnp.luna.toString())
            "numeLuna" -> SimpleFile(cnp.numeLuna)
            "zi" -> SimpleFile(cnp.zi.toString())
            "judet" -> SimpleFile(cnp.judet)
            "judetNumeric" -> SimpleFile(cnp.judetNumeric.toString())
            "nnn" -> SimpleFile(cnp.nnn.toString())
            "name" -> SimpleFile(cnp.name)
            "schimba" -> ChangesList(cnp)
            else -> null
        }
    }

    override val entries: List<String>
        get() = if (cnp.valid)
            listOf("valid", "nationalitate", "sex", "an", "luna", "numeLuna", "zi", "judet", "judetNumeric",
                "nnn", "name", "schimba")
        else
            listOf("valid")

    override fun toString(): String = "/$cnp"
}

class SimpleFile(override val contents: String): File {
    override fun toString(): String = contents
}
class SimpleLink(override val target: String): SymLink {
    override fun toString(): String = "[Symlink -> $target]"
}

class ChangesList(private val cnp: Cnp): VFSEntry, Directory {
    override fun get(name: String): VFSEntry? {
        return when (name) {
            "nationalitate" -> NationalitiesList(cnp)
            "sex" -> SexList(cnp)
            "an" -> YearList(cnp)
            "luna" -> MonthList(cnp)
            "zi" -> DayList(cnp)
            "judet" -> CountyList(cnp)
            "nnn" -> NNNList(cnp)
            else -> null
        }
    }

    override val entries: List<String> = listOf("nationalitate", "sex", "an", "luna", "zi", "judet", "nnn")

    override fun toString(): String = "/$cnp/schimba"
}

class NationalitiesList(private val cnp: Cnp): Directory {
    override fun get(name: String): VFSEntry? = if (name in entries) SimpleLink("/" + cnp.altaNationalitate(name).name) else null
    override val entries: List<String> get() = cnp.nationalitatiValide
    override fun toString(): String = "/$cnp/schimba/nationalitate"
}
class SexList(private val cnp: Cnp): Directory {
    override fun get(name: String): VFSEntry? = if (name in entries) SimpleLink("/" + cnp.altSex(name).name) else null
    override val entries: List<String> get() = cnp.sexeValide
    override fun toString(): String = "/$cnp/schimba/sex"
}
class YearList(private val cnp: Cnp): Directory {
    override fun get(name: String): VFSEntry? = if (name in entries) get(name.toInt()) else null
    fun get(year: Int): VFSEntry = SimpleLink("/" + cnp.altAn(year).name)
    override val entries: List<String> get() = cnp.aniValizi.map (Int::toString)
    override fun toString(): String = "/$cnp/schimba/an"
}
class MonthList(private val cnp: Cnp): Directory {
    override fun get(name: String): VFSEntry? = if (name in entries) get(name.toInt()) else null
    fun get(month: Int): VFSEntry = SimpleLink("/" + cnp.altaLuna(month).name)
    override val entries: List<String> = (1..12).map(Int::toString)
    override fun toString(): String = "/$cnp/schimba/luna"
}
class DayList(private val cnp: Cnp): Directory {
    override fun get(name: String): VFSEntry? = if (name in entries) get(name.toInt()) else null
    fun get(day: Int): VFSEntry = SimpleLink("/" + cnp.altaZi(day).name)
    override val entries: List<String> get() = cnp.zileValide.map(Int::toString)
    override fun toString(): String = "/$cnp/schimba/zi"
}
class CountyList(private val cnp: Cnp): Directory {
    override fun get(name: String): VFSEntry? = if (name in entries) SimpleLink("/" + cnp.altJudetGeneral(name).name) else null
    override val entries: List<String> = cnp.judeteGeneraleValide
    override fun toString(): String = "/$cnp/schimba/judet"
}
class NNNList(private val cnp: Cnp): Directory {
    override fun get(name: String): VFSEntry? = if (name in entries) get(name.toInt()) else null
    fun get(nnn: Int): VFSEntry = SimpleLink("/" + cnp.altNnn(nnn).toString())
    override val entries: List<String> = (0..999).map { it.toString().padStart(3, '0') }
    override fun toString(): String = "/$cnp/schimba/nnn"
}