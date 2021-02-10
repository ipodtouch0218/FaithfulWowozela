package me.ipodtouch0218.wowozela;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_13;
import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;

public class Wowozela extends JavaPlugin {

	private static final double PARTICLE_DIST = 3d, LINE_MIN_DIST = 0.1;
	public HashMap<UUID, Sound> sounds = new HashMap<>();
	private HashMap<UUID, Location> lastParticles = new HashMap<>();
	private HashMap<UUID, Float> lastPitch = new HashMap<>();
	
	private ParticleNativeAPI particleApi;
	private Particles_1_8 oneEight;
	private Particles_1_13 oneThirteen;
	
	@Override
	public void onEnable() {
		
		//Commands
		CmdWowozela cmd = new CmdWowozela(this);
		getCommand("wowozela").setExecutor(cmd);
		getCommand("wowozela").setTabCompleter(cmd);
		
		//Particle API
		initParticles();
		
		//save me from my sins
		new BukkitRunnable() {
			public void run() {
				
				for (Player pl : Bukkit.getOnlinePlayers()) {
					
					if (!pl.isSneaking() || sounds.get(pl.getUniqueId()) == null) {
						lastParticles.put(pl.getUniqueId(), null);
						continue;
					}
					
					double pitch = pl.getEyeLocation().getPitch() + 90;
					float h = (float) (pitch * 2f);
					float h2 = lastPitch.getOrDefault(pl.getUniqueId(), h);
					
					Vector start = pl.getEyeLocation().toVector();
					Vector end = pl.getEyeLocation().add(pl.getLocation().getDirection().multiply(PARTICLE_DIST)).toVector();
					
					Vector dir = end.clone().subtract(start).normalize().multiply(LINE_MIN_DIST);
					
					while (end.distance(start) > LINE_MIN_DIST) {
						Block target = start.add(dir).toLocation(pl.getWorld()).getBlock();
						try {
							if (!target.isPassable() && target.getBoundingBox().contains(start)) {
								start.subtract(dir);
								break;
							}
						} catch (Error e) {
							if (target.getType().isSolid()) {
								start.subtract(dir);
								break;
							}
						}
					}
					Location particleLoc = start.toLocation(pl.getWorld());
					
					Location lastParticleLoc = lastParticles.get(pl.getUniqueId());
					if (lastParticleLoc != null) {
						start = lastParticleLoc.toVector();
						end = particleLoc.toVector();
						
						double dist = start.distance(end);
						double count = 0;
						
						dir = end.clone().subtract(start).normalize().multiply(LINE_MIN_DIST);
						
						while (end.distanceSquared(start) > LINE_MIN_DIST*LINE_MIN_DIST) {
							java.awt.Color clr = new java.awt.Color(java.awt.Color.HSBtoRGB((float) ((h*(count/dist) + h2*(1-(count/dist)))/360f), 1f, 1f));
							spawnParticle(start.add(dir).toLocation(lastParticleLoc.getWorld()), Color.fromRGB(clr.getRed(), clr.getGreen(), clr.getBlue()));
							count += LINE_MIN_DIST;
						}
					}
					java.awt.Color clr = new java.awt.Color(java.awt.Color.HSBtoRGB(h/360f, 1f, 1f));
					spawnParticle(particleLoc, Color.fromRGB(clr.getRed(), clr.getGreen(), clr.getBlue()));
					lastPitch.put(pl.getUniqueId(), h);
					lastParticles.put(pl.getUniqueId(), particleLoc);
					particleLoc.getWorld().playSound(particleLoc, sounds.get(pl.getUniqueId()), 1f, (float) (2-((pitch/180d)*1.5)));
				}
				
			}
		}.runTaskTimer(this, 0, 1);
	}
	
	private void initParticles() {
		ParticleNativeAPI api = ParticleNativeCore.loadAPI(this);
		try { 
			oneEight = api.getParticles_1_8();
		} catch (Exception e) {
			
		}
		try {
			oneThirteen = api.getParticles_1_13();
		} catch (Exception e) {
			
		}
	}
	
	private void spawnParticle(Location loc, Color color) {
		Object packet;
		if (oneEight != null) {
			packet = oneEight.REDSTONE().packetColored(true, loc, color);
			oneEight.sendPacket(loc, 60, packet);
		} else if (oneThirteen != null) {
			packet = oneThirteen.DUST().color(color, 1f).packet(true, loc);
			oneThirteen.sendPacket(loc, 60, packet);
		}
	}
	
	public static String getVersion() {
	    String packageName = Bukkit.getServer().getClass().getPackage().getName();
	    return packageName.substring(packageName.lastIndexOf('.') + 1);
	}
}
