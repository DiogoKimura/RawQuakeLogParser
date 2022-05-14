import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths


private var lines: List<String>? = null
private const val FILENAME = "qgames.log"


private const val INITGAME = "InitGame: "
private const val ENDGAME = "ShutdownGame:"
private const val EXITGAME = "Exit: "

private const val KILLEVENT = "Kill: "
private const val CLICONEVENT = "ClientConnect: "
private const val CLIINFEVENT = "ClientUserinfoChanged: "

// Init Game Tag Parser
private const val HOSTNAMETAG = "\\sx_hostname\\"

// Client Info Tag Parser
private const val NAMETAG = "n\\"

private const val WORLD = "1022"

// Regex
private const val REG_KILL_EVENT = "$KILLEVENT(\\d+) (\\d+) (\\d+)"
private const val REG_SERVER_NAME = """\\sv_hostname\\(.*?)\\"""
private const val REG_USER_INDEX = "$CLIINFEVENT(\\d+)"
private const val REG_USER_NAME = """n\\(.*?)\\"""

enum class KillMod {
    MOD_UNKNOWN,
    MOD_SHOTGUN,
    MOD_GAUNTLET,
    MOD_MACHINEGUN,
    MOD_GRENADE,
    MOD_GRENADE_SPLASH,
    MOD_ROCKET,
    MOD_ROCKET_SPLASH,
    MOD_PLASMA,
    MOD_PLASMA_SPLASH,
    MOD_RAILGUN,
    MOD_LIGHTNING,
    MOD_BFG,
    MOD_BFG_SPLASH,
    MOD_WATER,
    MOD_SLIME,
    MOD_LAVA,
    MOD_CRUSH,
    MOD_TELEFRAG,
    MOD_FALLING,
    MOD_SUICIDE,
    MOD_TARGET_LASER,
    MOD_TRIGGER_HURT,
    MOD_GRAPPLE
}

fun main(args: Array<String>) {
    try {
        lines = fileLinesToList(FILENAME)
    } catch (e: java.lang.Exception) {
        downloadQuakeLog()
    } finally {
        lines = fileLinesToList(FILENAME)
    }

    logKillsByGame()
}

// GAMES ANALYSIS
fun countTags(tag: String): Int {
    var counter = 0
    lines?.forEach {
        if (it.contains(tag, false)) counter++
    }
    return counter
}

fun logKillsByGame() {
    var killCounter = 0
    var index = 0
    var isGameBegin = false
    val modKills = HashMap<Int, Int>()
    val userListStats = UserListStats()
    lines?.forEach {
        when {
            it.contains(INITGAME) -> {
                if (isGameBegin) {
                    // This match log is corrupted
                    println("This log's match is corrupted")
                    userListStats.getUserList().forEach {
                        println("Player ${it.ind} ${it.name} - " +
                                "Kills: ${it.kills}/ " +
                                "Deaths: ${it.deaths} / " +
                                "Suicide: ${it.suicide} / " +
                                "Points: ${it.getPoints()}")
                    }
                    modKills.forEach { kill ->
                        println("${KillMod.values()[kill.key]} : ${kill.value}")
                    }
                    println("We could only count: $killCounter Kills")
                    killCounter = 0
                    isGameBegin = false
                    index++
                    userListStats.clear()
                    modKills.clear()
                }
                isGameBegin = true
                println("|===========================================================================|")
                println("Game [$index] has started")

                println("Server name: " + getTagSingleValue(it, REG_SERVER_NAME))
            }
            it.contains(CLIINFEVENT) -> {
                val ind = getTagSingleValue(it, REG_USER_INDEX).toInt()
                val name = getTagSingleValue(it, REG_USER_NAME)
                userListStats.addUser(ind, name)
            }
            it.contains(KILLEVENT) -> {
                killCounter++

                val (killer, killed, mod) = getMatchResult(it, REG_KILL_EVENT)!!.destructured

                if (killer == WORLD || killer == killed) userListStats.addSuicideToUser(killed.toInt())
                else {
                    userListStats.addKillToUser(killer.toInt())
                    userListStats.addDeathToUser(killed.toInt())
                }

                if (modKills.containsKey(mod.toInt())){
                    modKills[mod.toInt()] = modKills[mod.toInt()]!! + 1
                } else {
                    modKills[mod.toInt()] = 1
                }
            }
            it.contains(ENDGAME) -> {
                userListStats.getUserList().forEach {
                    println("Player ${it.ind} ${it.name} - " +
                            "Kills: ${it.kills}/ " +
                            "Deaths: ${it.deaths} / " +
                            "Suicide: ${it.suicide} / " +
                            "Points: ${it.getPoints()}")
                }
                modKills.forEach { kill ->
                    println("${KillMod.values()[kill.key]} : ${kill.value}")
                }
                println("Games has ended with: $killCounter Kills")
                println("|===========================================================================|")
                killCounter = 0
                isGameBegin = false
                index++
                userListStats.clear()
                modKills.clear()
            }
        }
    }
}

fun getMatchResult(log: String, regex: String) = regex.toRegex().find(log)

fun parseKillInformation(log: String) {
    val (killer, killed, mod) = getMatchResult(log, REG_KILL_EVENT)!!.destructured
    println("$killer killed $killed with $mod")
}

fun parseInitGameInformation(log: String) {
    val ind = log.indexOf(HOSTNAMETAG)
    println(ind)
}

@Deprecated("dont use")
fun getTagValue(log: String, tag: String, pattern: String = "\\"): String {
    val indStartValue = log.indexOf(tag) + tag.length
    val indEndValue = log.indexOf(pattern, indStartValue) - 1
    val resultString = log.substring(indStartValue..indEndValue)
    //println(resultString)

    return resultString
}

fun getTagSingleValue(log: String, regex: String): String {
    return getMatchResult(log, regex)!!.destructured.component1()
}

// FILES
fun fileLinesToList(fileName: String): List<String> = File(fileName).useLines { it.toList() }

fun readFile(fileName: String) = File(fileName).readText()

fun downloadQuakeLog() {
    downloadFile(
        URL("https://gist.githubusercontent.com/cloudwalk-tests/be1b636e58abff14088c8b5309f575d8/raw/df6ef4a9c0b326ce3760233ef24ae8bfa8e33940/"),
        "qgames.log"
    )
}

fun downloadFile(url: URL, fileName: String) {
    url.openStream().use {
        Files.copy(it, Paths.get(fileName))
    }
}