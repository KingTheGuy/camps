package com.surv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerLoadEvent;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surv.land.vecPos;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

//FIXME(): SAVE/LOAD works??? but when loaded its not reading the data correctly

//[NEEDS TESTING]
//DONE: disable farm land trampaling
//DONE: on claim camp check if there is another claimed camp within the radius
//DONE: prevent "friends" from breaking claimed campfire
//DONE: for the player to add a friend they will hit a player with their shield
//to remove friends.. they will burn their shield(removing all friends)
//Must be tossed and not dropped(aka death drop does not count)
//player has to click on their campfire with a shield
//DONE: prevent none owners/friends from using:
//lava
//water
//minecarts
//armor stand
//prevent using eggs
//JUST PREVENT USING ITEMS
//PREVENT PLACING whitelisted items in claimed land
//DONE: prevent mobs from blowing up claimed areas..
//DONE: explosions, should cancel block breaking but not damage.
//NOTE: blown up outside of claim damaged inside claim
//DRAGON is yes.. WITHER is no..//NOTE(is skipped this part):
//DONE: prevent harming animals in claims
//DONE: prevent riding unowned animals???//NOTE: chance that this is already being canceled 
//not sure how im going to do this.. 
//for starters prevent riding anything in claimed land

//[DOING]
//FIXME: let none camp claims overlap.

//FIXME(this can be really bad): can't damage monsters in claims

//FIXME(ODD): remove the "need to be near camp" when 
//camp is claimed by someone else

//TODO:NOTE(DONE, but need to be worked in): Make is so admin.. or player with tag
//can remove camps.. or doing anything in the camp

//TODO: maybe to untable a horse you remove the saddle?

//[NEXT]
//TODO(maybe): maybe do block placing differently..
//instead of canceling it, have it be like
//how adventure mode dose it.
//TODO:(maybe): maybe blocks in claims take multiple breaks
//to break.. show the player how many more times they need to
//braek the block for it to break.
//maybe the player needs to click the block with iron in hand
//to re-enforce it.. idk.

//TODO: somehow figure out how to deal with per camp settings..
//aka [can pick crops] [can kill live stock]
//**This can actually be part of the camp settings
//ok ok maybe the way this is setup is by hitting the campfire with a sword-axe-or-hoe
//NOTE: for picking crops.. let player fully break and have
//them replant it

//TODO: add more sounds/effects/and messages.

//TODO: implement so that somehow the player can
//teleport to their claim with the Magic Mirror
//also tp to friends claims

//TODO: punching the campfire/ or when placing the banner will
//tell the player how to configure things for the camp

//FIXME: there was another idea or something that i had to fix

//TODO: add some check to limit amount of initial claims.
//need to limit claims, can't have player claim all the land they wantt

//TODO:should a claim protect chests with inventories?? //NOTE: USE BLOCK PROC if anything.. issue is the friends permission

//[not sure]

//TODO: figure out sql or some other save type

//TODO(sure but why??): make it so blocks take a few breaks to completey delete
//if there is no campfire near

//TODO(yea i cant figure out how to use a timer): when the player punches the campfire display a radius

//NOTE: i was gonna make its so you could not place stuff with buckets outside
//of camps, but that should just be left to claims.

public class campfire implements Listener {

	public static int default_radius = 6; // NOTE: this needs to be increased after testing
	List<Owner_settings> owners = new ArrayList<>(); // NEEDS SAVING
	Camp camper = new Camp();

	List<Camp> campfires = new ArrayList<>(); // NEEDS SAVING
	String saveFileName = "saved_camps.json";

	public void loadFromFile() {
		// Gson gson = new Gson();
		// try (FileReader reader = new FileReader(saveFileName)) {
		// Type listType = new TypeToken<List<Camp>>() {
		// }.getType();
		// campfires = gson.fromJson(reader, listType);
		// // getLogger().info("Successfully loaded placed blocks from file.");
		// } catch (IOException e) {
		// // getLogger().warning("Failed to load placed blocks from file: " +
		// // e.getMessage());
		// }
	}

