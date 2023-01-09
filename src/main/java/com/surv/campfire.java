package com.surv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerLoadEvent;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

//*This is a land claim plugin.. why make another one of these plugins?
// the reaason being that all the other ones require the player to use commands
// whether in one way or another.
//-main things are "no typing commands"..unless admin stuff

//NOTES: the campfire makes the most sense as "the block"
//it does not stack.. meaning tou wont really just have lots in the inventory.

//FIXME(): im too dumb, can't figure out of to load/save correctly

//[NEEDS TESTING]

//[DOING]
//TODO: clean up code
//FIXME: on camp claim check if there is another claimed camp within the radius
//this is not working correctly
//get the distance of a camp and check to see if the next one is closes.
//if it is then take that camp and keep checking if anything is smaller.

//TODO: prevent harming animals in claims
//TODO: add more sounds/effects/and messages.

//TODO:NOTE(Not sure if done..): Make is so admin.. or player with tag
//can remove camps.. or doing anything in the camp

//[NEXT]
//TODO: add comfig options.
//--prevent placing(can only place blocks within camp)
//TODO: possibly change the way that adding friends is handled.
//TODO: some way for the player to know where their previus claims are.
//TODO: implement a way to trade horses.. currently horses are locked to player who tamed them.
//TODO: figure out how to increase the size of a claim

//TODO(With claims being as big as they are this may not be a good idea): 
//somehow figure out how to deal with per camp settings..
//aka [can pick crops] [can kill live stock]
//**This can actually be part of the camp settings
//NOTE: for picking crops.. let player fully break and have
//them replant it
//possible ways to do this.. place chest or block below campfire to
//signiffying what can be done in the claim.

//TODO: implement so that somehow the player can
//teleport to their claim with the Magic Mirror
//also tp to friends claims

//[not sure]
//TODO: prevent claiming if another player is in the camp's radius
//and is not friends with the player
//TODO(maybe): maybe do block placing differently..
//instead of canceling it, have it be like
//how adventure mode dose it.
//TODO:(maybe): maybe blocks in claims take multiple breaks
//to break.. show the player how many more times they need to
//braek the block for it to break.
//maybe the player needs to click the block with iron in hand
//to re-enforce it.. idk.

//TODO: figure out sql or some other save type

//TODO(yea i cant figure out how to use a timer): when the player punches the campfire display a radius

//NOTE: i was gonna make its so you could not place stuff with buckets outside
//of camps, but that should just be left to claims.

public class campfire implements Listener {

	static int default_radius = 22; // NOTE: this needs to be increased after testing
	int maxClaims = 3;
	ArrayList<Owner_settings> owners = new ArrayList<>(); // NEEDS SAVING
	Camp camper = new Camp();

	ArrayList<Camp> campfires = new ArrayList<>(); // NEEDS SAVING
	String saveFileName = "saved_camps.json";

	public void saveToFile() {
		// Gson gson = new Gson();
		// try (FileWriter writer = new FileWriter(saveFileName)) {
		// gson.toJson(campfires, writer);
		// } catch (IOException o) {
		// }
	}

	public void loadFromFile() {
		// Gson gson = new Gson();
		// try (FileReader reader = new FileReader(saveFileName)) {
		// Type arrayType = new TypeToken<ArrayList<Camp>>() {
		// }.getType();
		// campfires = gson.fromJson(reader, arrayType);
		// } catch (IOException o) {
		// }
	}

	@EventHandler
	public void onServerStart(ServerLoadEvent ev) {
		System.out.println(String.format("Does this not work %s", campfires.toString()));
		loadFromFile();
		System.out.println(String.format("Does this not work %s", campfires.toString()));
	}

	class Owner_settings {
		String playerName;
		ArrayList<String> friendsList;
		int numberOfClaims;

		public boolean isOwner(Player p) {
			if (playerName == p.getName()) {
				return true;
			}
			return false;
		}

		public boolean isFriend(Player p) {
			// check if the player is a friend
			for (String friendName : friendsList) {
				if (friendName == p.getName()) {
					return true;
				}

			}
			return false;
		}
	}

	class Camp {
		String dimensionName;
		int posX;
		int posY;
		int posZ;
		int radiusTimer; // NOTE: currently not in use
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
			if (posX == loc.getX()) {
				if (posY == loc.getY()) {
					if (posZ == loc.getZ()) {
						return true;
					}
				}
			}
			return false;
		}

