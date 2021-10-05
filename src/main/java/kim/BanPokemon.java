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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

public class BanPokemon extends JavaPlugin implements Listener {

    private static BanPokemon instance= null;
    private SqlAPI sqlapi = new SqlAPI();

    @Override
    public void onEnable() {
        instance = this;
        sqlapi.initialize();
        Bukkit.getScheduler().runTaskLater(this,()->sqlapi.refreshBan(),20*5);
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("banpokemon").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String aliase, String[] args) {
                if(args.length==0){
                    sqlapi.test();
                }
                if(args.length==2){
                    if(args[0].equals("add")){
                        sqlapi.addPokemons(args[1]);
                        commandSender.sendMessage("封禁了宝可梦 "+args[1]);
                    }
                    if(args[0].equals("remove")){
                        sqlapi.removePokemons(args[1]);
                        commandSender.sendMessage("解封了宝可梦 "+args[1]);
                    }
                }
                return true;
            }
        });
        getCommand("bandrop").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String aliase, String[] args) {
                if(args.length==0){
                    sqlapi.test();
                }
                if(args.length==2){
                    if(args[0].equals("add")){
                        sqlapi.addDrops(args[1]);
                        commandSender.sendMessage("封禁了宝可梦掉落物 "+args[1]);
                    }
                    if(args[0].equals("remove")){
                        sqlapi.removeDrops(args[1]);
                        commandSender.sendMessage("解封了宝可梦掉落物 "+args[1]);
                    }
                }
                return true;
            }
        });
    }


    @Override
    public void onDisable() {
        sqlapi.close();
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
                String materialName = material.name().toUpperCase();
                if(sqlapi.getBanDrops().contains(materialName)){
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
                if (sqlapi.getBanPokemons().contains(p.getLocalizedName())){
                    pps.set(p.getPosition(),null);
                }
            }
        }
    }
    public void onPlayerPCCheck(Player player){
        PCStorage pcStorage = Pixelmon.storageManager.getPCForPlayer(player.getUniqueId());
        for (Pokemon p:pcStorage.getAll()) {
            if (p!=null){
                if (sqlapi.getBanPokemons().contains(p.getLocalizedName())){
                    pcStorage.set(p.getPosition(),null);
                }
            }
        }
    }

    public static BanPokemon getInstance(){
        return instance;
    }
}
