package com.baedian.lightcontrol;

import java.io.File;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = LightControl.MODID, version = LightControl.VERSION)
public class LightControl
{
    public static final String MODID = "lightcontrol";
    public static final String VERSION = "0.2";
    
    protected static Configuration conf;
    protected static Configuration blocks;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	File file = new File(event.getModConfigurationDirectory().toString()+"/lc");
    	
    	if(!file.exists())
    		file.mkdirs();
    	
    	file = new File(event.getModConfigurationDirectory().toString()+"/lc/lightcontrol.cfg");
    	conf = new Configuration(file);
    	
    	file = new File(event.getModConfigurationDirectory().toString()+"/lc/blocks.cfg");
    	blocks = new Configuration(file);
    	
    	conf.load();
    	blocks.load();
	}
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	
    }
}
