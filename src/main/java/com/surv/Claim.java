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
    getServer().getConsoleSender().sendMessage("fuck you we is live");
  }

  @Override
  public void onDisable() {
    getServer().getConsoleSender().sendMessage("hey now offline");
  }
}