	public void saveToFile() {
		// Gson gson = new GsonBuilder().setPrettyPrinting().create();
		// try (FileWriter writer = new FileWriter(saveFileName, false)) {
		// gson.toJson(campfires, writer);
		// // getLogger().info("Successfully saved placed blocks to file.");
		// } catch (IOException e) {
		// // getLogger().warning("Failed to save placed blocks to file: " +
		// // e.getMessage());
		// }
	}

	// public void loadFromFile() {
	// Gson gson = new Gson();
	// try (FileReader reader = new FileReader(save_file_name)) {
	// // FIXME: maybe i need do a for loop.. for this or
	// // maybe on save(i think its on save)
	// Type listType = new TypeToken<ArrayList<camp>>() {
	// }.getType();
	// campfires = gson.fromJson(reader, listType);
	// System.out.println("Successfully loaded JSONArray from file.");
	// System.out.println(new
	// GsonBuilder().setPrettyPrinting().create().toJson(campfires));
	// System.out.println(campfires.getClass().getName());
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	// public void saveToFile() {
	// Gson gson = new GsonBuilder().setPrettyPrinting().create();
	// try (FileWriter writer = new FileWriter(save_file_name, false)) {
	// gson.toJson(campfires, writer);
	// System.out.println("Successfully saved object to file.");
	// System.out.println(new
	// GsonBuilder().setPrettyPrinting().create().toJson(campfires));
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	@EventHandler
	public void onServerStart(ServerLoadEvent ev) {
		System.out.println("\033[0;31m " + new GsonBuilder().setPrettyPrinting().create().toJson(campfires));
		loadFromFile();
		System.out.println("\033[0;31m " + new GsonBuilder().setPrettyPrinting().create().toJson(campfires));
	}

	class Owner_settings {
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

	class Camp {
		vecPos pos;
		// TODO: add a dimentional check
		String dimension;
		private int radiusTimer; // NOTE: currently not in use
		String owner;
		boolean pick_crops, live_stock;

		// TODO: implement the claim radius indicator
		// so players can see the size of a claim

		// public void campRadiusEffect(Location loc, Player p) {
		// // Calculate the circumference of the circle
		// double circumference = 2 * Math.PI * default_radius;
		// // Divide the circumference by the number of particles to create
		// int particlesPerLoop = 50;
		// double degreeIncrement = 360 / particlesPerLoop;
		// // Create the particle effect
		// ParticleEff smoke = Particle.END_ROD;
		// for (double degrees = 0; degrees < 360; degrees += degreeIncrement) {
		// double radians = Math.toRadians(degrees);
		// double x = loc.getX() + default_radius * Math.cos(radians);
		// double z = loc.getZ() + default_radius * Math.sin(radians);
		// Location particleLocation = new Location(loc.getWorld(), x, loc.getY(), z);
		// smoke.display(particleLocation, p);
		// }
		// }
		public boolean samePos(Location loc) {
			// FIXME: on claim camp check if there is another claimed camp within the radius
			if (this.pos.x == loc.getX()) {
				if (this.pos.y == loc.getY()) {
					if (this.pos.z == loc.getZ()) {
						return true;
					}
				}
			}
			return false;
		}

		// TODO: check if location is a camp.. aka the block
		public Camp findCamp(Location loc) {
			if (campfires.size() > 0) {
				for (Camp c : campfires) {
					if (c.withInRadius(loc, default_radius)) {
						return c;
					}
				}
			}
			// not a camp
			return null;
		}

