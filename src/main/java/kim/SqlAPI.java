package kim;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlAPI {

    private Connection conn = null;
    private String[] bandrops = new String[]{};
    private String[] banpokemons = new String[]{};

    public void initialize() {
        String url = "jdbc:mysql://%ip%:%port%/%database%?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        File folder = BanPokemon.getInstance().getDataFolder();
        if(!folder.exists()){
            folder.mkdirs();
        }
        File f = new File(BanPokemon.getInstance().getDataFolder(), "mysql.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                cfg.set("address", "localhost");
                cfg.set("port", "3306");
                cfg.set("database", "inkdata");
                cfg.set("account", "root");
                cfg.set("password", "root");
                cfg.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        url = url.replace("%ip%", config.getString("address")).replace("%port%", config.getString("port"))
                .replace("%database%", config.getString("database"));
        String generate_table = "CREATE TABLE IF NOT EXISTS BanPokemonMap(key_name TINYTEXT NOT NULL,data TEXT NOT NULL);";
        try {
            conn = DriverManager.getConnection(url, config.getString("account"), config.getString("password"));
            conn.createStatement().execute(generate_table);
//			read = conn.prepareStatement("SELECT * from ChargeLog where name = ?;");
//			write = conn.prepareStatement("UPDATE USERBLOCK set data = ? where name = ?;");
//            insert = conn.prepareStatement("INSERT INTO ChargeLog(name,trade_no,money,time) values(?,?,?,now());");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshBan(){
        try {
            ResultSet set_bandrops = conn.createStatement().executeQuery("SELECT * from BanPokemonMap where key_name = 'bandrops';");
            if(set_bandrops.next()){
                bandrops = set_bandrops.getString("data").toUpperCase().split(",");
            }
            else{
                conn.createStatement().execute("insert into BanPokemonMap(key_name,data) values ('bandrops','');");
            }

            ResultSet set_banpokemons = conn.createStatement().executeQuery("SELECT * from BanPokemonMap where key_name = 'banpokemons';");
            if(set_banpokemons.next()){
                banpokemons = set_banpokemons.getString("data").toUpperCase().split(",");
            }
            else{
                conn.createStatement().execute("insert into BanPokemonMap(key_name,data) values ('banpokemons','');");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public String combine(String[] arg){
        StringBuilder sb = new StringBuilder();
        for(String s:arg){
            if(s.isEmpty())
                continue;
            sb.append(s);
            sb.append(",");
        }
        if(sb.length()!=0){
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    public void addDrops(String name){
        refreshBan();
        bandrops = Arrays.copyOf(bandrops,bandrops.length+1);
        bandrops[bandrops.length-1] = name;
        try {//UPDATE 表名称 SET 列名称 = 新值 WHERE 列名称 = 某值
            conn.createStatement().execute("update BanPokemonMap set data='"+combine(bandrops)+"' where key_name = 'bandrops'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeDrops(String name){
        refreshBan();
        List<String> s = new ArrayList<String>();
        s.addAll(Arrays.asList(bandrops));
        s.remove(name.toUpperCase());
        bandrops = s.toArray(new String[s.size()]);
        try {
            conn.createStatement().execute("update BanPokemonMap set data='"+combine(bandrops)+"' where key_name = 'bandrops'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPokemons(String name){
        refreshBan();
        banpokemons = Arrays.copyOf(banpokemons,banpokemons.length+1);
        banpokemons[banpokemons.length-1] = name;
        try {
            conn.createStatement().execute("update BanPokemonMap set data='"+combine(banpokemons)+"' where key_name = 'banpokemons'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePokemons(String name){
        refreshBan();
        List<String> s = new ArrayList<String>();
        s.addAll(Arrays.asList(banpokemons));
        s.remove(name.toUpperCase());
        banpokemons = s.toArray(new String[s.size()]);
        try {
            conn.createStatement().execute("update BanPokemonMap set data='"+combine(banpokemons)+"' where key_name = 'banpokemons'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void test(){
        refreshBan();
        System.out.println("掉落物:"+combine(bandrops));
        System.out.println("宝可梦:"+combine(banpokemons));
    }

    public void close(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getBanDrops(){
        return Arrays.asList(bandrops);
    }

    public List<String> getBanPokemons(){
        return Arrays.asList(banpokemons);
    }
}
