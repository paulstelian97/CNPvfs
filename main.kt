interface VFSEntry {
    companion object {
        val cnpRoot = CNPRootVFS
    }

    val type: String get() = when (this) {
        is Directory -> "Directory"
        is File -> "File"
        is SymLink -> "SymLink"
        else -> "Unknown"
    }
}

interface Directory: VFSEntry {
    operator fun get(name: String): VFSEntry?
    val entries: List<String>
}

interface File: VFSEntry {
    val contents: String
}

interface SymLink: VFSEntry {
    val target: String
}

class DirectoryWithParent(private val parentDirectory: DirectoryWithParent?, private val curDir: Directory, private val name: String) {
    operator fun get(name: String): DirectoryWithParent? {
        val child = curDir[name] as? Directory
        return when (name) {
            "", "." -> this
            ".." -> parentDirectory
            else -> DirectoryWithParent(this, child ?: return null, name)
        }
    }
    operator fun invoke(): Directory = curDir
    override fun toString(): String = if (parentDirectory != null) "$parentDirectory/$name" else "/$name"
    companion object {
        val cnpRoot = DirectoryWithParent(null, VFSEntry.cnpRoot, "")
    }
}

inline class DirectoryWithSymlinkResolution(private val underlying: DirectoryWithParent) {
    operator fun get(name: String): DirectoryWithSymlinkResolution? {
        // We only resolve to a directory so this is fine
        if (name == "")
            return this
        if (!name.contains('/')) {
            when (name) {
                // Simpler cases
                "", "." -> return this
                // ".." -> works fine with below case
            }
            // Simple resolution, just resolve one path entry
            val child = if (name == "..") this()[name]!!() else this()()[name] ?: return null
            if (child is Directory) {
                return DirectoryWithSymlinkResolution(underlying[name]!!)
            }
            if (child is SymLink) {
                val result = this[child.target] ?: return null
                // It's a directory but with a wrong parent now
                val unpackedResult = result()()
                val reparentedResult = DirectoryWithParent(this(), unpackedResult, name)
                return DirectoryWithSymlinkResolution(reparentedResult)
            }
            return null
        }
        if (name[0] == '/')
            return cnpRoot[name.substring(1)]
        // Parse the first component up to the target
        val currentName = name.substringBefore("/")
        val remainder = name.substringAfter("/")
        val currentResolved = this[currentName] ?: return null
        return currentResolved[remainder]
    }
    operator fun invoke() = underlying
    override fun toString(): String = underlying.toString()
    companion object {
        val cnpRoot = DirectoryWithSymlinkResolution(DirectoryWithParent.cnpRoot)
    }
}

fun interactiveListCurrent(curDir: DirectoryWithSymlinkResolution) {
    // Must get all elements in here
    for (file in curDir()().entries)
        println(file)
}
fun interactiveListChild(curDir: DirectoryWithSymlinkResolution, target: String) {
    val child = curDir()()[target]
    if (child == null) {
        println("Error: No such file or directory $target")
        return
    }
    if (child !is Directory) {
        println(target)
        return
    }
    // Since we know that the child is a directory, we can just do the below
    interactiveListCurrent(curDir[target]!!)
}
fun interactivePrintFile(curDir: DirectoryWithSymlinkResolution, target: String) {
    when (val entry = curDir()()[target]) {
        null -> println("Error: No such file or directory $target")
        !is File -> println("Error: $target is not a file")
        else -> println(entry.contents)
    }
}

fun main() {
    var curDir = DirectoryWithSymlinkResolution.cnpRoot
    val commandRegex = Regex("""\s*(ls|cd|cat)\s+(.*)""")

    interactiveLoop@ while (true) {
        print("> ")
        val command = readLine() ?: return
        when (command) {
            "" -> continue@interactiveLoop
            "exit", "quit" -> return
            "ls" -> {interactiveListCurrent(curDir); continue@interactiveLoop}
            "pwd" -> {
                println(curDir)
                continue@interactiveLoop
            }
        }
        val match = commandRegex.matchEntire(command)
        if (match == null) {
            println("Invalid command!")
            continue@interactiveLoop
        }
        val (commandName, argument) = match.destructured
        when (commandName) {
            "ls" -> interactiveListChild(curDir, argument)
            "cd" -> {
                val target = curDir[argument]
                if (target == null) {
                    println("Cannot change to directory $argument")
                } else {
                    curDir = target
                }
            }
            "cat" -> interactivePrintFile(curDir, argument)
        }
    }
}