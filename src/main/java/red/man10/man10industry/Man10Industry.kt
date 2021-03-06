package red.man10.man10industry

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import red.man10.man10industry.models.*
import java.util.*
import kotlin.collections.HashMap

typealias PlayerSkillData = MutableMap<Int, Int>

class Man10Industry : JavaPlugin() {

    val prefix = "§a[§bm§3Industry§a] §b"

    val listener = MIListener(this)
    val db = MIDatabase(this)
    val config = MIConfig(this)
    val util = MIUtility(this)
    val gui = MIGUI(this)
    val skill = MISkillData(this)
    val chat = MIChat(this)
    val machine = MIMachine(this)

    var chanceSets = HashMap<String, ChanceSet>()
    var recipies = HashMap<String, Recipe>()
    var skills = mutableListOf<Skill>()
    var machines = HashMap<String, Machine>()

    val player_slimit = HashMap<UUID, Int>()

    //var mysql: MySQLManager? = null

    var isLocked = false

    override fun onEnable() {

        //val mysql = MySQLManager(this, "MI_ConnectionTest")

//        if (!(mysql.connected)) {
//            isLocked = true
//            broadcastMessage(prefix + "§aDatabase Error - §fLocking mIndustry")
//            //return
//        }

        server.pluginManager.registerEvents(listener, this)
        server.pluginManager.registerEvents(gui, this)

        saveDefaultConfig()

        config.loadAll(Bukkit.getConsoleSender(), true)
        val get = getPlayerData(this)
        get.start()
        for (player in Bukkit.getOnlinePlayers()) {
            skill.currentPlayerData.put(player.uniqueId, mutableMapOf())
        }

        val timer = Timer()
        timer.schedule(setData(this), 60000)

        val create = createTable(this)
        create.start()

    }

