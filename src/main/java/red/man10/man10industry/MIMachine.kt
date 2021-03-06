package red.man10.man10industry

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import red.man10.man10industry.models.*
import java.awt.Graphics2D
import java.util.*

class MIMachine(val pl: Man10Industry) {

    fun process(p: PlayerSkillData, machine: Machine, inputs: MutableList<ItemStack>, pla: Player): MutableList<ItemStack>? {
        print(inputs)
        for (recipe in machine.recipes) {
                if (recipe.inputs != inputs){
                    break
                }
            val chance = recipe.chanceSets
            val skillid = mutableListOf<Int>()
            for (c in chance) {
                for (i in 0 until pl.skills.size) {
                    if (pl.skills[i] == c.key){
                        skillid.add(i)
                        break
                    }
                }
            }

            if (skillid.size == 0) return mutableListOf(ItemStack(Material.AIR))

            val flags = mutableListOf<Boolean>()

            for (i in skillid){
                val level = chance[pl.skills[i]]

                if (p[i]!! < level!!.req ){
                    pla.sendMessage("${pl.prefix}§cレベルが足りません")
                    return inputs
                }

                var pre: Int = 0
                var min = 0.0


                for (l in level.chances){

                    if (p[i]!!.toInt() >= l.key && pre < l.key){
                        min = l.value
                        pre = l.key.toString().toInt()
                    }

                }
                val r = Random().nextDouble()

                if (min >= r){
                    flags.add(true)
                }else{
                    flags.add(false)
                    val r2 = Random().nextDouble()
                    pla.sendMessage(r2.toString() + "/" + min.toString() + "/" + pl.player_slimit[pla.uniqueId]!! + "/" + p[i]!!)
                    if (min < r2 && p[i]!! < 100 && pl.player_slimit[pla.uniqueId]!! > 0) {
                        pla.sendMessage("${pl.prefix}§e${pl.skills[i-1].name}スキル§aがレベルアップしました！§6[§f${p[i]!!}Lv->${p[i]!! + 1}Lv§6]")
                        val s = pl.skill.currentPlayerData[pla.uniqueId]
                        s!![i] = p[i]!! + 1
                        pl.player_slimit[pla.uniqueId] = pl.player_slimit[pla.uniqueId]!! - 1
                    }
                }
            }

            for (i in flags){
                if (!i)return null
            }

            return recipe.outputs

        }
        return mutableListOf(ItemStack(Material.AIR))
    }

    fun createMapItem(machineKey: String): ItemStack {
        var map = MappRenderer.getMapItem(pl!!, machineKey)
        map.itemMeta.displayName = "§b§l" + pl!!.machines[machineKey]!!.name + "§r§7(100/100)"
        return map
    }

    fun createAllMachineMapp(){
        for (machine in pl!!.machines) {
            MappRenderer.draw(machine.key, 0) { key: String, mapId: Int, g: Graphics2D ->
                //      画面更新をする
                val result = drawImage(g, machine.value.imageName!!, 0, 0, 128, 128)
                if (!result) {
                    g.drawString("No Image Found", 10, 10)
                }
                true
            }
            MappRenderer.displayTouchEvent("machine") { key: String, mapId: Int, player: Player, x: Int, y: Int ->
                player.chat("/mi usemachine $key")
                true
            }
        }
    }

    fun drawImage(g: Graphics2D, imageKey: String, x: Int, y: Int, w: Int, h: Int): Boolean {
        val image = MappRenderer.image(imageKey)
        if (image == null) {
            Bukkit.getLogger().warning("no image:$imageKey")
            return false
        }

        g.drawImage(image, x, y, w, h, null)

        return true
    }

}