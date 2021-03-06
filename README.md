# Raw Quake Log Parser
This is a kotlin implementation to parse a log file retrived from a Quake 3 Server.

## How to use
Even being a test project, if you want to use it. Just install Intellij IDE, import this project and run it.

## Understanding the code
It's really a messy project, but I'll try to explain a little.

```kotlin
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
```
The main() will download the log's file (if it isn't downloaded), transform the file in a String list. The logKillsByGame() will loop the list and will do a specific parse with each game event.
<br><br>Every found match will generate an output. The log's match can be corrupted (without ShutDownGame event), in this case, the log will be output prematurely
<br>The output is a set of data:
- Server name
- Players with its index, name (last configured) and stats (kills, deaths, suicide and score(Kills - Suicide))
  - The suicide points is computed when the player kills itself (with a weapon or hurted by falling impact)
- Kill Mode points
- Total Kills