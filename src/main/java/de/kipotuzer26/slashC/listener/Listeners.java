package de.kipotuzer26.slashC.listener;

import de.kipotuzer26.slashC.Main;
import de.kipotuzer26.slashC.commands.SlashC;
import de.kipotuzer26.slashC.util.PrimarySkinColor;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static de.kipotuzer26.slashC.commands.SlashC.*;

public class Listeners implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!SlashC.isInMode(e.getPlayer())) { return; }

        Block b = e.getPlayer().getEyeLocation().getBlock();
        boolean isWaterLogged = false;
        if(b.getBlockData() instanceof Waterlogged){
            Waterlogged wl = (Waterlogged) b.getBlockData();
            isWaterLogged = wl.isWaterlogged();
        }

        Material[] water = {Material.SEA_PICKLE, Material.SEAGRASS, Material.KELP_PLANT, Material.TALL_SEAGRASS};

        if (e.getPlayer().getEyeLocation().getBlock().getType() == Material.WATER ||
                Arrays.asList(water).contains(e.getPlayer().getEyeLocation().getBlock().getType())|| isWaterLogged) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 100000, 255, true, false, false));
        } else {
            e.getPlayer().removePotionEffect(PotionEffectType.CONDUIT_POWER);
        }
        if (e.getPlayer().getEyeLocation().getBlock().getLightFromSky() == 0 && !e.getPlayer().getEyeLocation().getBlock().getType().isOccluding()) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 255, true, false, false));
        } else {
            e.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
        }

        if (e.getPlayer().isOp()) { return; }

        if (e.getPlayer().getEyeLocation().getBlock().getType().isOccluding()) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100000, 255, true, false, false));
        } else {
            e.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
        }
        int max = 100;
        if (e.getTo().distance((Location)SlashC.playersInSpectator.get(e.getPlayer().getUniqueId())) > max) {
            Location center = (Location)SlashC.playersInSpectator.get(e.getPlayer().getUniqueId());
            e.getPlayer().spawnParticle(Particle.EXPLOSION_NORMAL, e.getTo().toVector().subtract(center.toVector()).normalize().multiply(max + 5).add(center.toVector()).toLocation(e.getPlayer().getWorld()), 20);
            e.getPlayer().setVelocity(((Location)SlashC.playersInSpectator.get(e.getPlayer().getUniqueId())).toVector().subtract(e.getTo().toVector()).normalize());
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_ELYTRA, 0.5F, 0.5F);
            renderParticles(e.getPlayer(), e.getTo(), center, max);
        }


    }


    private void renderParticles(Player player, Location playerL, Location center, int max) {
        Vector v = playerL.toVector().subtract(center.toVector());
//        showVector(v, player, center);
//        showPlaneVector(v, player, center);
        //renders Plane
//        for(float x = -10;x<10; x+=1f) {
//           for (float y = -10; y < 10; y += 1f) {
//                particle(planee_VCoordinatesTo3d(v, x, y), player, center, max);
//            }
//        }
        //renders filled circle
//        int s = 2;
//        for (int i = 1; i < 20; i++){
//            for (int x = 0; x < 360; x += 360/(i*4)) {
//                    particle(planee_VCoordinatesTo3d(v, Math.sin(Math.toRadians(x))*i*s,  Math.cos(Math.toRadians(x))*i*s), player, center, max);
//            }
//        }

        class RenderParticles extends BukkitRunnable {
            private int count = 1;
            @Override
            public void run() {
                if (count < 20) {
                    int s = 2;
                    // Your task logic here
                    for (int x = 0; x < 360; x += 360/(count*4)) {
                        particle(planee_VCoordinatesTo3d(v, Math.sin(Math.toRadians(x))*count*s,  Math.cos(Math.toRadians(x))*count*s), player, center, max);
                    }
                    // Increment the count
                    count++;
                } else {
                    // When the task has run 20 times, cancel it
                    cancel();
                }
            }
        }

        new RenderParticles().runTaskTimer(Main.getPlugin(), 0, 2);
    }

    private void particle(Vector v, Player player, Location center, int max){
      player.spawnParticle(Particle.DRIP_LAVA, v.clone().normalize().multiply(max+5).add(center.toVector().clone()).toLocation(player.getWorld()), 1);
//      player.spawnParticle(Particle.DRIP_LAVA, center.clone().add(v),1);
    }

    private void showVector(Vector v, Player player, Location center){
        for(float x = 0; x<1; x+= 0.05f){
            player.spawnParticle(Particle.DRIP_LAVA, center.clone().add(v.clone().multiply(x)),1);
        }
    }

    private void showPlaneVector(Vector v, Player player, Location center){
        Vector vx= new Vector(0, 0-v.getZ(), v.getY()).normalize().multiply(1.5);
        Vector vy= new Vector(0-v.getY(), v.getX(), 0).normalize().multiply(1.5);
        for(float x = 0; x<1; x+= 0.05f){
            player.spawnParticle(Particle.ELECTRIC_SPARK, center.clone().add(vx.clone().multiply(x).add(v.clone().multiply(1.1f))),1);
            player.spawnParticle(Particle.ELECTRIC_SPARK, center.clone().add(vy.clone().multiply(x).add(v.clone().multiply(1.1f))),1);


        }
    }

    private Vector planee_VCoordinatesTo3d(Vector v, double x, double y){
//        // X=v*0.9+x*[0,-vz, vy]+y*[-vx,vy,0]
//        return v.clone().multiply(1.1f)
//                .add(new Vector(0, 0-v.getZ(), v.getY()).normalize().multiply(x))
//                .add(new Vector(0-v.getY(), v.getX(), 0).normalize().multiply(y));

        return v.clone().multiply(1.1f)
                .add(new Vector(0, 0-v.getZ(), v.getY()).normalize().multiply(x))
                .add(new Vector(0, 0-v.getZ(), v.getY()).crossProduct(v).normalize().multiply(y));
    }


    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        PrimarySkinColor.playerColor.remove(e.getPlayer().getUniqueId());
//        System.out.println("removed Player from Buffer");
        if(e.getPlayer().getVehicle() != null) {
            if (SlashC.ridingEntity.containsValue(e.getPlayer().getVehicle().getUniqueId()) && !isInMode(e.getPlayer())) {
                e.getPlayer().leaveVehicle();
            }
        }
        if (SlashC.isInMode(e.getPlayer())) {
            SlashC.removePlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        PrimarySkinColor.loadColor(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (SlashC.isInMode(e.getPlayer()) && !e.getPlayer().isOp()) {
            if (e.getCause() != TeleportCause.PLUGIN) {
                e.getPlayer().sendMessage(PREFIX + ChatColor.RED + "You are not allowed to teleport right now");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void vehicleMove(VehicleMoveEvent e){
        if(!SlashC.ridingEntity.containsValue(e.getVehicle().getUniqueId())){
            return;
        }
        SlashC.playersInSpectator.replace(getKeyByValue(ridingEntity, e.getVehicle().getUniqueId()), e.getVehicle().getLocation());
    }

    @EventHandler
    public void onSpectator(PlayerCommandPreprocessEvent e){
        if(!(e.getMessage().contains("gamemode")&&e.getMessage().split(" ").length == 2)){ return;}
            if(e.getPlayer().isOp() && !SlashC.isInMode(e.getPlayer()) && !e.getMessage().contains("spectator")){return;}
            e.setCancelled(true);
            if(e.getPlayer().isOp()){
                e.getPlayer().sendMessage(PREFIX + ChatColor.GREEN + "Für Vanilla Spectator: /gamemode specator <Nutzer>");
            }
            SlashC.onCommand(e.getPlayer());

    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}