		// this claims the camp
		public void claimCamp(Location loc, Player p) {
			Camp camp = findCamp(loc);
			if (camp != null) {
				if (camp.owner == null) {
					Boolean intercepts = false;
					for (Camp c : campfires) {
						World cWorld = Bukkit.getWorld(c.dimension);
						Location cLocation = new Location(cWorld, (double) c.pos.x, (double) c.pos.y, (double) c.pos.z);
						// FIXME: broken should not be able to claim intercepting/ overlapping
						if (c != camp) {
							if (camp.withInRadius(cLocation, default_radius * 2)) {
								if (camp.owner == p.getName()) {
									Audience.audience(p).sendActionBar(
											() -> Component
													.text(
															String.format(ChatColor.GOLD
																	+ String.format("Your claims are overlapping.. claim anyways.", c.owner))));
								} else {
									intercepts = true;
									Audience.audience(p).sendActionBar(
											() -> Component
													.text(String
															.format(
																	ChatColor.RED
																			+ String.format("Sorry, did not claim. overlaps %s's claim.", c.owner))));
								}
							}
						}
					}
					if (intercepts == false) {
						camp.owner = p.getName();
						Audience.audience(p).sendActionBar(
								() -> Component.text(String.format(ChatColor.GOLD + "Congrats, you have claimed this camp!")));
						this.defineOwnerSettings(p); // YES?
						// should maybe tell the player how many claims they have now.
					}
				} else {
					// already claimed
					// FIXME: somehow after claiming this code runs..
					String message = "This has already been claimed, by";
					if (camp.owner == p.getName()) {
						Audience.audience(p).sendActionBar(
								() -> Component.text(String.format(ChatColor.GOLD + "%s you!", message)));
					} else {
						Audience.audience(p).sendActionBar(
								() -> Component.text(String.format(ChatColor.GOLD + "%s %s", message, camp.owner)));
					}
				}

			}
		}

		public boolean isAdmin(Location loc, Player p) {
			Camp camp = findCamp(loc);
			if (camp != null) {
				if (p.isOp()) {
					Audience.audience(p).sendActionBar(
							() -> Component.text(
									String.format(ChatColor.GRAY + String.format("This claim belongs to %s", camp.owner))));
					return true;
				}
			}
			return false;
		}

		public boolean isFriend(Location loc, Player p) {
			Camp camp = findCamp(loc);
			if (camp != null) {
				for (Owner_settings o : owners) {
					if (camp.owner == o.name) {
						for (String f : o.friends) {
							if (f == p.getName()) {
								return true;// this is a friend
							}
						}
						break;
					}
				}
			}
			return false;
		}

		public void radiusTick() {
			if (campfires.size() > 0) {
				for (Camp c : campfires) {
					if (c.radiusTimer > 0) {
						// int new_value = c.radiusTimer--;
						c.radiusTimer--;
						// return this.radiusTimer--;
						// this.radiusTimer = new_value;
						System.out.print(String.format("timer at: 0", c.radiusTimer));
					}

				}

			}
		}

		public boolean isOwner(Location loc, Player p) {
			var camp = findCamp(loc);
			if (camp != null) {
				if (camp.owner == p.getName()) {
					return true;
				}

			}
			return false;
		}

