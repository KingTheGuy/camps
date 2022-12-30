package com.surv;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.surv.land.vecPos;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

//[not sure]
//TODO([DONE?]explosion will remove the camp): need to figure how to prevent it from being destroyed
//or if destroyed removed it from camps list.
//FIXME: if claimed prevent explosion destruction

//TODO([i am fucked]): implement presistant camp.. aka save and load on reboot
//SAVEs
//TODO: figure out sql or some other save type

//TODO(sure but why??): make it so blocks take a few breaks to completey delete
//if there is no campfire near

//TODO(yea i cant figure out how to use a timer): when the player punches the campfire display a radius

//NOTE: i was gonna make its so you could not place stuff with buckets outside
//of camps, but that should just be left to claims.

//[NEXT]
//TODO: for the player to add a friend they will hit a player with their shield
//to remove friends.. they will burn their shield(removing all friends)
//Must be tossed and not dropped(aka death drop does not count)

//TODO: somehow figure out how to deal with pair camp settings..
//aka [can pick crops] [can kill live stock]
//**This can actually be part of the camp settings
//ok ok maybe the way this is setup is by hitting the campfire with a sword-axe-or-hoe

//[AT SOME POINT]
//TODO: implement so that somehow the player can
//teleport to their claim with the Magic Mirror
//also tp to friends claims

//FIXME: there was another idea or something that i had to fix

//FIXME(current fix allow placement of furnace): umm boss how do you make a campfire??

public class campfire implements Listener {

	public static int default_radius = 6; // NOTE: this needs to be increased after testing

	ArrayList<camp> campfires = new ArrayList<>();
	ArrayList<owner_settings> owners = new ArrayList<>();
	camp camper = new camp();

	// TODO: add some check to limit amount of initial claims
	// NOTE: owner is used to get permissions/rules for the claim
	class owner_settings {
		String name;
		// ArrayList<camp> claims; // max 3 claims
		ArrayList<String> friends;

		public boolean isOwner(Player p) {
			if (this.name == p.getName()) {
				return true;
			}
			return false;
		}

		public boolean isFriend(Player p) {
			// check if the player is a friend
			for (String f : this.friends) {
				if (f == p.getName()) {
					return true;
				}

			}
			return false;
		}
	}

	class camp {
		vecPos pos;
		int radiusTimer; // NOTE: currently not in use
		String owner;
		boolean pick_crops, live_stock;

		// this claims the camp
		public void claimCamp(Location loc, Player p) {
			this.defineOwnerSettings(p);
			for (camp c : campfires) {
				if (c.withInRadius(loc, default_radius)) {
					Audience audience = Audience.audience(p);
					if (c.owner == null) {
						c.owner = p.getName();
						// p.sendMessage(String.format("Congrats, you have claimed this camp!"));
						// p.sendTitle();
						audience.sendActionBar(
								() -> Component.text(String.format(ChatColor.GOLD + "Congrats, you have claimed this camp!")));
						// should maybe tell the player how many claims they have now.
					} else {
						// already claimed
						// FIXME: somehow after claiming this code runs..
						String message = "This has already been claimed, by";
						if (c.owner == p.getName()) {
							// p.sendMessage(String.format("%s you!", message));
							audience.sendActionBar(
									() -> Component.text(String.format(ChatColor.GOLD + "%s you!", message)));
						} else {
							audience.sendActionBar(
									() -> Component.text(String.format(ChatColor.GOLD + "%s %s", message, c.owner)));
							// p.sendMessage(String.format("%s %s", message, c.owner));
						}
					}
					break;
				}
			}
		}

		// this check is owner has settings, if not it creates it
		private void defineOwnerSettings(Player p) {
			if (owners.size() > 0) {
				boolean ownerIsDefined = false;
				for (owner_settings o : owners) {// get all settings
					if (o.name == p.getName()) {
						ownerIsDefined = true;
						break;
					}
				}
				if (!ownerIsDefined) {
					owner_settings new_owner = new owner_settings();
					new_owner.name = p.getName();
					owners.add(new_owner);
				}
			}
		}

		// PICK_CROPS.. prevents leaving empty
		// LIVE_STOCK.. prevents leaving less than 2
		// FIXME: these may not actaully work
		// considering that a mob may be left underground
		// somewhere
		public boolean canPickCrops() {
			if (this.pick_crops) {
				return true;
			}
			return false;
		}

		public boolean canKillLiveStock() {
			if (this.live_stock) {
				return true;
			}
			return false;
		}

		// within campfire
		// FIXME: FUCK numbers add my own type

