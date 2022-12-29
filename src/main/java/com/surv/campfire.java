package com.surv;

import java.util.ArrayList;

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
import org.bukkit.event.entity.ExplosionPrimeEvent;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.surv.land.vecPos;

//TODO(explosion will remove the camp): need to figure how to prevent it from being destroyed
//or if destroyed removed it from camps list.

//TODO:implement presistant camp.. aka save and load on reboot
//SAVEs

//TODO(maybe): make it so blocks take a few breaks to completey delete
//if there is no campfire near

//TODO(yea i cant figure a timer): when the player punches the campfire display a radius

//NOTE: i was gonna make its so you could not place stuff with buckets outside
//of camps, but that should just be left to claims.

public class campfire implements Listener {
	ArrayList<camp> campfires = new ArrayList<>();
	public static int default_radius = 6; // TODO: this needs to be increased after testing

	class camp {
		vecPos pos;
		int radiusTimer;

		public boolean withIn(Location loc, int radius) {
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
			if (c.withIn(loc, radius)) {
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
		if (block.getType() == Material.CAMPFIRE) {
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
		} else {
			if ((block.getType().toString().contains("LOG")) || (block.getType() == Material.CRAFTING_TABLE)
					|| (block.getType() == Material.LADDER) ||
					(block.getType().toString().contains("SHULKER")) || (block.getType().toString().contains("TORCH"))
					|| (block.getType().toString().contains("LANTERN"))) {
				// // can place it
			} else {
				if (campfires.size() == 0) {
					// cancel because there are no campfires
					// ev.setCancelled(true); // cencel because not within a campfire
					destroy = true;
				} else {
					for (camp c : campfires) {
						if (c.withIn(ev.getBlock().getLocation(), default_radius)) {
							destroy = false;
							break;
						} else {
							// ev.setCancelled(true); // cencel because not within a campfire
							destroy = true;
						}
					}
				}
			}

		}
		if (destroy) {
			// ev.getBlock().setType(Material.AIR);
			ev.getBlock().breakNaturally(true); // TODO: may have to relook at this
			player.spawnParticle(Particle.CRIT_MAGIC, block.getLocation(), 10, 1, 1, 1);
			player.playSound(block.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.BLOCKS, 1, 1);
		}
	}

	@EventHandler
	public void onBlockDestroy(BlockBreakEvent ev) {
		Block block = ev.getBlock();
		if (block.getType() == Material.CAMPFIRE) {
			if (campfires.size() == 0) {
				// do nothing
			} else {
				for (camp c : campfires) {
					if (c.samePos(block.getLocation())) {
						campfires.remove(c);
						break;
					}
					// if (c.pos.x == block.getX()) {
					// if (c.pos.y == block.getY()) {
					// if (c.pos.z == block.getZ()) {
					// }
					// }
					// }
				}
			}
		}
	}
}