		// this check is owner has settings, if not it creates it
		private void defineOwnerSettings(Player p) {
			boolean ownerIsDefined = false;
			if (owners.size() > 0) {
				for (Owner_settings o : owners) {// get all settings
					if (o.name == p.getName()) {
						ownerIsDefined = true;
						break;
					}
				}
			}
			if (!ownerIsDefined) {
				Owner_settings new_owner = new Owner_settings();
				new_owner.friends = new ArrayList<String>();
				new_owner.name = p.getName();
				owners.add(new_owner);
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

		public void removeFriends(Location loc, Player p) {
			// to remove friend, hit campfire with shield in hand
			Camp camp = findCamp(loc);
			if (camp != null) {
				if (camp.owner == p.getName()) {
					for (Owner_settings o : owners) {
						if (o.isOwner(p)) {
							ArrayList<String> clear_Friends = new ArrayList<>();
							o.friends = clear_Friends;
							Audience.audience(p).sendActionBar(
									() -> Component.text(
											String.format(ChatColor.RED + String.format("All friends have been removed."))));
						}
					}
				}
			}
		}

		public void addFriend(Location loc, Player p, Player d) {
			Camp camp = findCamp(loc);
			if (camp != null) {
				if (camp.owner == p.getName()) {
					// the attacker is the camp site owner
					// should not need to check the size of owners
					for (Owner_settings o : owners) {
						boolean contains_friend = false;
						for (String f : o.friends) {
							if (f == d.getName()) {
								Audience.audience(p).sendActionBar(
										() -> Component.text(
												String.format(ChatColor.GRAY + String.format("%s, is already a friend.", d.getName()))));
							}
						}
						if (contains_friend == false) {
							o.friends.add(d.getName());
							Audience.audience(p).sendActionBar(
									() -> Component.text(
											String.format(ChatColor.BLUE + String.format("Added %s as a friend.", d.getName()))));
							Audience.audience(d).sendActionBar(
									() -> Component.text(
											String
													.format(ChatColor.BLUE + String.format("%s, has added you as a friend.", p.getName()))));
						}
					}
				} else {
					// Audience new_audience = new Audience();
					Audience.audience(p).sendActionBar(
							() -> Component.text(String.format(ChatColor.GRAY + "The claim must be your's, to add friend.")));
				} // else do nothing
			}
			// if campfire 0, do nothing
		}

		public boolean hasPermission(Location loc, Player p, boolean blockBreak) {
			Camp camp = findCamp(loc);
			if (camp != null) {
				if ((camp.owner == null) || (camp.owner == p.getName())) {
					// has no owner or is owner
					return true; // you can build/break
				} else {
					for (Owner_settings o : owners) {
						if (o.name == camp.owner) {
							if (o.friends != null) {
								for (String f : o.friends) {
									if (f == p.getName()) {
										return true;
									}
								}
							}
						}
					}
				}
				// there is a camp owner and an owner nd player is not it
				return false;
			}
			// because we are not in a claim and i need to break blocks
			if (blockBreak == true) {
				return true;
			}
			// if no camps
			return false;
		}

		private boolean withInRadius(Location loc, int radius) {
			// checks if block is with the claim
			if (land.getDistance(this.pos.x, this.pos.z, loc.blockX(), loc.getBlockZ()) <= radius) {
				if (this.dimension == loc.getWorld().getName()) {
					return true;
				}
			}
			return false;
		}

	}

	// this is when a block is destroyed.. by any means (even explosoins)
	// TODO: make sure that this does not affect claimed/protected camps
	@EventHandler
	public void onNonePlayerDestroy(BlockDestroyEvent ev) {
		Location loc = ev.getBlock().getLocation();
		// for (Camp c : campfires) {
		// if (c.samePos(loc)) {
		// campfires.remove(c);
		// System.out.print(String.format("campfire %s was removed, campfires are now:
		// %s", c, campfires.size()));
		// }
		// }
		Camp camp = camper.findCamp(loc);
		if (camp != null) {
			if (camp.owner != null) {
				// this should not break the camp because its claimed
				ev.setCancelled(true);
			} else {
				if (camp.samePos(loc)) {
					campfires.remove(camp);
				}
			}
		}
	}

	// this should prevent random explosions entity explosions within claim
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent ev) {
		Camp camp = camper.findCamp(ev.getLocation());
		if (camp != null) {
			if (camp.owner != null) {
				if (camp.withInRadius(ev.getLocation(), default_radius * 2)) {// defaut radius + the size of
																																			// the explosion
					ev.blockList().clear();
					// ev.setCancelled(true);
				}
			}
		}
	}

