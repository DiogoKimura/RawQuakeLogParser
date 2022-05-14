class UserListStats() {
    private val userList = ArrayList<UserStats>()

    fun addUser(ind: Int, name: String){
        val user = UserStats(ind,name)
        userList.forEach {
            if (it.ind == user.ind){
                it.name = user.name
                return
            }
        }
        userList.add(user)
    }

    fun findUser(ind: Int) : UserStats?{
        userList.forEach {
            if (it.ind == ind)return it
        }

        return null
    }

    fun addKillToUser(ind: Int) = findUser(ind)?.addKill()

    fun addDeathToUser(ind: Int) = findUser(ind)?.addDeath()

    fun addSuicideToUser(ind: Int) = findUser(ind)?.addSuicide()

    fun getUserList() = userList

    fun clear() = userList.clear()
}

class UserStats (
    val ind: Int,
    var name: String,
    var kills: Int = 0,
    var deaths: Int = 0,
    var suicide: Int = 0
)
{
    fun addKill() = kills ++
    fun addDeath() = deaths ++
    fun addSuicide() = suicide ++
    fun getPoints() = kills - suicide
}