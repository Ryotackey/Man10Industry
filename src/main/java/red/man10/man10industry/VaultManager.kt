package red.man10

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

import java.util.UUID

/**
 * Created by takatronix on 2017/03/04.
 */
class VaultManager(private val plugin: JavaPlugin) {
    var showMessage = true

    init {
        setupEconomy()
    }

    private fun setupEconomy(): Boolean {
        plugin.logger.info("setupEconomy")
        if (plugin.server.pluginManager.getPlugin("Vault") == null) {
            plugin.logger.warning("Vault plugin is not installed")
            return false
        }
        val rsp = plugin.server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            plugin.logger.warning("Can't get vault service")
            return false
        }
        economy = rsp.provider
        plugin.logger.info("Economy setup")
        return economy != null
    }


    fun canUseVault(): Boolean {
        return if (economy != null) {
            true
        } else false
    }

    ///////////////////////////    public boolean canUseVault() {
    //        if(economy != null){
    //            return true;
    //        }
    //        return false;
    //    }    public boolean canUseVault() {
    //        if(economy != null){
    //            return true;
    //        }
    //        return false;
    //    }//////////
    //      残高確認
    /////////////////////////////////////
    fun getBalance(uuid: UUID): Double {
        if (economy == null) {
            plugin.logger.warning("Vault plugin is not installed")
            return 0.0
        }

        return economy!!.getBalance(Bukkit.getOfflinePlayer(uuid).player)
    }

    /////////////////////////////////////
    //      残高確認
    /////////////////////////////////////
    fun showBalance(uuid: UUID) {
        val p = Bukkit.getOfflinePlayer(uuid).player
        val money = getBalance(uuid)
        p.player.sendMessage(ChatColor.YELLOW.toString() + "Your balance: " + balanceString(money))
    }

    /////////////////////////////////////
    //      引き出し
    /////////////////////////////////////
    fun withdraw(uuid: UUID, money: Double): Boolean? {
        if (economy == null) {
            plugin.logger.warning("Vault plugin is not installed")
            return false
        }

        val p = Bukkit.getOfflinePlayer(uuid)
        if (p == null) {
            Bukkit.getLogger().info("cant find user:" + uuid.toString())
            return false
        }
        val resp = economy!!.withdrawPlayer(p, money)
        if (resp.transactionSuccess()) {
            if (p.isOnline) {
                if (showMessage) {
                    p.player.sendMessage(ChatColor.YELLOW.toString() + balanceString(money) + " paid")
                }
            }
            return true
        }
        return false
    }

    /////////////////////////////////////
    //      deposit / お金を入れる
    /////////////////////////////////////
    fun deposit(uuid: UUID, money: Double): Boolean? {
        if (economy == null) {
            plugin.logger.warning("Vault plugin is not installed")
            return false
        }
        val p = Bukkit.getOfflinePlayer(uuid)
        if (p == null) {
            Bukkit.getLogger().info("Cant find user:" + uuid.toString())
            return false
        }
        val resp = economy!!.depositPlayer(p, money)
        if (resp.transactionSuccess()) {
            if (p.isOnline) {
                if (showMessage) {
                    p.player.sendMessage("§eYou received" + balanceString(money))

                }
            }
            return true
        }
        return false
    }

    internal fun balanceString(bal: Double): String {
        return String.format("$%,.0f", bal)
    }

    companion object {

        var economy: Economy? = null
    }

}