	// @EventHandler
	// public void onExplosionPrime(ExplosionPrimeEvent ev) {
	// // int radius = (int) ev.getRadius();
	// Location loc = ev.getEntity().getLocation();
	// Camp camp = camper.findCamp(loc);
	// if (camp != null) {
	// // TODO:MAKE sure this is withinradius is not needed here
	// // if (camp.withInRadius(loc, default_radius)) {
	// if (camp.owner != null) {
	// ev.setCancelled(true);
	// } else {
	// if (camp.withInRadius(loc, (int) ev.getRadius())) {
	// campfires.remove(camp);
	// }
	// }
	// // }
	// }
	// }

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		Player player = ev.getPlayer();
		Block block = ev.getBlock();
		boolean destroy = false;
		Camp camp = camper.findCamp(ev.getBlock().getLocation());
		if (camp != null) { // if in camp
			if (camp.hasPermission(ev.getBlock().getLocation(), player, true) == true) {
				if (block.getType().toString().contains("BANNER")) {
					// claim has no owner.. so claim it
					camper.claimCamp(block.getLocation(), player);
				}
			} else {
				destroy = true;
			}
		} else {
			if (block.getType() == Material.CAMPFIRE) {
				Camp new_camp = new Camp();
				vecPos pos = new vecPos();
				pos.x = block.getX();
				pos.y = block.getY();
				pos.z = block.getZ();
				new_camp.pos = pos;
				new_camp.dimension = block.getWorld().getName();
				boolean sameSpot = false;
				if (campfires.size() != 0) {
					for (Camp c : campfires) {
						if (c.samePos(block.getLocation())) {
							sameSpot = true;
							break;
						}
					}
				}
				if (sameSpot) {
				} else {
					Audience.audience(player).sendActionBar(
							() -> Component.text(String.format(ChatColor.GRAY + "Camp placed. consider claiming it.")));
					campfires.add(new_camp);
					// SAVE starts here
					saveToFile();
				}
			} else if ((block.getType().toString().contains("LOG")) || (block.getType() == Material.CRAFTING_TABLE)
					|| (block.getType() == Material.LADDER) ||
					(block.getType().toString().contains("SHULKER")) || (block.getType().toString().contains("TORCH"))
					|| (block.getType().toString().contains("LANTERN")) || (block.getType() == Material.FURNACE)
					|| (block.getType() == Material.SCAFFOLDING)) {
				// // can place it
			} else {
				destroy = true;
			}
		}
		if (destroy) {
			if (camp != null) {
				Audience.audience(player).sendActionBar(
						() -> Component
								.text(String.format(ChatColor.GRAY + "Sorry, Camp is owner by %s", camp.owner)));
			} else {
				Audience.audience(player).sendActionBar(
						() -> Component.text(String.format(ChatColor.GRAY + "Need to be near a camp.")));
			}
			// player.dropItem(true);
			// ev.getBlock().breakNaturally(true); // TODO: may have to relook at this
			player.spawnParticle(Particle.CRIT_MAGIC, block.getLocation(), 10, 1, 1, 1);
			player.playSound(block.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.BLOCKS, 1, 1);
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockDestroy(BlockBreakEvent ev) {
		Block block = ev.getBlock();
		Player player = ev.getPlayer();
		if (camper.hasPermission(block.getLocation(), player, true) == false) {
			ev.setCancelled(true);
		}
		if (block.getType() == Material.CAMPFIRE) {
			if (campfires.size() > 0) {
				for (Camp c : campfires) {
					if (c.samePos(block.getLocation())) {
						// prevents friends from breaking campfire/claim
						if (c.isFriend(ev.getBlock().getLocation(), player)) {
							ev.setCancelled(true);
						} else {
							campfires.remove(c);
							if (c.owner != null) {
								Audience.audience(player).sendActionBar(
										() -> Component.text(String.format(ChatColor.GRAY + "You have removed your claim")));
							} else {
								Audience.audience(player).sendActionBar(
										() -> Component.text(String.format(ChatColor.GRAY + "Camp removed")));
							}
							saveToFile();
							break;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent ev) {
		Entity attacker = ev.getDamager();
		Entity damaged = ev.getEntity();
		if (attacker instanceof Player) {
			if (damaged instanceof Player) {
				Player player = (Player) attacker;
				Player damagedPlayer = (Player) damaged;
				if (player.getInventory().getItemInMainHand().getType() == Material.SHIELD) {
					player.sendMessage("yes this should be running");
					camper.addFriend(damagedPlayer.getLocation(), player, damagedPlayer);
				}
			} else {
				Camp camp = camper.findCamp(damaged.getLocation());
				if (camp != null) {
					if (camp.hasPermission(damaged.getLocation(), (Player) attacker, false) == false) {
						// NOTE: this is where attack mobs claim permission comes in.
						// i dont think that FALSE matters, we will see
						// FIXME: this completly prevent killing anything in a claim
						switch (damaged.getType()) {
							case BLAZE:
							case CAVE_SPIDER:
							case CREEPER:
							case ELDER_GUARDIAN:
							case ENDERMAN:
							case ENDERMITE:
							case EVOKER:
							case GHAST:
							case GIANT:
							case GUARDIAN:
							case HOGLIN:
							case HUSK:
							case MAGMA_CUBE:
							case PIGLIN:
							case PIGLIN_BRUTE:
							case PILLAGER:
							case POLAR_BEAR:
							case SHULKER:
							case SILVERFISH:
							case SKELETON:
							case SLIME:
							case SPIDER:
							case STRAY:
							case VEX:
							case VINDICATOR:
							case WITCH:
							case WITHER:
							case WITHER_SKELETON:
							case ZOGLIN:
							case ZOMBIE:
								// continue
							default:
								Audience.audience(attacker).sendActionBar(
										() -> Component.text(
												String.format(ChatColor.RED + "%s is inside %s's claim",
														damaged.getType().toString().replace("_", " "), camp.owner)));
								ev.setCancelled(true);
								break;
						}

					}
				}
			}
		}

	}

	@EventHandler
	public void onPlayerIntEntity(PlayerInteractEntityEvent ev) {
		Player player = ev.getPlayer();
		if (ev.getRightClicked() instanceof AbstractHorse) {
			AbstractHorse horse = (AbstractHorse) ev.getRightClicked();
			if (horse.getOwner() != null) {
				if (horse.getOwner().getName() != player.getName()) {
					ev.setCancelled(true);
					Audience.audience(player).sendActionBar(
							() -> Component.text(
									String.format(ChatColor.RED + String.format("Horse belongs to %s.", horse.getOwner().getName()))));
				}

			}
		}
	}

	@EventHandler
	public void onPlayerStartBreaking(PlayerInteractEvent ev) {
		Player player = ev.getPlayer();
		Block blockClicked = ev.getClickedBlock();
		// get item in hand
		if (blockClicked == null) {
			// go on live life
		} else {
			Camp camp = camper.findCamp(blockClicked.getLocation());
			if (camp != null) {
				// FIXME(did it?): may also prevet using doors and redstone.. may need to add a
				// perm thing to allow or not allow
				// redstone ussage
				if (camp.hasPermission(blockClicked.getLocation(), player, true) == false) {
					Audience.audience(player).sendActionBar(
							() -> Component.text(
									String.format(ChatColor.RED + "Sorry but this is %s's claim", camp.owner)));
					ev.setCancelled(true);
				}
				// this should prevent claimed land from being trampled
				if (blockClicked.getType() == Material.FARMLAND) {
					ev.setCancelled(true);
				}

			}
			if (blockClicked.getType() == Material.CAMPFIRE) {
				if (player.getInventory().getItemInMainHand().getType() == Material.SHIELD) {
					camper.removeFriends(player.getLocation(), player);
				}
				// TODO: do the onclick display how to do functions
			}
		}
	}
}