    override fun onDisable() {
        // Plugin shutdown logic
        val set = setPlayerData(this)
        set.start()
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (isLocked) {
                sender.sendMessage(prefix + "§aPlugin Locked")
                if (sender.hasPermission("mi.op")){
                    sender.sendMessage(prefix + "(Bypassing)")
                } else {
                    return true
                }
            }
            if (sender.hasPermission("mi.use")){
                when (args.size) {
                    0 -> {
                        chat.showTopMenu(sender)
                        return true
                    }
                    1 -> {
                        when (args[0]) {
                            "myskill" -> {

                                sender.sendMessage(player_slimit.toString())

                                if (!skill.currentPlayerData.containsKey(sender.uniqueId) || skill.currentPlayerData[sender.uniqueId] == null || skill.currentPlayerData[sender.uniqueId]!!.isEmpty()) {
                                    val p = mutableMapOf<Int, Int>()

                                    for(i in 0 until skills.size){
                                        p[i+1] = 0
                                    }
                                    skill.currentPlayerData.put(sender.uniqueId, p)
                                }

                                if (!player_slimit.containsKey(sender.uniqueId) || player_slimit[sender.uniqueId] == null){
                                    player_slimit[sender.uniqueId] = 500
                                }

                                var skillId = 1
                                for (skill in skills) {
                                    when (skillId) {
                                        1 -> {
                                            sender.sendMessage(prefix + "§e加工スキル:")
                                        }
                                        5 -> {
                                            sender.sendMessage(prefix + "§e魔法スキル:")
                                        }
                                        9 -> {
                                            sender.sendMessage(prefix + "§e学問スキル:")
                                        }
                                        13 -> {
                                            sender.sendMessage(prefix + "§eスペシャルスキル:")
                                        }
                                    }
                                    val skillArrow = "§8 " + "〉".repeat(7 - skill.name.length)
                                    val level = this.skill.currentPlayerData[sender.uniqueId]!![skillId]!!
                                    sender.sendMessage(prefix +
                                            "§b " + skill.name +
                                            skillArrow +
                                            "§a " + String.format("%3s", level.toString()) + "Lv " +
                                            chat.returnProgressBar((level.toDouble()/100)))
                                    //sender.sendMessage(prefix + "§b" + skill.name + " §a" + currentPlayerData[sender.uniqueId]!![skillId] + "Lv - " + chat.returnProgressBar(0.3))
                                    //sender.sendMessage(prefix + "§b" + chat.returnProgressBar(0.3) + " §b" + skill.name + " §a" + currentPlayerData[sender.uniqueId]!![skillId] + "Lv ")

                                    skillId++
                                }
                                return true
                            }
                        }
                    }
                }
            }
            if (sender.hasPermission("mi.op")) {
                when (args.size) {
                    0 -> {
                    }
                    1 -> {
                        when (args[0]) {
                            "help" -> {
                                showHelp(sender)
                                return true
                            }
                            "reload" -> {
                                config.loadAll(sender, false)
                                return true
                            }
                            "list" -> {
                                showList(sender)
                                return true
                            }
                            "save" ->{
                                val set = setPlayerData(this)
                                set.start()
                                return true
                            }
                        }
                    }
                    2 -> {
                        when (args[0]) {
                            "usemachine" -> {
                                gui.openProcessingView(sender, args[1])
                                return true
                            }
                            "setinput" -> {
                                if (recipies[args[1]] != null) {
                                    gui.openInputSetView(sender, args[1])
                                } else {
                                    sender.sendMessage(prefix + "Recipe doesn't exist.")
                                }
                                return true
                            }
                            "setoutput" -> {
                                if (recipies[args[1]] != null) {
                                    gui.openOutputSetView(sender, args[1])
                                } else {
                                    sender.sendMessage(prefix + "Recipe doesn't exist.")
                                }
                                return true
                            }
                            "update" -> {
                                skill.currentPlayerData[Bukkit.getPlayer(args[1]).uniqueId]!!.clear()
                            }
                            "get" -> {
                                if (machines[args[1]] != null) {
                                    sender.inventory.addItem(machine.createMapItem(args[1]))
                                    return true
                                }
                            }
                        }
                    }
                    3 -> {
                        when(args[0]) {
                            "info" -> {
                                when (args[1]) {
                                    "c" -> {
                                        if (chanceSets[args[2]]  != null) {
                                            sender.sendMessage(prefix + "§bData of ChanceSet: " + args[2])
                                            sender.sendMessage(prefix + "§7" + chanceSets[args[2]])
                                        } else {
                                            sender.sendMessage(prefix + "§bChanceSet: " + args[2] + " doesn't exist")
                                        }
                                        return true
                                    }
                                    "m" -> {
                                        if (machines[args[2]] != null) {
                                            sender.sendMessage(prefix + "§bData of Machine: " + args[2])
                                            sender.sendMessage(prefix + "§7" + machines[args[2]])
                                        } else {
                                            sender.sendMessage(prefix + "§bMachine: " + args[2] + " doesn't exist")
                                        }
                                        return true
                                    }
                                    "r" -> {
                                        if (recipies[args[2]] != null) {
                                            sender.sendMessage(prefix + "§bData of Recipe: " + args[2])
                                            sender.sendMessage(prefix + "§7" + recipies[args[2]])
                                        } else {
                                            sender.sendMessage(prefix + "§bRecipe: " + args[2] + " doesn't exist")
                                        }
                                        return true
                                    }
                                }
                            }

                        }
                    }
                    4 -> {
                        when(args[0]) {
                            "setlevel" -> {
                                val targetPlayer = Bukkit.getPlayer(args[1])
                                if (targetPlayer == null) {
                                    sender.sendMessage(prefix + "§bPlayer doesn't exist")
                                    return true
                                }
                                //val targetSkill = skills[args[2].toInt() + 1]
                                if (skills.size < args[2].toInt()) {
                                    sender.sendMessage(prefix + "§bSkill doesn't exist")
                                    return true
                                }
                                val level = args[3].toInt()
                                if (level < 0 || level > 100) {
                                    sender.sendMessage(prefix + "§bLevel value is invalid")
                                    return true
                                }
                                if (!skill.currentPlayerData.containsKey(targetPlayer.uniqueId)){
                                    skill.currentPlayerData[targetPlayer.uniqueId] = mutableMapOf(args[2].toInt() to level)
                                }else{
                                    val s = skill.currentPlayerData[targetPlayer.uniqueId]
                                    s!![args[2].toInt()] = level
                                }
                                sender.sendMessage(prefix + "§bLevel set")
                                return true
                            }
                        }
                    }
                }
            }
            warnWrongCommand(sender)
        } else {
            sender.sendMessage(prefix + "Can't run from console.")
            return true
        }
        return false
    }

    fun showHelp(sender: CommandSender) {
        sender.sendMessage("§a******** §b§lm§3§lIndustry §a********")
        sender.sendMessage("§bData Managements")
        sender.sendMessage("§3/mi reload §7Load all data")
        sender.sendMessage("§3/mi list §7View all CS, Machines, Recipes, Skills")
        sender.sendMessage("§3/mi info [c/m/r] [key] §7View specific data of CS/ Machine/ Recipe")
        sender.sendMessage("§3/mi setinput [recipeKey] §7Set an input for recipe")
        sender.sendMessage("§3/mi setoutput [recipeKey] §7Set an output for recipe")
        sender.sendMessage("§3/mi usemachine [machineKey] §7Use a machine")
        sender.sendMessage("§3/mi setlevel [playerId] [skillId] [level] §7Set a level of player")
        sender.sendMessage("§3/mi update [playerId] §7Update player's skill cache by DB.")
        sender.sendMessage("§3/mi get [machineId] §7Get a machine")
        sender.sendMessage("§bVer 0.2 : by Shupro & Ryotackey")
        sender.sendMessage("§a***************************")
    }

    fun showList(sender: CommandSender) {
        sender.sendMessage(prefix + "§bChanceSets: ")
        sender.sendMessage(prefix + "§7" + chanceSets.keys)

        sender.sendMessage(prefix + "§bSkills: ")
        val skillStringList = mutableListOf<String>()
        skills.onEach { skillStringList.add(it.name) }
        sender.sendMessage(prefix + "§7" + skillStringList)

        sender.sendMessage(prefix + "§bRecipes: ")
        sender.sendMessage(prefix + "§7" + recipies.keys)

        sender.sendMessage(prefix + "§bMachines: ")
        sender.sendMessage(prefix + "§7" + machines.keys)
    }

    fun warnWrongCommand(sender: CommandSender) {
        sender.sendMessage(prefix + "Wrong Command. /mi help")
    }

}
