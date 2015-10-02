package com.baedian.lightcontrol;

import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.baedian.lightcontrol.common.PacketPipeline;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = LightControl.MODID, version = LightControl.VERSION)
public class LightControl
{
    public static final String MODID = "lightcontrol";
    public static final String VERSION = "0.3";
    
    protected static Configuration conf;
    
    protected static boolean DEBUG_MODE = false;
    protected static boolean UPDATE_MODE = false;
    
    public static Logger logger = LogManager.getLogger("LightControl");
    public static final PacketPipeline packetPipeline = new PacketPipeline();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	conf = new Configuration(event.getSuggestedConfigurationFile());
    	conf.load();
    	
    	DEBUG_MODE = conf.get(Configuration.CATEGORY_GENERAL, "Debug Mode", true, "Enabled Debug Messages during Initialization.").getBoolean();
    	UPDATE_MODE = conf.get(Configuration.CATEGORY_GENERAL, "Force-Update", false, "Updateds the blocks list, filling in missing blocks with set light values.").getBoolean();
    	
    	if(UPDATE_MODE)
    	{
    		conf.removeCategory(conf.getCategory(Configuration.CATEGORY_GENERAL));
        	conf.get(Configuration.CATEGORY_GENERAL, "Debug Mode", DEBUG_MODE, "Enabled Debug Messages during Initialization.").getBoolean();
        	conf.get(Configuration.CATEGORY_GENERAL, "Force-Update", false, "Updates the blocks list, filling in missing blocks with set light values.");
    	}
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	packetPipeline.init();
    }
    
    @SideOnly(Side.SERVER)
    public void buildBlockConfig()
    {    	
    	Map<String, Property> list = conf.getCategory("blocks").getValues();    
    	conf.removeCategory(conf.getCategory("blocks"));
    	
    	if(UPDATE_MODE || list.isEmpty())
		{
    		Iterator<?> iterator = Block.blockRegistry.iterator();
			
			while(iterator.hasNext())
	        {
	        	Block block = (Block) iterator.next();
	        	 
	        	String name = Block.blockRegistry.getNameForObject(block);
	        	int value = (list.containsKey(name))?list.get(name).getInt():block.getLightValue();
	        		        	
	        	if(value > 0) {
	        		if(DEBUG_MODE) logger.info(name+">>>"+value);
	        		conf.get("blocks", name, value);
	        	}
	        } 
		}    	
    }
    
    public void setupBlockLightValues()
    {
    	Map<String, Property> list = conf.getCategory("blocks").getValues();    
    	Iterator<?> iterator = list.entrySet().iterator();

		while(iterator.hasNext())
		{
			@SuppressWarnings("unchecked")
			Map.Entry<String, Property> pair = (Map.Entry<String, Property>) iterator.next();
			
			if(Block.blockRegistry.containsKey(pair.getKey())) 
				Block.getBlockFromName(pair.getKey()).setLightLevel(pair.getValue().getInt()/15.0f);				
		}
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {        
    	packetPipeline.postInit();
    	
    	buildBlockConfig();
    	
		conf.save();
    }
    
    

}