		public Camp findCamp(Location loc) {
			// FIXME: Should be Nearest camp
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
						World cWorld = Bukkit.getWorld(c.dimensionName);
						Location cLocation = new Location(cWorld, (double) c.posX, (double) c.posY, (double) c.posZ);
						if (camp != c) {
							if (camp.withInRadius(cLocation, default_radius * 2)) {

								if (c.owner == p.getName() && camp.owner == p.getName()) {
									Audience.audience(p).sendActionBar(
											() -> Component
													.text(
															String.format(ChatColor.GOLD
																	+ String.format("Your claims are overlapping.. claim anyways.", c.owner))));
								} else if (c.owner != null) {
									System.out.println("is this working? there is an owner");
									intercepts = true;
									Audience.audience(p).sendActionBar(
											() -> Component
													.text(String
															.format(
																	ChatColor.RED
																			+ String.format("Sorry, did not claim. overlaps %s's claim.", c.owner))));
									p.sendMessage(String.format(ChatColor.RED + "Camp is overlapping with %s's claim", c.owner,
											camp.posX, camp.posY,
											camp.posZ));
								}
							}
						}
					}
					if (intercepts == false) {
						defineOwnerSettings(p); // YES?
						for (Owner_settings o : owners) {
							if (o.playerName == p.getName()) {
								if (o.numberOfClaims < maxClaims) {
									o.numberOfClaims += 1;
									camp.owner = p.getName();
									Audience.audience(p).sendActionBar(
											() -> Component.text(String.format(ChatColor.GOLD + "Congrats, you have claimed this camp!")));
									p.sendMessage(
											String.format(ChatColor.GOLD + "Nice! Only you and your friends can build here now.", camp.posX,
													camp.posY,
													camp.posZ));
									p.sendMessage(ChatColor.AQUA + "Interact with the campfire for more info.");
								} else {
									Audience.audience(p).sendActionBar(
											() -> Component.text(String.format(ChatColor.RED + "Hey! slow down there.. too many claims.")));
									p.sendMessage(
											String.format(ChatColor.RED + "Looks like you've reached the limit on claims %s", maxClaims));
								}
							}
						}
					}
				} else {
					// already claimed
					// FIXME: somehow after claiming this code runs..
					String message = "This has already been claimed, by";
					if (camp.owner == p.getName()) {
						Audience.audience(p).sendActionBar(
								() -> Component.text(String.format(ChatColor.GRAY + "%s you!", message)));
					} else {
						Audience.audience(p).sendActionBar(
								() -> Component.text(String.format(ChatColor.RED + "%s %s", message, camp.owner)));
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
					if (camp.owner == o.playerName) {
						for (String f : o.friendsList) {
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
		public void defineOwnerSettings(Player p) {
			boolean ownerIsDefined = false;
			if (owners.size() > 0) {
				for (Owner_settings o : owners) {// get all settings
					if (o.playerName == p.getName()) {
						ownerIsDefined = true;
						break;
					}
				}
			}
			if (!ownerIsDefined) {
				Owner_settings new_owner = new Owner_settings();
				new_owner.friendsList = new ArrayList<String>();
				new_owner.playerName = p.getName();
				owners.add(new_owner);
			}
		}

		// PICK_CROPS.. prevents leaving empty
		// LIVE_STOCK.. prevents leaving less than 2
		// FIXME: these may not actaully work
		// considering that a mob may be left underground
		// somewhere
		public boolean canPickCrops() {
			if (pick_crops) {
				return true;
			}
			return false;
		}

		public boolean canKillLiveStock() {
			if (live_stock) {
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
							o.friendsList = clear_Friends;
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
						for (String f : o.friendsList) {
							if (f == d.getName()) {
								Audience.audience(p).sendActionBar(
										() -> Component.text(
												String.format(ChatColor.GRAY + String.format("%s, is already a friend.", d.getName()))));
							}
						}
						if (contains_friend == false) {
							o.friendsList.add(d.getName());
							Audience.audience(p).sendActionBar(
									() -> Component.text(
											String.format(ChatColor.AQUA + String.format("Added %s as a friend.", d.getName()))));
							Audience.audience(d).sendActionBar(
									() -> Component.text(
											String
													.format(ChatColor.AQUA + String.format("%s, has added you as a friend.", p.getName()))));
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
						if (o.playerName == camp.owner) {
							if (o.friendsList != null) {
								for (String f : o.friendsList) {
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

		public boolean withInRadius(Location loc, int radius) {
			// checks if block is with the claim
			System.out.printf("here is the xyz [%s,%s,%s]", posX, posY, posZ);
			if (getDistance(posX, posZ, loc.blockX(), loc.getBlockZ()) <= radius) {
				if (dimensionName == loc.getWorld().getName()) {
					return true;
				}
			}
			return false;
		}

	}

	public int getDistance(int x1, int z1, int x2, int z2) {
		int z = x2 - x1;
		int x = z2 - z1;
		return (int) Math.sqrt(x * x + z * z);
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
				if (camp.withInRadius(ev.getLocation(), default_radius + (ev.blockList().size() * 2))) {
					ev.blockList().clear();
					// ev.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		Player player = ev.getPlayer();
		Block block = ev.getBlock();
		boolean destroy = false;
		Camp camp = camper.findCamp(ev.getBlock().getLocation());
		Boolean campPlaced = false;

		if (block.getType() == Material.CAMPFIRE) {
			Camp new_camp = new Camp();
			new_camp.posX = block.getX();
			new_camp.posY = block.getY();
			new_camp.posZ = block.getZ();
			new_camp.dimensionName = block.getWorld().getName();
			boolean sameSpot = false;
			if (camp != null) {
				if (camp.hasPermission(ev.getBlock().getLocation(), player, false) == false) {
					System.out.println("this dude does not have permission");
					destroy = true;
				}
			}
			if (destroy == true) {
				// beacuse destory dont add camp

			} else {
				if (camp != null) {
					if (camp.samePos(block.getLocation())) {
						sameSpot = true;
					}
				}
				if (sameSpot) {
				} else {
					campPlaced = true;
					Audience.audience(player).sendActionBar(
							() -> Component.text(String.format(ChatColor.GRAY + "Camp placed. consider claiming it.")));
					player.sendMessage(ChatColor.AQUA + "+Claim a camp by placing a banner nearby.");
					campfires.add(new_camp);
					// SAVE starts here
					saveToFile();
				}
			}
		} else if ((block.getType().toString().contains("LOG")) || (block.getType() == Material.CRAFTING_TABLE)
				|| (block.getType() == Material.LADDER) ||
				(block.getType().toString().contains("SHULKER")) || (block.getType().toString().contains("TORCH"))
				|| (block.getType().toString().contains("LANTERN")) || (block.getType() == Material.FURNACE)
				|| (block.getType() == Material.SCAFFOLDING)) {
			// // can place it
		} else {
			if (camp != null) { // if in camp
				if (camp.hasPermission(ev.getBlock().getLocation(), player, true) == true) {
					// claim has no owner.. so claim it
					if (block.getType().toString().contains("BANNER")) {
						camper.claimCamp(block.getLocation(), player);
					}
				} else {
					destroy = true;
				}
			} else {
				destroy = true;
			}
		}

		if (campPlaced == true) {

		} else {
			if (destroy) {
				if (camp != null) {
					Audience.audience(player).sendActionBar(
							() -> Component
									.text(String.format(ChatColor.GRAY + "Sorry, Camp is owner by %s", camp.owner)));
				} else {
					Audience.audience(player).sendActionBar(
							() -> Component.text(String.format(ChatColor.GRAY + "Missing camp.")));
				}
				// player.dropItem(true);
				// ev.getBlock().breakNaturally(true); // TODO: may have to relook at this
				player.spawnParticle(Particle.CRIT_MAGIC, block.getLocation(), 10, 1, 1, 1);
				player.playSound(block.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.BLOCKS, 1, 1);
				ev.setCancelled(true);
			}

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
								player.sendMessage(ChatColor.GRAY + "You have removed your claim");
								for (Owner_settings o : owners) {
									if (o.playerName == player.getName()) {
										o.numberOfClaims -= 1;
									}
								}
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
					// player.sendMessage("yes this should be running");
					camper.addFriend(damagedPlayer.getLocation(), player, damagedPlayer);
				}
			} else {
				Camp camp = camper.findCamp(damaged.getLocation());
				if (camp != null) {
					// TODO: implement protecting animals
					// if (camp.hasPermission(damaged.getLocation(), (Player) attacker, false) ==
					// false) {
					// // i dont think that FALSE matters, we will see
					// Audience.audience(attacker).sendActionBar(
					// () -> Component.text(
					// String.format(ChatColor.RED + "%s is inside %s's claim",
					// damaged.getType().toString().replace("_", " "), camp.owner)));
					// ev.setCancelled(true);
					// }
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
				if (camp != null) {
					if (camp.owner == player.getName()) {
						// TODO: do the onclick display how to do functions
						player.sendMessage(ChatColor.GOLD + "=====");
						player.sendMessage(ChatColor.AQUA + "+Remove a claim by breaking the campfire.");
						player.sendMessage(ChatColor.AQUA
								+ "+Add friends by giving them a pat wiht your shield. This will allow them access to your claims.");
						player.sendMessage(ChatColor.AQUA
								+ "+Remove friends giving any campfire you claimed a pat with a shield.. this will remove all friends.");
						player.sendMessage(ChatColor.GOLD + "=====");
					}
				}
			}
		}
	}
}