		public boolean canBuildDestroy(Location loc, Player p, boolean blockBreak) {
			if (campfires.size() > 0) {
				for (camp c : campfires) {
					if (c.withInRadius(loc, default_radius)) {
						if ((c.owner == null) || (c.owner == p.getName())) {
							// has no owner or is owner
							return true; // you can build/break
						} else {
							for (owner_settings o : owners) {
								if (o.name == c.owner) {
									for (String f : o.friends) {
										if (f == p.getName()) {
											return true;
										}
									}
								}
							}
						}
						// there is a camp owner and an owner nd player is not it
						return false;
					}
				}
			}
			// because we are not in a claim and i need to break blocks
			if (blockBreak == true) {
				return true;
			}
			// if no camps
			return false;
		}
		// FIXME: i need to figure these numbers shit

		private boolean withInRadius(Location loc, int radius) {
			// checks if block is with the claim
			if (land.getDistance(this.pos.x, this.pos.z, loc.blockX(), loc.getBlockZ()) <= radius) {
				return true;
			}
			return false;
		}

		public boolean samePos(Location loc) {
			if (this.pos.x == loc.getX()) {
				if (this.pos.y == loc.getY()) {
					if (this.pos.z == loc.getZ()) {
						return true;
					}
				}
			}
			return false;
		}

	}

	// this is when a block is destroyed.. by any means (even explosoins)
	@EventHandler
	public void onExplosion(BlockDestroyEvent ev) {
		Location loc = ev.getBlock().getLocation();
		for (camp c : campfires) {
			if (c.samePos(loc)) {
				campfires.remove(c);
				System.out.print(String.format("campfire [%s] was removed, campfires are now: %s", c, campfires.size()));
			}
		}
	}

	@EventHandler
	public void onExplosionPrime(ExplosionPrimeEvent ev) {
		int radius = (int) ev.getRadius();
		Location loc = ev.getEntity().getLocation();
		for (camp c : campfires) {
			if (c.withInRadius(loc, radius)) {
				campfires.remove(c);
				if (campfires.size() == 0) {
					break;
				}
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		Player player = ev.getPlayer();
		Block block = ev.getBlock();
		boolean destroy = false;
		if (block.getType().toString().contains("BANNER")) {
			camper.claimCamp(block.getLocation(), player);
		} else if (block.getType() == Material.CAMPFIRE) {
			// may have to check if there is already another campfire..
			// maybe not.. the flag/claim prob only has too.
			camp new_camp = new camp();
			vecPos pos = new vecPos();
			pos.x = block.getX();
			pos.y = block.getY();
			pos.z = block.getZ();
			new_camp.pos = pos;
			boolean sameSpot = false;
			if (campfires.size() != 0) {
				for (camp c : campfires) {
					if (c.samePos(block.getLocation())) {
						sameSpot = true;
						break;
					}
				}
			}
			if (sameSpot) {
			} else {
				campfires.add(new_camp);
			}
			// SAVE starts here

		} else if ((block.getType().toString().contains("LOG")) || (block.getType() == Material.CRAFTING_TABLE)
				|| (block.getType() == Material.LADDER) ||
				(block.getType().toString().contains("SHULKER")) || (block.getType().toString().contains("TORCH"))
				|| (block.getType().toString().contains("LANTERN")) || (block.getType() == Material.FURNACE)) {
			// // can place it
		} else {
			// FIXME: i if camp is claimed prevent
			// player none owner from placing camp
			if (camper.canBuildDestroy(block.getLocation(), player, false) == false) {
				destroy = true;
			}
		}
		if (destroy) {
			// ev.getBlock().setType(Material.AIR);
			ev.setCancelled(true);
			Audience audience = Audience.audience(player);
			audience.sendActionBar(
					() -> Component.text(String.format(ChatColor.GRAY + "Need to be near a camp.")));
			// player.dropItem(true);
			// ev.getBlock().breakNaturally(true); // TODO: may have to relook at this
			player.spawnParticle(Particle.CRIT_MAGIC, block.getLocation(), 10, 1, 1, 1);
			player.playSound(block.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.BLOCKS, 1, 1);
		}
	}

	@EventHandler
	public void onBlockDestroy(BlockBreakEvent ev) {
		Block block = ev.getBlock();
		Player player = ev.getPlayer();
		if (camper.canBuildDestroy(block.getLocation(), player, true) == false) {
			ev.setCancelled(true);
		}
		// FIXME(seems good to me): something here is wrong, but i dont feel like fixing

		// FIXME(ok good chance this does not need to be fix.. as the entire camp is
		// removed anyways): i need to remove the "claim" if the campfire gets removed
		if (block.getType() == Material.CAMPFIRE) {
			if (campfires.size() == 0) {
				// do nothing
			} else {
				for (camp c : campfires) {
					if (c.samePos(block.getLocation())) {
						campfires.remove(c);
						break;
					}
				}
			}
		}
	}

}
