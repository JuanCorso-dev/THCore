# THCore

**THCore** is a core library plugin for Paper 1.21.1 that serves as the shared foundation for all TH Team plugins. It eliminates repetitive boilerplate code by providing ready-to-use systems for databases, commands, GUIs, messages, cooldowns, and third-party API integrations.

## Features

- **Database System** — MySQL and SQLite support via HikariCP connection pooling, auto-selected from config
- **Command Framework** — `BaseCommand` + `SubCommand` pattern with automatic tab-completion and permission checks
- **GUI System** — `BaseGUI` (chest), `FullGUI` (chest + player inventory), and `PagedGUI` (paginated) with priority slot resolution, `GUIAction`, `GUIRequirement`, dynamic PAPI titles, open/close hooks, and auto-refresh
- **ItemBuilder** — Fluent builder for `ItemStack` (name, lore, enchants, glow, CustomModelData)
- **PlayerData** — Per-player key-value store persisted in the database (SQLite/MySQL)
- **Cooldown Manager** — Per-player, per-key cooldowns stored in memory with auto-cleanup
- **Message System** — Full MiniMessage support (`<red>`, `<gradient:...>`), legacy `&c` codes, and hex `&#RRGGBB`
- **Tasks** — Static helpers for sync/async Bukkit scheduler operations
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
| [LuckPerms](https://luckperms.net/) | Groups, prefix/suffix, meta values per player |

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

// LuckPerms — groups, prefix, suffix, meta
LuckPermsHook lp = THCoreAPI.getLuckPerms();
if (lp != null) {
    String group  = lp.getPrimaryGroup(player);        // "admin", "vip", "default"...
    boolean isVip = lp.isInGroup(player, "vip");
    String prefix = lp.getPrefix(player);              // "[VIP] " or null
    String suffix = lp.getSuffix(player);
    String level  = lp.getMetaValue(player, "level");  // custom meta key
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

### 4. Create a GUI (BaseGUI — chest only)

`BaseGUI` is the foundation for chest-based menus. It supports `GUIButton` with priority-based slot resolution, `GUIRequirement` conditions, `GUIAction` handlers, dynamic PlaceholderAPI titles, and auto-refresh.

```java
public class MyShopGUI extends BaseGUI {

    public MyShopGUI(THCore plugin) {
        super(plugin, "<gold>Shop — Balance: %vault_eco_balance_fixed%$</gold>", 3);

        // Refresh title + items every second
        setUpdateInterval(20);

        // Actions on open / close
        addOpenAction(
            GUIAction.sound(Sound.BLOCK_CHEST_OPEN, 1f, 1f),
            GUIAction.message("<gray>Welcome to the shop!")
        );
        addCloseAction(
            GUIAction.sound(Sound.BLOCK_CHEST_CLOSE, 1f, 1f)
        );

        // VIP button — shown only to players with "shop.vip" (priority 10)
        addButton(new GUIButton(13, vipDiamondItem)
            .priority(10)
            .require(GUIRequirement.permission("shop.vip"))
            .onClick(
                GUIAction.takeMoney(500),
                GUIAction.console("give %player% diamond 5"),
                GUIAction.message("<green>You bought 5 diamonds!")
            )
        );

        // Normal button — fallback for non-VIPs (priority 1)
        // Only executes if the player has $100; otherwise runs deny action
        addButton(new GUIButton(13, normalItem)
            .priority(1)
            .requireClick(GUIRequirement.money(100))
            .onDeny(GUIAction.message("<red>You need $100!"))
            .onClick(
                GUIAction.takeMoney(100),
                GUIAction.console("give %player% gold_ingot 1")
            )
        );

        // Close button
        addButton(new GUIButton(26, barrierItem)
            .onClick(GUIAction.close())
        );
    }

    @Override
    protected void fillItems() {
        fillEmpty(grayGlassPane); // fill empty slots with decoration
    }
}

// Open it:
new MyShopGUI(plugin).open(player);
```

#### GUIRequirement — conditions

```java
GUIRequirement.permission("shop.vip")          // player has permission
GUIRequirement.noPermission("shop.banned")     // player lacks permission
GUIRequirement.money(500)                      // player has ≥ $500 (Vault)
GUIRequirement.playerPoints(100)               // player has ≥ 100 points (PlayerPoints)
GUIRequirement.placeholder("%player_level%", ">=", "10") // PAPI comparison

// Combine with logical operators
GUIRequirement.permission("vip").or(GUIRequirement.money(1000))
GUIRequirement.permission("banned").negate()
GUIRequirement.money(100).and(GUIRequirement.permission("shop.access"))

// Custom lambda
GUIRequirement custom = player -> player.getLevel() >= 10;
```

#### GUIAction — actions

```java
GUIAction.message("<red>Text")                          // send message to player
GUIAction.broadcast("<gold>Announcement")               // broadcast to all
GUIAction.console("give %player% diamond 1")            // run as console (%player% = name)
GUIAction.playerCommand("/warp spawn")                  // run as player
GUIAction.close()                                       // close the GUI
GUIAction.sound(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)   // play sound
GUIAction.title("<gold>Title", "<gray>Subtitle", 10, 40, 10)
GUIAction.giveMoney(500)                                // give money (Vault)
GUIAction.takeMoney(100)                                // take money (Vault)
GUIAction.openMenu(() -> new OtherMenu(plugin))         // open another BaseGUI
GUIAction.openFullMenu(() -> new OtherFullMenu(plugin)) // open a FullGUI
GUIAction.chain(action1, action2, action3)              // run multiple in order

// Custom lambda
GUIAction custom = player -> player.setFlying(true);
```

### 5. Create a FullGUI (chest + player inventory)

`FullGUI` uses a **unified slot system (0–89)** that covers both the chest and the player's inventory. The player's inventory is automatically backed up on open and restored on close (including on disconnect and death).

```
Chest:       0 –  8  (row 0)    54 – 62  (player main row 1)
             9 – 17  (row 1)    63 – 71  (player main row 2)
            18 – 26  (row 2)    72 – 80  (player main row 3)
            27 – 35  (row 3)    81 – 89  (hotbar)
            36 – 44  (row 4)
            45 – 53  (row 5)
```

```java
public class MyFullMenu extends FullGUI {

    public MyFullMenu(THCore plugin) {
        super(plugin, "<gold>Full Menu — %vault_eco_balance_fixed%$</gold>", 6);

        setUpdateInterval(20);

        addOpenAction(GUIAction.sound(Sound.BLOCK_CHEST_OPEN, 1f, 1f));
        addCloseAction(GUIAction.sound(Sound.BLOCK_CHEST_CLOSE, 1f, 1f));

        // VIP button at chest slot 4
        addButton(new GUIButton(4, vipItem)
            .priority(10)
            .require(GUIRequirement.permission("menu.vip"))
            .onClick(GUIAction.message("<green>VIP action!"))
        );

        // Close button at hotbar slot 81
        addButton(new GUIButton(81, barrierItem)
            .onClick(GUIAction.close())
        );
    }

    @Override
    protected void fillItems() {
        fillEmpty(grayGlassPane);
    }
}

// Open it:
new MyFullMenu(plugin).open(player);
```

### 6. ItemBuilder

Fluent builder for `ItemStack`. Available anywhere — no THCore instance needed.

```java
import com.thteam.thcore.item.ItemBuilder;

ItemStack item = new ItemBuilder(Material.DIAMOND)
    .name("<aqua><bold>VIP Diamond")
    .lore("<gray>Click to buy", "<yellow>Cost: $500")
    .amount(3)
    .enchant(Enchantment.SHARPNESS, 5)
    .flag(ItemFlag.HIDE_ENCHANTS)
    .glow(true)          // visual glow without showing enchantment tooltip
    .model(1001)         // CustomModelData for resource packs
    .unbreakable(true)
    .build();

// Copy constructor (leaves original unchanged)
ItemStack copy = new ItemBuilder(existingItem).name("<red>Modified").build();
```

### 7. PagedGUI

Paginated chest GUI built on top of `BaseGUI`. All `BaseGUI` features work here too.

```java
public class PlayerListGUI extends PagedGUI {

    public PlayerListGUI(THCore plugin) {
        super(plugin, "<gold>Online Players — Page %page%</gold>", 6);

        setPrevButtonItem(new ItemBuilder(Material.ARROW).name("<gray>← Previous").build());
        setNextButtonItem(new ItemBuilder(Material.ARROW).name("<gray>Next →").build());

        addOpenAction(GUIAction.sound(Sound.BLOCK_CHEST_OPEN, 1f, 1f));
    }

    @Override
    protected void fillBorder() {
        // Fill non-content slots with decoration (bottom row is nav, rest is content)
        fillEmpty(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
    }
}

// Add items dynamically before opening:
PlayerListGUI gui = new PlayerListGUI(plugin);
for (Player p : Bukkit.getOnlinePlayers()) {
    gui.addPageItem(
        new ItemBuilder(Material.PLAYER_HEAD).name("<white>" + p.getName()).build(),
        GUIAction.message("<gray>You selected: <white>" + p.getName())
    );
}
gui.open(viewer);
```

Default layout for a 6-row chest:
- **Content slots**: 0–44 (rows 1–5)
- **Prev button**: slot 45 (bottom-left)
- **Page info**: slot 49 (bottom-center) — shows "Page X / Y" automatically
- **Next button**: slot 53 (bottom-right)

Customize with:
```java
setContentSlots(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25); // inner area only
setPrevSlot(45);
setNextSlot(53);
setPageInfoSlot(49);
```

### 8. PlayerData

Per-player key-value store backed by the database. Table is auto-created on startup.

```java
// Load (blocking — run async for best performance)
PlayerData data = THCoreAPI.getPlayerDataManager().load(player.getUniqueId());

// Read values
int kills  = data.getInt("kills", 0);
String rank = data.getString("rank", "default");
boolean vip = data.getBoolean("vip", false);

// Write values
data.set("kills", kills + 1);
data.set("rank", "VIP");
data.remove("temp_key");

// Check existence
if (data.has("rank")) { ... }

// Save (prefer async to avoid blocking the main thread)
data.saveAsync(plugin);  // non-blocking
data.save();             // blocking (use on async thread)

// Delete all data for a player
THCoreAPI.getPlayerDataManager().delete(player.getUniqueId());
```

Works with both SQLite and MySQL — uses `INSERT OR REPLACE` / `ON DUPLICATE KEY UPDATE` automatically.

### 9. Tasks

Static helpers for the Bukkit scheduler.

```java
import com.thteam.thcore.util.Tasks;

// Sync — runs on the main thread
Tasks.run(plugin, () -> player.sendMessage("immediate"));
Tasks.delay(plugin, 20, () -> player.sendMessage("after 1s"));
Tasks.repeat(plugin, 0, 20, () -> broadcastTime());

// Async — runs off the main thread (safe for I/O, DB queries)
Tasks.async(plugin, () -> loadFromDatabase());
Tasks.asyncDelay(plugin, 40, () -> fetchWebData());
Tasks.asyncRepeat(plugin, 0, 200, () -> syncWithExternalAPI());

// Cancel a task
BukkitTask task = Tasks.repeat(plugin, 0, 20, runnable);
Tasks.cancel(task); // null-safe
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
