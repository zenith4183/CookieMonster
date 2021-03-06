/**
 * Programmer: Jacob Scott Program Name: CMEcon Description: handler for econ
 * events Date: Apr 11, 2011
 */
package com.pi.coelho.CookieMonster;

import com.nijikokun.register_1_3.payment.Method;
import com.nijikokun.register_1_3.payment.Methods;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author jacob
 */
public class CMEcon implements Listener {

	protected static Method economyMethod = null;
	protected static Methods _econMethods = new Methods();
	protected static Economy vaultEcon = null;
	CookieMonster plugin = null;
	PluginManager pm;

	public CMEcon(CookieMonster plugin) {
		this.plugin = plugin;
		pm = plugin.getServer().getPluginManager();

		// first attempt to load external economy plugins
		Plugin v = pm.getPlugin("Vault");
		if (v instanceof Vault) {
			RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp != null) {
				vaultEcon = rsp.getProvider();
			}
		}
		if (vaultEcon == null) {

			Methods.setMethod(pm);

			if ((economyMethod = Methods.getMethod()) != null) {
				CookieMonster.Log("Using " + economyMethod.getName()
						+ " v" + economyMethod.getVersion() + " for economy");
			}
		} else {
			CookieMonster.Log("Using Vault v" + v.getDescription().getVersion() + " for economy");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {if (vaultEcon == null) {
		// Check to see if the plugin thats being disabled is the one we are using
		if (_econMethods != null && Methods.hasMethod() && Methods.checkDisabled(event.getPlugin())) {
			economyMethod = null;
			Methods.reset();
			CookieMonster.Log(" Economy Plugin was disabled.");
		}
	}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		if (vaultEcon == null && !Methods.hasMethod()) {
			if (Methods.setMethod(pm) && Methods.hasMethod()) {
				economyMethod = Methods.getMethod();
				CookieMonster.Log("Using " + economyMethod.getName()
						+ " v" + economyMethod.getVersion() + " for economy");
			}
		}
	}

	public static boolean active() {
		return vaultEcon != null || economyMethod != null;
	}

	public static boolean hasAccount(Player pl) {
		if(vaultEcon != null) {
			return vaultEcon.hasAccount(pl.getName());
		}
		return pl != null && economyMethod != null && economyMethod.hasAccount(pl.getName());
	}

	public static boolean canAfford(Player pl, double amt) {
		if(vaultEcon != null) {
			return pl != null ? vaultEcon.getBalance(pl.getName()) >= amt : false;
		}
		return pl != null ? getBalance(pl.getName()) >= amt : false;
	}

	public static double getBalance(Player pl) {
		return getBalance(pl.getName());
	}

	public static double getBalance(String playerName) {
		if(vaultEcon != null) {
			return vaultEcon.getBalance(playerName);
		}
		if (economyMethod != null && economyMethod.hasAccount(playerName)) {
			return economyMethod.getAccount(playerName).balance();
		}
		return 0;
	}

	public static void addMoney(Player pl, double amt) {
		addMoney(pl.getName(), amt);
	}

	public static void addMoney(String playerName, double amt) {
		if(vaultEcon != null) {
			if(!vaultEcon.hasAccount(playerName)) {
				return;
			}
			vaultEcon.depositPlayer(playerName, amt);
		} else if (economyMethod != null) {
			if (!economyMethod.hasAccount(playerName)) {
				// TODO? add methods for creating an account
				return;
			}
			economyMethod.getAccount(playerName).add(amt);
		}
	}

	public static void subtractMoney(Player pl, double amt) {
		subtractMoney(pl.getName(), amt);
	}

	public static void subtractMoney(String playerName, double amt) {
		if(vaultEcon != null) {
			if(!vaultEcon.hasAccount(playerName)) {
				return;
			}
			vaultEcon.withdrawPlayer(playerName, amt);
		} else if (economyMethod != null) {
			if (!economyMethod.hasAccount(playerName)) {
				// TODO? add methods for creating an account
				return;
			}
			economyMethod.getAccount(playerName).subtract(amt);
		}
	}

	public static String format(double amt) {
		if(vaultEcon != null) {
			return vaultEcon.format(amt);
		} else if (economyMethod != null) {
			return economyMethod.format(amt);
		}
		return String.format("%.2f", amt);
	}

	public static String formatCurrency(double amt) {
		if(vaultEcon != null) {
			return vaultEcon.format(amt) + " " + (amt == 1 ? vaultEcon.currencyNameSingular() : vaultEcon.currencyNamePlural());
		} else if (economyMethod != null) {
			return economyMethod.format(amt);
		}
		return String.format("%.2f", amt);
	}
} // end class CMEcon

