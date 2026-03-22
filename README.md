# THCore

**THCore** is a core library plugin for Paper 1.21.1 that serves as the shared foundation for all TH Team plugins. It eliminates repetitive boilerplate code by providing ready-to-use systems for databases, commands, GUIs, messages, cooldowns, and third-party API integrations.

## Features

- **Database System** — MySQL and SQLite support via HikariCP connection pooling, auto-selected from config
- **Command Framework** — `BaseCommand` + `SubCommand` pattern with automatic tab-completion and permission checks
- **GUI System** — `BaseGUI` abstract class with per-instance listeners, auto click-cancel, and close handling
- **Cooldown Manager** — Per-player, per-key cooldowns stored in memory with auto-cleanup
- **Message System** — Full MiniMessage support (`<red>`, `<gradient:...>`), legacy `&c` codes, and hex `&#RRGGBB`
- **Hook System** — Soft-depend integrations that activate only if the target plugin is installed

## Supported Integrations (all soft-depend)

| Plugin | Functionality |
|---|---|
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | Economy, Permissions, Chat |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Parse & register placeholders (`%thcore_version%`, `%thcore_hooks%`) |
| [WorldGuard](https://dev.bukkit.org/projects/worldguard) | Region checks at location/player |
| [Nexo](https://nexomc.com/) | Custom item creation and ID lookup |
| [Oraxen](https://www.spigotmc.org/resources/oraxen.72448/) | Custom item creation and ID lookup |
| [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/) | Custom item access, namespaced IDs |
| [MythicMobs](https://www.spigotmc.org/resources/mythicmobs.5702/) | Mob spawning and type detection |
| [CoinsEngine](https://www.spigotmc.org/resources/coinsengine.84121/) | Multi-currency balance management |
| [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/) | Points balance management |

## Requirements

- **Paper** 1.21.1 or higher
- **Java** 21 or higher

## Installation

1. Download `THCore.jar` from [Releases](../../releases)
2. Place it in your server's `plugins/` folder
3. Restart the server

THCore will load automatically. All integrations are optional — it works on a vanilla Paper server with none of the above plugins installed.

## Configuration

```yaml
# plugins/THCore/config.yml

database:
  type: sqlite   # sqlite or mysql

  sqlite:
    file: thcore.db

  mysql:
    host: localhost
    port: 3306
    database: thcore
    username: root
    password: ''

messages:
  prefix: '<gray>[<aqua>THCore</aqua>]</gray> '
```

## Commands

| Command | Permission | Description |
|---|---|---|
| `/thcore reload` | `thcore.admin` | Reloads configuration |
| `/thcore info` | `thcore.admin` | Shows version, DB type, active hooks |
| `/thcore hooks` | `thcore.admin` | Lists all hooks and their status |

## Usage in Your Plugin

### 1. Add THCore as a dependency

```kotlin
// build.gradle.kts
dependencies {
    compileOnly(files("libs/THCore.jar"))
}
```

```yaml
# plugin.yml
depend: [THCore]
```

### 2. Use the API

```java
// Economy (Vault)
VaultHook vault = THCoreAPI.getVault();
if (vault != null) {
    vault.deposit(player, 500);
    double balance = vault.getBalance(player);
}

// Cooldowns
CooldownManager cd = THCoreAPI.getCooldownManager();
cd.setCooldown(player.getUniqueId(), "my_skill", 10_000L);

if (cd.hasCooldown(player.getUniqueId(), "my_skill")) {
    long remaining = cd.getRemainingSeconds(player.getUniqueId(), "my_skill");
    player.sendMessage("Wait " + remaining + "s");
}

// Database
THCoreAPI.getDatabase().executeUpdate(
    "INSERT INTO my_table (uuid, value) VALUES (?, ?)",
    player.getUniqueId().toString(), 100
);

// Messages
THCoreAPI.getMessageManager().send(player, "<green>Done!</green>");

// WorldGuard region check
WorldGuardHook wg = THCoreAPI.getWorldGuard();
if (wg != null && wg.isInRegion(player, "spawn")) {
    // player is in the "spawn" region
}

// Custom items (Nexo)
NexoHook nexo = THCoreAPI.getNexo();
if (nexo != null) {
    ItemStack item = nexo.getItem("my_custom_item");
}
```

### 3. Create a command

```java
public class MyCommand extends BaseCommand {

    public MyCommand(THCore plugin) {
        super(plugin);

        registerSubCommand(new SubCommand() {
            public String getName() { return "give"; }
            public String getPermission() { return "myplugin.give"; }
            public String getDescription() { return "Give an item"; }

            public void execute(CommandSender sender, String[] args) {
                // logic here
            }
        });
    }
}

// In your plugin's onEnable():
new MyCommand(plugin).register(this, "mycommand");
```

### 4. Create a GUI

```java
public class MyShopGUI extends BaseGUI {

    public MyShopGUI(THCore plugin) {
        super(plugin, "<gold><bold>My Shop</bold></gold>", 3); // 3 rows
    }

    @Override
    protected void fillItems() {
        setItem(13, new ItemStack(Material.DIAMOND));
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        if (event.getSlot() == 13) {
            event.getWhoClicked().sendMessage("Clicked!");
        }
    }
}

// Open it:
new MyShopGUI(plugin).open(player);
```

## Building from Source

```bash
git clone https://github.com/JuanCorso-dev/THCore.git
cd THCore
./gradlew shadowJar
# Output: build/libs/THCore-1.0.0.jar
```

## License

This project is private and intended for use by TH Team plugins only.

---

**Author:** [JuanCorso-dev](https://github.com/JuanCorso-dev)
