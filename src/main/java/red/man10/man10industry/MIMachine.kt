package red.man10.man10industry

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import red.man10.man10industry.models.*
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
}