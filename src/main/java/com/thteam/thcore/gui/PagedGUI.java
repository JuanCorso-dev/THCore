package com.thteam.thcore.gui;

import com.thteam.thcore.THCore;
import com.thteam.thcore.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for paginated chest-only GUIs.
 * Extends BaseGUI — all BaseGUI features (requirements, actions, dynamic titles,
 * auto-refresh, open/close hooks) are available here too.
 *
 * Content items are added via {@link #addPageItem} and automatically laid out
 * across pages. The bottom row is reserved for navigation by default.
 *
 * Usage:
 *
 *   public class PlayerListGUI extends PagedGUI {
 *       public PlayerListGUI(THCore plugin) {
 *           super(plugin, "<gold>Online Players</gold>", 6);
 *           setPrevButtonItem(new ItemBuilder(Material.ARROW).name("<gray>← Previous").build());
 *           setNextButtonItem(new ItemBuilder(Material.ARROW).name("<gray>Next →").build());
 *       }
 *
 *       @Override
 *       protected void fillBorder() {
 *           fillEmpty(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
 *       }
 *   }
 *
 *   PlayerListGUI gui = new PlayerListGUI(plugin);
 *   for (Player p : Bukkit.getOnlinePlayers()) {
 *       gui.addPageItem(
 *           new ItemBuilder(Material.PLAYER_HEAD).name("<white>" + p.getName()).build(),
 *           GUIAction.console("msg %player% " + p.getName())
 *       );
 *   }
 *   gui.open(viewer);
 */
public abstract class PagedGUI extends BaseGUI {

    // ---------------------------------------------------------------- Page items

    private record PageItem(ItemStack item, List<GUIAction> actions) {}

    private final List<PageItem> pageItems = new ArrayList<>();
    private int currentPage = 0;

    // ---------------------------------------------------------------- Layout configuration

    // Slots where content items are placed (defaults to all slots except bottom row)
    private int[] contentSlots;

    // Navigation slots (defaults based on rows)
    private int prevSlot;
    private int nextSlot;
    private int pageInfoSlot;

    // Navigation button items
    private ItemStack prevButtonItem;
    private ItemStack nextButtonItem;
    private ItemStack noPageItem; // shown in nav slot when no prev/next page available

    private final int rows;

    // ---------------------------------------------------------------- Constructor

    /**
     * @param plugin  THCore instance
     * @param title   Inventory title (MiniMessage + PAPI)
     * @param rows    Chest rows (1–6). Bottom row is reserved for navigation.
     */
    protected PagedGUI(THCore plugin, String title, int rows) {
        super(plugin, title, rows);
        this.rows = Math.max(1, Math.min(6, rows));

        // Default layout: all slots except bottom row are content
        int navRowStart = (this.rows - 1) * 9;
        contentSlots = new int[navRowStart];
        for (int i = 0; i < navRowStart; i++) contentSlots[i] = i;

        prevSlot     = navRowStart;          // bottom-left
        nextSlot     = navRowStart + 8;      // bottom-right
        pageInfoSlot = navRowStart + 4;      // bottom-center

        // Default button items
        prevButtonItem = new ItemBuilder(Material.ARROW)
            .name("<gray>← Previous page")
            .build();
        nextButtonItem = new ItemBuilder(Material.ARROW)
            .name("<gray>Next page →")
            .build();
        noPageItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .build();
    }

    // ---------------------------------------------------------------- Configuration

    /**
     * Overrides the default content slots.
     * These are the slots where page items will be placed.
     */
    protected void setContentSlots(int... slots) {
        this.contentSlots = Arrays.copyOf(slots, slots.length);
    }

    /** Sets the slot used for the "previous page" button. */
    protected void setPrevSlot(int slot) { this.prevSlot = slot; }

    /** Sets the slot used for the "next page" button. */
    protected void setNextSlot(int slot) { this.nextSlot = slot; }

    /** Sets the slot used for the page indicator ("Page X / Y"). */
    protected void setPageInfoSlot(int slot) { this.pageInfoSlot = slot; }

    /** Sets the item shown for the "previous page" button when a previous page exists. */
    protected void setPrevButtonItem(ItemStack item) { this.prevButtonItem = item; }

    /** Sets the item shown for the "next page" button when a next page exists. */
    protected void setNextButtonItem(ItemStack item) { this.nextButtonItem = item; }

    /** Sets the item shown in nav slots when no page is available (decorative filler). */
    protected void setNoPageItem(ItemStack item) { this.noPageItem = item; }

    // ---------------------------------------------------------------- Content

    /**
     * Adds a content item to the paginated list.
     * Items are placed in content slots across pages in the order they are added.
     *
     * @param item    The item to display
     * @param actions Actions to execute when the item is clicked (optional)
     */
    public void addPageItem(ItemStack item, GUIAction... actions) {
        pageItems.add(new PageItem(item, List.of(actions)));
    }

    /** Returns the total number of pages. */
    public int getPageCount() {
        if (contentSlots.length == 0) return 1;
        return Math.max(1, (int) Math.ceil((double) pageItems.size() / contentSlots.length));
    }

    /** Returns the current page index (0-based). */
    public int getCurrentPage() { return currentPage; }

    /** Navigates to a specific page and refreshes the GUI. */
    public void setPage(int page) {
        currentPage = Math.max(0, Math.min(page, getPageCount() - 1));
        refresh();
    }

    /** Navigates to the next page if one exists. */
    public void nextPage() {
        if (currentPage < getPageCount() - 1) setPage(currentPage + 1);
    }

    /** Navigates to the previous page if one exists. */
    public void prevPage() {
        if (currentPage > 0) setPage(currentPage - 1);
    }

    // ---------------------------------------------------------------- BaseGUI integration

    /**
     * Called by PagedGUI's fillItems() for subclasses to add border decoration.
     * Do NOT place items in content slots here — they are managed by PagedGUI.
     */
    protected abstract void fillBorder();

    /**
     * Final override: calls fillBorder() then renders page items and nav buttons.
     * Subclasses must override {@link #fillBorder()} instead of this method.
     */
    @Override
    protected final void fillItems() {
        fillBorder();
        renderPageItems();
        renderNavButtons();
    }

    // ---------------------------------------------------------------- Rendering (private)

    private void renderPageItems() {
        int startIndex = currentPage * contentSlots.length;

        for (int i = 0; i < contentSlots.length; i++) {
            int itemIndex = startIndex + i;
            int slot = contentSlots[i];

            if (itemIndex < pageItems.size()) {
                PageItem pageItem = pageItems.get(itemIndex);
                final int capturedIndex = itemIndex;

                // Register a button for this slot with the item's actions
                addButton(new GUIButton(slot, pageItem.item())
                    .onClick(player -> {
                        for (GUIAction action : pageItems.get(capturedIndex).actions()) {
                            action.execute(player);
                        }
                    })
                );
            } else {
                // Empty slot — clear whatever was there
                setItem(slot, null);
            }
        }
    }

    private void renderNavButtons() {
        int totalPages = getPageCount();

        // Previous button
        if (currentPage > 0) {
            addButton(new GUIButton(prevSlot, prevButtonItem)
                .onClick(player -> prevPage())
            );
        } else {
            setItem(prevSlot, noPageItem);
        }

        // Next button
        if (currentPage < totalPages - 1) {
            addButton(new GUIButton(nextSlot, nextButtonItem)
                .onClick(player -> nextPage())
            );
        } else {
            setItem(nextSlot, noPageItem);
        }

        // Page info
        if (pageInfoSlot >= 0) {
            ItemStack infoItem = new ItemBuilder(Material.PAPER)
                .name("<gray>Page <white>" + (currentPage + 1) + "<gray> / <white>" + totalPages)
                .build();
            setItem(pageInfoSlot, infoItem);
        }
    }
}
