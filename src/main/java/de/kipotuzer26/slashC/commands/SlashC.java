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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.EulerAngle;
@CommandAlias("c|camera")
public class SlashC extends BaseCommand {
   public static Map<UUID, Location> playersInSpectator = new HashMap();
   private static Map<UUID, GameMode> playerGameMode = new HashMap();
   private static Map<UUID, Collection<PotionEffect>> playerPotions = new HashMap();
   public static Map<UUID, UUID> playerStands = new HashMap();
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

      while(var1.hasNext()) {
         PotionEffect effect = (PotionEffect)var1.next();
         player.removePotionEffect(effect.getType());
      }

      player.setGameMode((GameMode)playerGameMode.get(player.getUniqueId()));
      player.teleport((Location)playersInSpectator.get(player.getUniqueId()));
      player.addPotionEffects((Collection)playerPotions.get(player.getUniqueId()));
      Bukkit.getEntity((UUID)playerStands.get(player.getUniqueId())).remove();
      playersInSpectator.remove(player.getUniqueId());
      playerPotions.remove(player.getUniqueId());
      playerGameMode.remove(player.getUniqueId());
      player.sendMessage(PREFIX + ChatColor.GOLD + "Du wurdest in den normalen Modus zurückgesetzt");
   }

   public static void addPlayer(Player player) {
      playersInSpectator.put(player.getUniqueId(), player.getLocation());
      playerPotions.put(player.getUniqueId(), player.getActivePotionEffects());
      playerGameMode.put(player.getUniqueId(), player.getGameMode());
      ArmorStand armorStand = (ArmorStand)player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
      armorStand.setAI(false);
      armorStand.setInvulnerable(true);
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
      armorStand.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
      armorStand.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
      armorStand.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
      armorStand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.CHEST, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.LEGS, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.FEET, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
      armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
      Location loc = player.getLocation();
      double eulerX = (double)(loc.getYaw() - armorStand.getLocation().getYaw());
      double eulerZ = (double)loc.getPitch();
      armorStand.setHeadPose(new EulerAngle(Math.toRadians(eulerZ), Math.toRadians(eulerX), 0.0D));
      playerStands.put(player.getUniqueId(), armorStand.getUniqueId());
      Iterator var11 = player.getActivePotionEffects().iterator();

      while(var11.hasNext()) {
         PotionEffect effect = (PotionEffect)var11.next();
         player.removePotionEffect(effect.getType());
      }

      player.setGameMode(GameMode.SPECTATOR);
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
}
