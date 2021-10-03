package kim;

import catserver.api.bukkit.event.ForgeEvent;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.PixelmonSendOutEvent;
import com.pixelmonmod.pixelmon.api.events.storage.ChangeStorageEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

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
            PlayerPartyStorage pps = Pixelmon.storageManager.getParty(player.getUniqueId());
            //box代表容器 -1 指玩家背包 0 - 256 指PC
            System.out.println(event.pokemon.getLocalizedName()+" "+event.newPosition.box+" "+event.newPosition.order);
            onPlayerPartyCheck(player);

//            if (event.pokemon.getLocalizedName().equals("百变怪")){
//                StoragePosition storagePosition = new StoragePosition(event.newPosition.box,event.newPosition.order);
//                if (event.newPosition.box>=0){
//                    PCStorage pcStorage = new PCStorage(pps.uuid);
//                    pcStorage.set(storagePosition,null);
//                }else {
//                    pps.set(storagePosition,null);
//                }
//            }
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
        for (Pokemon p:pps.getAll()) {
            if (p!=null){
                if (p.getLocalizedName().equals("百变怪")){
                    pps.set(p.getPosition(),null);
                }
            }
        }
    }
    public void onPlayerPCCheck(Player player){
//        PCStorage pcStorage = new PCStorage(player.getUniqueId());
//        for (Pokemon p:pcStorage.getAll()) {
//            if (p!=null){
//                if (p.getLocalizedName().equals("百变怪")){
//                    System.out.println(p.getLocalizedName());
//                    pcStorage.set(p.getPosition(),null);
//                }
//            }
//        }
    }
}
