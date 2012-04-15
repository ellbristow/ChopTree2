package me.Iso.ChopTree;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.server.Packet103SetSlot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ChopTreeBlockListener implements Listener {	
	
    public static Player pubplayer = null;
    public static ChopTree plugin;
	
    public ChopTreeBlockListener(ChopTree instance) {
        plugin = instance;
    }
	
    @EventHandler (priority = EventPriority.NORMAL)
    public void onBlockBreak (BlockBreakEvent event) {

        if (event.isCancelled()) return;
		
        Block block = event.getBlock();
        if (block.getType() == Material.LOG) {
            if (denyPermission(event.getPlayer())) return;
            if (denyActive(event.getPlayer())) return;
            if (denyItem(event.getPlayer())) return;

            event.setCancelled(true);

            if (Chop (event.getBlock(), event.getPlayer(), event.getBlock().getWorld())) {
                if (!plugin.moreDamageToTools) {
                    if (breaksTool(event.getPlayer(), event.getPlayer().getItemInHand())) {
                        event.getPlayer().getInventory().clear(event.getPlayer().getInventory().getHeldItemSlot());
                    }
                }	
            } else {
                event.setCancelled(false);
            }
        }		
    
    }
	
    public boolean Chop (Block block, Player player, World world) {
        List <Block> blocks = new LinkedList <Block>();
        Block highest = getHighestLog(block);
        if (isTree(highest, player, block)) {
            getBlocksToChop(block, highest, blocks);
            //Chop the blocks
            if (plugin.logsMoveDown) {
                moveDownLogs(block, blocks, world, player);
            } else {
                popLogs(block, blocks, world, player);
            }
        } else {
            return false;
        }
        //player.updateInventory();
        updateInventory(player); //bergkillers update inventory method
        return true;
    }
	
    /*
    * From bergerkiller's post on bukkit forum
    * http://forums.bukkit.org/threads/updating-the-inventory-using-craftbukkit-with-bukkit.27455/
    */
	
    @Deprecated
    public static void updateInventory(Player p) {
        CraftPlayer c = (CraftPlayer) p;
        for (int i = 0;i < 36;i++) 
        {
            int nativeindex = i;
            if (i < 9) 
            	nativeindex = i + 36;
            ItemStack olditem =  c.getInventory().getItem(i);
            net.minecraft.server.ItemStack item = null;
            if (olditem != null && olditem.getType() != Material.AIR) 
            {
                item = new net.minecraft.server.ItemStack(0, 0, 0);
                item.id = olditem.getTypeId();
                item.count = olditem.getAmount();
            }
            Packet103SetSlot pack = new Packet103SetSlot(0, nativeindex, item);
            c.getHandle().netServerHandler.sendPacket(pack);
        }
    }
    //************************************************************************************************
	
    public void getBlocksToChop (Block block, Block highest, List <Block> blocks) {
        while (block.getY() <= highest.getY()) {
            if (!blocks.contains(block)) {
                blocks.add(block);
            }
            getBranches(block, blocks, block.getRelative(BlockFace.NORTH));
            getBranches(block, blocks, block.getRelative(BlockFace.NORTH_EAST));
            getBranches(block, blocks, block.getRelative(BlockFace.EAST));
            getBranches(block, blocks, block.getRelative(BlockFace.SOUTH_EAST));
            getBranches(block, blocks, block.getRelative(BlockFace.SOUTH));
            getBranches(block, blocks, block.getRelative(BlockFace.SOUTH_WEST));
            getBranches(block, blocks, block.getRelative(BlockFace.WEST));
            getBranches(block, blocks, block.getRelative(BlockFace.NORTH_WEST));
            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH))) {
                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH));
            }
            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST))) {
                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST));
            }
            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST))) {
                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST));
            }
            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST))) {
                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST));
            }
            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH))) {
                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH));
            }
            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST))) {
                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST));
            }
            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST))) {
                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST));
            }
            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST))) {
                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST));
            }
            if (!blocks.contains(block.getRelative(BlockFace.UP)) && block.getRelative(BlockFace.UP).getType() == Material.LOG) {
                block = block.getRelative(BlockFace.UP);
            } else break;
        }
    }
	
    public void getBranches(Block block, List<Block> blocks, Block other) {
        if (!blocks.contains(other) && other.getType() == Material.LOG) {
            getBlocksToChop(other, getHighestLog(other), blocks);
        }
    }
	
    public Block getHighestLog(Block block) {
        while (block.getRelative(BlockFace.UP).getType() == Material.LOG) {
            block = block.getRelative(BlockFace.UP);
        }
        return block;
    }

    public boolean isTree (Block block, Player player, Block first) {

        if (!plugin.onlyTrees) return true;

        if (plugin.trees.containsKey(player)) {
            Block[] blockarray = plugin.trees.get(player);
            for (int counter = 0; counter < Array.getLength(blockarray); counter++) {
                if (blockarray[counter] == block) return true;
                if (blockarray[counter] == first) return true;
            }
        }
        int counter = 0;
        if (block.getRelative(BlockFace.UP).getType() == Material.LEAVES) counter++;
        if (block.getRelative(BlockFace.DOWN).getType() == Material.LEAVES) counter++;
        if (block.getRelative(BlockFace.NORTH).getType() == Material.LEAVES) counter++;
        if (block.getRelative(BlockFace.EAST).getType() == Material.LEAVES) counter++;
        if (block.getRelative(BlockFace.SOUTH).getType() == Material.LEAVES) counter++;
        if (block.getRelative(BlockFace.WEST).getType() == Material.LEAVES) counter++;
        if (counter >= 2) return true;
        if (block.getData() == (byte) 1) {
            block = block.getRelative(BlockFace.UP);
            if (block.getRelative(BlockFace.UP).getType() == Material.LEAVES) counter++;
            if (block.getRelative(BlockFace.NORTH).getType() == Material.LEAVES) counter++;
            if (block.getRelative(BlockFace.EAST).getType() == Material.LEAVES) counter++;
            if (block.getRelative(BlockFace.SOUTH).getType() == Material.LEAVES) counter++;
            if (block.getRelative(BlockFace.WEST).getType() == Material.LEAVES) counter++;
            if (counter >= 2) return true;
        }
        return false;
    }

    public void popLogs (Block block, List<Block> blocks, World world, Player player) {
        ItemStack item = new ItemStack (1, 1, (short) 0, null);
        item.setAmount(1);
        for (int counter = 0; counter < blocks.size(); counter++) {

            block = blocks.get(counter);
            item.setType(block.getType());
            item.setDurability(block.getData());
            block.setType(Material.AIR);
            world.dropItem(block.getLocation(), item);

            if (plugin.moreDamageToTools) {
                if (breaksTool(player, player.getItemInHand())) {
                    player.getInventory().clear(player.getInventory().getHeldItemSlot());
                    if (plugin.interruptIfToolBreaks) break;
                }
            }			
        }
    }

    public void moveDownLogs (Block block, List<Block> blocks, World world, Player player) {
        ItemStack item = new ItemStack (1, 1, (short) 0, null);
        item.setAmount(1);

        Block down;
        List <Block> downs = new LinkedList <Block> ();
        for (int counter = 0; counter < blocks.size(); counter++) {
            block = blocks.get(counter);
            down = block.getRelative(BlockFace.DOWN);
            if (down.getType() == Material.AIR || down.getType() == Material.LEAVES) {				
                down.setType(Material.LOG);
                down.setData(block.getData());
                block.setType(Material.AIR);
                downs.add(down);
            } else {
                item.setType(block.getType());
                item.setDurability(block.getData());
                block.setType(Material.AIR);
                world.dropItem(block.getLocation(), item);

                if (plugin.moreDamageToTools) {
                    if (breaksTool(player, player.getItemInHand())) {
                        player.getInventory().clear(player.getInventory().getHeldItemSlot());
                    }
                }
            }
        }

        for (int counter = 0; counter < downs.size(); counter++) {
            block = downs.get(counter);
            if (isLoneLog(block)) {
                item.setType(block.getType());
                item.setDurability(block.getData());
                block.setType(Material.AIR);
                world.dropItem(block.getLocation(), item);
                downs.remove(block);

                if (plugin.moreDamageToTools) {
                    if (breaksTool(player, player.getItemInHand())) {
                        player.getInventory().clear(player.getInventory().getHeldItemSlot());
                    }
                }
            }
        }

        if (plugin.trees.containsKey(player)) {
            plugin.trees.remove(player);
        }
        if (downs.isEmpty()) return;
        Block[] blockarray = new Block[downs.size()];
        for (int counter = 0; counter < downs.size(); counter++) blockarray[counter] = downs.get(counter);
        plugin.trees.put(player, blockarray);

    }
    
    public boolean breaksTool (Player player, ItemStack item) {

        if (item != null) {
            if (isTool(item.getTypeId())) {
                short damage = item.getDurability();
                if (isAxe(item.getTypeId())) {
                    damage = (short) (damage + 1);
                } else {
                    damage = (short) (damage + 2);
                }
                if (damage >= item.getType().getMaxDurability()) {
                    return true;
                } else {
                    item.setDurability(damage);
                }
            }
        }
        return false;
    }
	
    public boolean isTool (int ID){
        if (ID == 256
            || ID == 257
            || ID == 258
            || ID == 267
            || ID == 268
            || ID == 269
            || ID == 270
            || ID == 271
            || ID == 272
            || ID == 273
            || ID == 274
            || ID == 275
            || ID == 276
            || ID == 277
            || ID == 278
            || ID == 279
            || ID == 283
            || ID == 284
            || ID == 285
            || ID == 286) return true;
        return false;
    }
	
    public boolean isAxe (int ID) {
        if (ID == 258 || ID == 271 || ID == 275 || ID == 278 || ID == 286) return true;
        return false;
    }
	
    public boolean isLoneLog (Block block) {
        if (block.getRelative(BlockFace.UP).getType() == Material.LOG) return false;
        if (block.getRelative(BlockFace.DOWN).getType() != Material.AIR) return false;
        if (hasHorizontalCompany(block)) return false;
        if (hasHorizontalCompany(block.getRelative(BlockFace.UP))) return false;
        if (hasHorizontalCompany(block.getRelative(BlockFace.DOWN))) return false;
        return true;
    }
	
    public boolean hasHorizontalCompany (Block block) {
        if (block.getRelative(BlockFace.NORTH).getType() == Material.LOG) return true;
        if (block.getRelative(BlockFace.NORTH_EAST).getType() == Material.LOG) return true;
        if (block.getRelative(BlockFace.EAST).getType() == Material.LOG) return true;
        if (block.getRelative(BlockFace.SOUTH_EAST).getType() == Material.LOG) return true;
        if (block.getRelative(BlockFace.SOUTH).getType() == Material.LOG) return true;
        if (block.getRelative(BlockFace.SOUTH_WEST).getType() == Material.LOG) return true;
        if (block.getRelative(BlockFace.WEST).getType() == Material.LOG) return true;
        if (block.getRelative(BlockFace.NORTH_WEST).getType() == Material.LOG) return true;
        return false;
    }
	
    public boolean denyPermission (Player player) {
        //Permissions check
        if (!player.hasPermission("choptree.chop")) {
            return true;
        }
        return false;
    }

    public boolean denyActive (Player player) {

        ChopTreePlayer ctPlayer = new ChopTreePlayer(plugin, player.getName());
        
        if (ctPlayer.isActive()) {
            return false;
        }
        return true;
    }
	
    public boolean denyItem (Player player) {

        if (!plugin.useAnything) {
            ItemStack item = player.getItemInHand();
            if (item != null) {
                for (String tool : plugin.allowedTools) {
                    if (tool.equalsIgnoreCase(item.getType().name())) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
	
    public boolean interruptWhenBreak (Player player) {

        if (plugin.interruptIfToolBreaks) {
            return true;
        }
        return false;
    }	
}
