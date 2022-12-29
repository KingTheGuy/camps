package com.surv;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

//TODO: claims are campfire sites/ is a claimed campfire
//so idk, a camp site can be claimed with a flag
//if campfire -> if calimed.. get who claimed. //TODO: add boolean claimed to camp
//NOTE: this may not work well, because what heppens when an entry is then removed??
//maybe a claim takes the size of claim
//TODO(maybe): to add someone as friend you need to punch hit them with a shield??

//FIXME: something is broken and this is all a mess

public class land implements Listener {
  ArrayList<owners> claims = new ArrayList<>();

  public static int getDistance(int x1, int z1, int x2, int z2) {
    int z = x2 - x1;
    int x = z2 - z1;
    return (int) Math.sqrt(x * x + z * z);
  }

  // main
  class owners {
    String name;
    ArrayList<claim> claims; // max 3 claims
    ArrayList<String> friends;

    public boolean isOwner(Player p) {
      if (this.name == p.getName()) {
        return true;
      }
      return false;
    }
  }

  // secound
  class claim {
    vecPos pos;
    ArrayList<String> allowed_players;
    boolean slay_animals, pick_crops;

    public boolean withIn(Location loc, int radius) {
      if (land.getDistance(this.pos.x, this.pos.z, loc.blockX(), loc.getBlockZ()) <= radius) {
        return true;
      }
      return false;
    }
  }

  static class vecPos {
    int x, y, z;
  }

  public boolean hasClaim(Player player) {
    for (owners o : claims) { // this goes through each owner
      // then search each owner for claims
      if (o.name == player.getName()) {
        player.sendMessage("oh looks like you already have a claim");
        return true;
        // break;

      }
    }
    return false;
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent ev) {
    Player player = ev.getPlayer();
    String message = String.format("block is: %s", ev.getBlock().getType());
    Block block = ev.getBlock();
    player.sendMessage(message);
    if (ev.getBlock().getType().toString().contains("BANNER")) {
      claim claim = new claim();
      vecPos pos = new vecPos();
      pos.x = block.getX();
      pos.y = block.getY();
      pos.z = block.getZ();
      claim.pos = pos;
      // ArrayList<String> friends = new ArrayList<>();
      // friends.add("jumbo");
      // claim.allowed_players = friends;
      claim.slay_animals = false;
      claim.pick_crops = true;
      // check if player already has a claim
      if (!hasClaim(player)) {
        // MUST CREATE A NEW OWNER
        owners owner = new owners();
        ArrayList<claim> owner_claims = new ArrayList<>();
        owner.name = player.getName();
        owner.claims = owner_claims;
        claims.add(owner);
      }
      for (owners o : claims) {
        if (o.name == player.getName()) {
          if (o.claims.size() < 3) {
            o.claims.add(claim);
            break;
          } else {
            player.sendMessage("sorry about that, looks like you already have 3 claims");
            break;
          }
        }
      }
      player.sendMessage("nice, this is a campfire");
      System.out.printf("block is at [%s,%s,%s]%n", ev.getBlock().getX(), ev.getBlock().getY(), ev.getBlock().getZ());
    }
    for (owners o : claims) {
      for (claim c : o.claims) {
        if (c.withIn(block.getLocation(), campfire.default_radius))
          if (o.isOwner(player)) {

          } else {
            ev.setCancelled(true);
          }

      }
    }
    String new_message = String.format("current claims: %s", claims);
    player.sendMessage(new_message);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent ev) {
    Player player = ev.getPlayer();
    Block block = ev.getBlock();
    for (owners o : claims) {
      player.sendMessage("for sure this should be working now");
      for (claim c : o.claims) {
        if (c.withIn(block.getLocation(), campfire.default_radius)) {
          if (o.isOwner(player)) {
            break;
          } else {
            player.sendMessage("this should be getting canceled");
            ev.setCancelled(true);
            break;
          }
        }
      }
    }
    // if (ev.getBlock().getType() == Material.GRASS_BLOCK) {
    // ev.setCancelled(true);
    // }
    System.out.printf("block is at [%s,%s,%s]%n", ev.getBlock().getX(), ev.getBlock().getY(), ev.getBlock().getZ());
  }

}
