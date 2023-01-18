package com.surv;

import org.bukkit.plugin.java.JavaPlugin;

// public class App 
// {
//     public static void main( String[] args )
//     {
//         System.out.println( "Hello World!" );
//     }
// }
public class Claim extends JavaPlugin {
  @Override
  public void onEnable() {
    // getServer().getPluginManager().registerEvents(new land(), this);
    getServer().getPluginManager().registerEvents(new campfire(), this);
    getServer().getConsoleSender().sendMessage("campfire plugin loaded.");
  }

  @Override
  public void onDisable() {
    getServer().getConsoleSender().sendMessage("fires have been out put.");
  }
}
