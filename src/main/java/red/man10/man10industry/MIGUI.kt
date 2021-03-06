package red.man10.man10industry

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class MIGUI(val pl: Man10Industry): Listener {

    fun openProcessingView(p: Player, machineId: String) {
        if (pl.machines[machineId] != null) {

            val inv = Bukkit.createInventory(null, 54, pl.prefix + "§0加工メニュー")

            val blackGlass = createItem(Material.STAINED_GLASS_PANE, 15.toShort(), "", mutableListOf())
            placeItem(blackGlass, inv, mutableListOf(45, 46, 47, 48, 50, 51, 52, 53))

            val rightArrow = createItem(Material.STAINED_GLASS_PANE, 5, "§b§lクリックで§e§l加工!", mutableListOf("§8"  + machineId))
            placeItem(rightArrow, inv, mutableListOf(49))

            p.openInventory(inv)
        } else {
            p.sendMessage(pl.prefix + "Machine §e" + machineId + " §bdoesn't exists.")
        }
    }

    fun openInputSetView(p: Player,  recipeId: String) {
        val inv = Bukkit.createInventory(null, 54, pl.prefix + "§0Set Input")

        val blackGlass = createItem(Material.STAINED_GLASS_PANE, 15.toShort(), "", mutableListOf())
        placeItem(blackGlass, inv, mutableListOf(45, 46, 47, 48, 50, 51, 52, 53))

        val greenGlass = createItem(Material.STAINED_GLASS_PANE, 5.toShort(), "§a§lSet Input", mutableListOf("§8"  + recipeId))
        placeItem(greenGlass, inv, mutableListOf(49))

        p.openInventory(inv)
    }

    fun openOutputSetView(p: Player,  recipeId: String) {
        val inv = Bukkit.getServer().createInventory(null, 54, pl.prefix + "§0Set Output")

        val blackGlass = createItem(Material.STAINED_GLASS_PANE, 15.toShort(), "", mutableListOf())
        placeItem(blackGlass, inv, mutableListOf(45, 46, 47, 48, 50, 51, 52, 53))

        val greenGlass = createItem(Material.STAINED_GLASS_PANE, 5.toShort(), "§a§lSet Output", mutableListOf("§8"  + recipeId))
        placeItem(greenGlass, inv, mutableListOf(49))

        p.openInventory(inv)
    }


    @EventHandler
    fun onItemClick(e: InventoryClickEvent) {
        val inv = e.inventory
        val p = e.whoClicked
        when (inv.name) {
            (pl.prefix + "§0加工メニュー") -> {
                val blankSlots = mutableListOf(45, 46, 47, 48, 50, 51, 52, 53)
                val arrowSlots = mutableListOf(49)

                when {
                    (blankSlots.contains(e.slot)) -> {
                        e.isCancelled = true
                    }
                }

                if (arrowSlots.contains(e.slot)) {
                    e.isCancelled = true
                    val machineKey = inv.getItem(e.slot).itemMeta.lore.first().removePrefix("§8")

                    val skillData = pl.skill.currentPlayerData[e.whoClicked.uniqueId]!!
                    val inputs = mutableListOf<ItemStack>()
                    for (slot in 0 until 45) {
                        if (inv.getItem(slot) != null) {
                            inputs.add(inv.getItem(slot))
                        }else{
                            inputs.add(ItemStack(Material.AIR))
                        }
                    }
                    val outputs = pl.machine.process(skillData, pl.machines[machineKey]!!, inputs, e.whoClicked as Player)
                    if (outputs == mutableListOf(ItemStack(Material.AIR))){
                        p.sendMessage("${pl.prefix}§bレシピが間違ってます")
                        return
                    }

                    if (outputs == null){
                        p.sendMessage("${pl.prefix}§c作成に失敗しました")
                        for (i in 0 until 45){
                            inv.setItem(i, ItemStack(Material.AIR))
                        }
                        return
                    }

                    for (slot in 0 until 45) {
                        inv.setItem(slot, ItemStack(Material.AIR))
                    }

                    var count = 0
                    for (item in outputs) {
                        inv.setItem(count, item)
                        count++
                    }
                    p.sendMessage("${pl.prefix}§a作成に成功しました")
                }
            }

            (pl.prefix + "§0Set Input"), (pl.prefix + "§0Set Output") -> {
                val itemSlots = mutableListOf(45, 46, 47, 48, 49, 50, 51, 52, 53)
                if (itemSlots.contains(e.slot)) {
                    e.isCancelled = true
                }

                if (e.slot == 49) {
                    var items = mutableListOf<ItemStack>()
                    for (slot in 0..44) {
                        if (inv.getItem(slot) != null) {//!= ItemStack(Material.AIR)) {
                            items.add(inv.getItem(slot))
                        }else{
                            items.add(ItemStack(Material.AIR))
                        }
                    }

                    val recipeKey = inv.getItem(49).itemMeta.lore.first().removePrefix("§8")

                    if (inv.title == (pl.prefix + "§0Set Input")) {
                        pl.recipies[recipeKey]!!.inputs = items
                    } else {
                        pl.recipies[recipeKey]!!.outputs = items
                    }

                    val encodedItems = pl.util.itemStackArrayToBase64(items.toTypedArray())
                    print(encodedItems)
                    if (inv.title == (pl.prefix + "§0Set Input")) {
                        pl.config.setInput(encodedItems, recipeKey)
                    } else {
                        pl.config.setOutput(encodedItems, recipeKey)
                    }
                    p.closeInventory()

                }
            }
        }
    }

    fun createItem(material: Material, itemtype: Short?, itemName: String, loreList: MutableList<String>): ItemStack {
        val CIitemStack = ItemStack(material, 1, itemtype!!)
        val CIitemMeta = CIitemStack.itemMeta
        CIitemMeta.displayName = itemName
        CIitemMeta.lore = loreList
        CIitemStack.itemMeta = CIitemMeta
        return CIitemStack
    }

    fun createDHItem(itemName: String, loreList: MutableList<String>, durability: Short): ItemStack { //DH = DiamondHoe
        val CIItemStack = ItemStack(Material.DIAMOND_HOE, 1, durability)
        val CIItemMeta = CIItemStack.itemMeta
        CIItemMeta.displayName = itemName
        CIItemMeta.isUnbreakable = true
        CIItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE)
        CIItemMeta.lore = loreList
        CIItemStack.itemMeta = CIItemMeta
        return CIItemStack
    }

    fun placeItem(item: ItemStack, inv: Inventory, places: MutableList<Int>) {
        for (place in places) {
            inv.setItem(place, item)
        }
    }

}