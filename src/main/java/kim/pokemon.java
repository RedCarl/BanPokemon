package kim;

import catserver.api.bukkit.event.ForgeEvent;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.DropEvent;
import com.pixelmonmod.pixelmon.api.events.PixelmonSendOutEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.storage.ChangeStorageEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class pokemon extends JavaPlugin implements Listener {
    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {

        Bukkit.getPluginManager().registerEvents(this, this);
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void onPixelmonSendOut(ForgeEvent e) {

        if (e.getForgeEvent() instanceof ChangeStorageEvent) {
            ChangeStorageEvent event = (ChangeStorageEvent)e.getForgeEvent();
            Player player = Bukkit.getPlayer(event.pokemon.getOwnerPlayerUUID());
            Bukkit.getScheduler().runTaskLater(this,()->{
                onPlayerPartyCheck(player);
            },1);
        }
    }

    @EventHandler
    public void onBattleGetSpoil(ForgeEvent e){   //处理掉落物
        if(e.getForgeEvent() instanceof DropEvent){
            DropEvent event = (DropEvent) e.getForgeEvent();
            event.getDrops().stream().collect(Collectors.toList()).forEach(item->{
                ItemStack is = CraftItemStack.asBukkitCopy(item.itemStack);
                Material material = is.getType();
                String materialName = material.name();
                if(materialName.equals(Material.GLOWSTONE_DUST.name())){ //萤石粉
                    event.removeDrop(item);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        onPlayerPartyCheck(event.getPlayer());
        onPlayerPCCheck(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        onPlayerPartyCheck(event.getPlayer());
        onPlayerPCCheck(event.getPlayer());
    }

    public void onPlayerPartyCheck(Player player){
        PlayerPartyStorage pps = Pixelmon.storageManager.getParty(player.getUniqueId());
        for (Pokemon p:pps.getTeam().stream().collect(Collectors.toList())) {
            if (p!=null){
                if (p.getSpecies().getLocalizedName().equals("百变怪")){
                    pps.set(p.getPosition(),null);
                }
            }
        }
    }
    public void onPlayerPCCheck(Player player){
        PCStorage pcStorage = Pixelmon.storageManager.getPCForPlayer(player.getUniqueId());
        for (Pokemon p:pcStorage.getAll()) {
            if (p!=null){
                if (p.getLocalizedName().equals("百变怪")){
                    pcStorage.set(p.getPosition(),null);
                }
            }
        }
    }
}
