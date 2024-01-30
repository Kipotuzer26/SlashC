package de.kipotuzer26.slashC.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import de.kipotuzer26.slashC.util.PrimarySkinColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

@CommandAlias("c|camera")
public class SlashC extends BaseCommand {
   public static Map<UUID, Location> playersInSpectator = new HashMap();
   private static Map<UUID, GameMode> playerGameMode = new HashMap();
   private static Map<UUID, Collection<PotionEffect>> playerPotions = new HashMap();
   public static Map<UUID, UUID> playerStands = new HashMap();
   public static Map<UUID, UUID> ridingEntity = new HashMap();
   public static final String PREFIX;

   public static boolean isInMode(Player player) {
      return playersInSpectator.containsKey(player.getUniqueId());
   }

   public static void removeAllPlayers() {
      Iterator var0 = playersInSpectator.keySet().iterator();

      while(var0.hasNext()) {
         UUID uuid = (UUID)var0.next();
         Player player = Bukkit.getPlayer(uuid);
         removePlayer(player);
      }

   }

   public static void removePlayer(Player player) {
      Iterator var1 = player.getActivePotionEffects().iterator();
      Location loc = playersInSpectator.get(player.getUniqueId());
      loc.getWorld().loadChunk(loc.getChunk());
      ArmorStand stand = (ArmorStand) Bukkit.getEntity((UUID) playerStands.get(player.getUniqueId()));
      if(stand != null) {
         stand.setHealth(0);
         stand.remove();
      } else{
         System.out.println("Error: Stand UUID: "+playerStands.get(player.getUniqueId()));
      }
      playerStands.remove(player.getUniqueId());

      while(var1.hasNext()) {
         PotionEffect effect = (PotionEffect)var1.next();
         player.removePotionEffect(effect.getType());
      }

      player.setGameMode((GameMode)playerGameMode.get(player.getUniqueId()));
      player.teleport((Location)playersInSpectator.get(player.getUniqueId()));
      player.spawnParticle(Particle.END_ROD,(Location)playersInSpectator.get(player.getUniqueId()), 10);
      player.addPotionEffects((Collection)playerPotions.get(player.getUniqueId()));

      playersInSpectator.remove(player.getUniqueId());
      playerPotions.remove(player.getUniqueId());
      playerGameMode.remove(player.getUniqueId());
      playerStands.remove(player.getUniqueId());
      if(ridingEntity.containsKey(player.getUniqueId())){
         Entity vehicle = Bukkit.getEntity(ridingEntity.get(player.getUniqueId()));
         if(vehicle != null) {
            vehicle.addPassenger(player);
            ridingEntity.remove(player.getUniqueId());
         }
      }
      player.sendMessage(PREFIX + ChatColor.GOLD + "Du wurdest in den normalen Modus zurückgesetzt");
   }

   public static void addPlayer(Player player) {
      if(player.getVehicle() != null && !(player.getVehicle() instanceof Player)) {
         ridingEntity.put(player.getUniqueId(), player.getVehicle().getUniqueId());
      }
      playersInSpectator.put(player.getUniqueId(), player.getLocation());
      playerPotions.put(player.getUniqueId(), player.getActivePotionEffects());
      playerGameMode.put(player.getUniqueId(), player.getGameMode());
      player.setGameMode(GameMode.SPECTATOR);
      ArmorStand armorStand = (ArmorStand)player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
      playerStands.put(player.getUniqueId(), armorStand.getUniqueId());
      armorStand.setAI(false);
      armorStand.setInvulnerable(true);
      armorStand.addDisabledSlots(EquipmentSlot.HAND);
      armorStand.setGravity(false);
      armorStand.setCustomName(player.getDisplayName());
      armorStand.setCustomNameVisible(true);
      armorStand.setArms(true);
      armorStand.setInvisible(true);
      ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
      SkullMeta skull = (SkullMeta)item.getItemMeta();
      skull.setDisplayName(player.getName());
      ArrayList<String> lore = new ArrayList();
      lore.add("Custom head");
      skull.setLore(lore);
      skull.setOwningPlayer(player);
      item.setItemMeta(skull);
      armorStand.getEquipment().setHelmet(item);
      armorStand.getEquipment().setBoots(coloredArmor(new ItemStack(Material.LEATHER_BOOTS), player));
      armorStand.getEquipment().setLeggings(coloredArmor(new ItemStack(Material.LEATHER_LEGGINGS), player));
      armorStand.getEquipment().setChestplate(coloredArmor(new ItemStack(Material.LEATHER_CHESTPLATE), player));
      armorStand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.CHEST, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.LEGS, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.FEET, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.HAND, LockType.ADDING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
      Location loc = player.getLocation();
      double eulerX = (double)(loc.getYaw() - armorStand.getLocation().getYaw());
      double eulerZ = (double)loc.getPitch();
      armorStand.setHeadPose(new EulerAngle(Math.toRadians(eulerZ), Math.toRadians(eulerX), 0.0D));
      if(ridingEntity.containsKey(player.getUniqueId())){
         Entity vehicle = Bukkit.getEntity(ridingEntity.get(player.getUniqueId()));
         vehicle.addPassenger(armorStand);
      }
      Iterator var11 = player.getActivePotionEffects().iterator();

      while(var11.hasNext()) {
         PotionEffect effect = (PotionEffect)var11.next();
         player.removePotionEffect(effect.getType());
      }
      player.sendMessage(PREFIX + ChatColor.GREEN + "Du wurdest in den spectator Modus gesetzt");
   }
   @Description("Limited spectator")
   @Default
   public static void onCommand(Player player) {
//      if (!(sender instanceof Player)) {
//         sender.sendMessage(ChatColor.RED + "Dieser Command kann nur von Spielern ausgeführt werden.");
//         return;
//      } else {
//         Player player = (Player)sender;
         if (!playersInSpectator.containsKey(player.getUniqueId())) {
            addPlayer(player);
         } else {
            removePlayer(player);
         }

         return;
//      }
   }

   static {
      PREFIX = ChatColor.WHITE + "SlashC " + ChatColor.GRAY + ">> ";
   }

   private static ItemStack coloredArmor(ItemStack item, Player player){
      ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
      LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
// from RGB:
      java.awt.Color c = PrimarySkinColor.getPrimaryColor(player.getUniqueId());
//      System.out.println(c);
//      System.out.println(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
      leatherArmorMeta.setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
      item.setItemMeta(leatherArmorMeta);
      return item;
   }

}
