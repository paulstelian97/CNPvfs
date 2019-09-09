sealed class CNPVFSEntry {
    open val resolved: CNPVFSEntry? get() = this

    companion object {
        val root = RootVFS
        fun resolve(name: String): CNPVFSEntry? = root[name]
        fun resolve(entry: CNPVFSEntry?): CNPVFSEntry? =
            if (entry is SymLink) entry.resolved else entry
    }

    val type: String get() = when (this) {
        is Directory -> "Directory"
        is File -> "File"
        is SymLink -> "SymLink"
        else -> "Unknown"
    }
}

interface Directory {
    operator fun get(name: String): CNPVFSEntry?
    val entries: List<String> get
}

interface File {
    val contents: String get
}

interface SymLink {
    val target: String get
    val resolved: CNPVFSEntry? get() = CNPVFSEntry.resolve(target)
}

object RootVFS : CNPVFSEntry(), Directory {
    override val entries: List<String>
        get() {
            val result = mutableListOf<String>()
            // The root must support listing the normal objects
            for (i in 0..9)
                result.add(i.toString())
            return result
        }

    override fun get(name: String): CNPVFSEntry? {
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
}

class PartialDirectory(val dirName: String): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? {
        if (name !in entries)
            return null
        if (name == "name")
            return SimpleFile(dirName);
        // We've got another partialDirectory, we shall make a symlink
        return SimpleLink(dirName + name)
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
}

class FinalDirectory(cnp: String): CNPVFSEntry(), Directory {
    private val cnp: Cnp = Cnp(cnp)
    override fun get(name: String): CNPVFSEntry? {
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
        get() {
            val result = mutableListOf<String>()
            // Add a few classes
            result.add("valid")
            if (!cnp.valid)
                return result
            result.add("nationalitate")
            result.add("sex")
            result.add("an")
            result.add("luna")
            result.add("numeLuna")
            result.add("zi")
            result.add("judet")
            result.add("judetNumeric")
            result.add("nnn")
            result.add("name")
            // allow changes
            result.add("schimba")
            return result
        }
}

class SimpleFile(override val contents: String): CNPVFSEntry(), File
class SimpleLink(override val target: String): CNPVFSEntry(), SymLink {
    override val resolved get() = super<SymLink>.resolved
}

class ChangesList(val cnp: Cnp): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? {
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

    override val entries: List<String>
        get() {
            return listOf("nationalitate", "sex", "an", "luna", "zi", "judet", "nnn")
        }

}

class NationalitiesList(val cnp: Cnp): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? = if (name in entries) SimpleLink(cnp.altaNationalitate(name).name) else null
    override val entries: List<String> get() = cnp.nationalitatiValide
}
class SexList(val cnp: Cnp): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? = if (name in entries) SimpleLink(cnp.altSex(name).name) else null
    override val entries: List<String> get() = cnp.sexeValide
}
class YearList(val cnp: Cnp): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? = if (name in entries) get(name.toInt()) else null
    fun get(year: Int): CNPVFSEntry = SimpleLink(cnp.altAn(year).name)
    override val entries: List<String> get() = cnp.aniValizi.map (Int::toString)
}
class MonthList(val cnp: Cnp): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? = if (name in entries) get(name.toInt()) else null
    fun get(month: Int): CNPVFSEntry = SimpleLink(cnp.altaLuna(month).name)
    override val entries: List<String> get() = (1..12).map(Int::toString)
}
class DayList(val cnp: Cnp): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? = if (name in entries) get(name.toInt()) else null
    fun get(day: Int): CNPVFSEntry = SimpleLink(cnp.altaZi(day).name)
    override val entries: List<String> get() = cnp.zileValide.map(Int::toString)
}
class CountyList(val cnp: Cnp): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? = if (name in entries) SimpleLink(cnp.altJudetGeneral(name).name) else null
    override val entries: List<String> get() = cnp.judeteGeneraleValide
}
class NNNList(val cnp: Cnp): CNPVFSEntry(), Directory {
    override fun get(name: String): CNPVFSEntry? = if (name in entries) get(name.toInt()) else null
    fun get(nnn: Int): CNPVFSEntry = SimpleLink(cnp.altNnn(nnn).toString())
    override val entries: List<String> get() = (0..999).map { it.toString().padStart(3, '0') }
}

fun lsRecursive(dir: Directory, prefix: String = "") {
    for (entry in dir.entries) {
        val elem = dir[entry]
        val type = elem?.type ?: "<NOT FOUND>"
        val symlinklore = if (elem is SymLink) {
            val targetElem = elem.resolved
            val targetName = elem.target
            val targetType = targetElem?.type ?: "<NOT FOUND>"
            "(symlink to /$targetName: $targetType)"
        } else ""
        println("$prefix$entry: $type$symlinklore")
        if (elem is Directory)
            lsRecursive(elem, "$prefix\t")
        if (elem is File)
            println("$prefix\tFile contents: \"${elem.contents}\"")
    }
}

fun main() {
    val root = CNPVFSEntry.root
    val partial = root["100010101000"]
    if (partial == null) {
        println("/100010101000 not found")
        return
    }
    if (partial !is Directory) {
        println("/100010101000 is not a directory")
        return
    }
    val final = partial["6"]?.resolved
    if (final !is Directory) {
        println("/100010101000/6 is not a directory")
        return
    }
    println("Listing of /1000101010006: ${final.entries}")
    lsRecursive(final)
}