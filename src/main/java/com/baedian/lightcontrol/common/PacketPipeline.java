package com.baedian.lightcontrol.common;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import Util.Packet;

import com.baedian.lightcontrol.LightControl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

/**
 * @author Richard Coan
 * @see http://www.minecraftforge.net/wiki/Netty_Packet_Handling#Within_your_.40Mod_Class
 */
@ChannelHandler.Sharable
public class PacketPipeline extends MessageToMessageCodec<FMLProxyPacket,Packet> {

	protected EnumMap<Side, FMLEmbeddedChannel> channels;
	protected LinkedList<Class<? extends Packet>> packets = new LinkedList<Class<? extends Packet>>();
	protected boolean isPostInitialised = false;
	
	public void postInit()
	{
		
	}
	
	public void init()
	{
		this.channels = NetworkRegistry.INSTANCE.newChannel("LCx", this);
		registerPackets();
	}
	
	 @SideOnly(Side.CLIENT)
     private EntityPlayer getClientPlayer() {
		 return Minecraft.getMinecraft().thePlayer;
     }
	
	public void registerPackets()
	{
		registerPacket(ConfigPacket.class);
	}
	 
	public boolean registerPacket(Class<? extends Packet> clazz)
	{
		if(this.packets.size() > 256)
		{
			LightControl.logger.info("Too many packets registered: Could not register : " + clazz.getCanonicalName());
			return false;
		}
		
		if(this.packets.contains(clazz))
		{
			LightControl.logger.info("Packet already registered: Could not register : " + clazz.getCanonicalName());
			return false;
		}
		
		if(this.isPostInitialised)
		{
			LightControl.logger.info("Registration Period has ended: Could not register : " + clazz.getCanonicalName());
			return false;
		}
		
		this.packets.add(clazz);
		return true;
	}
		
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg,List<Object> out) 
			throws Exception
	{
		ByteBuf buffer = Unpooled.buffer();
		Class<? extends Packet> clazz = msg.getClass();
		
		if(!this.packets.contains(msg.getClass()))
			throw new NullPointerException("No Packet Registered for: " + msg.getClass().getCanonicalName());
		
		byte discriminator = (byte) this.packets.indexOf(clazz);
		buffer.writeByte(discriminator);
		msg.encode(ctx, buffer);
		
		FMLProxyPacket proxyPacket = new FMLProxyPacket(buffer.copy(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
        out.add(proxyPacket);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg,List<Object> out) 
			throws Exception 
	{
		ByteBuf payload = msg.payload();
		byte discriminator = payload.readByte();
		Class<? extends Packet> clazz = this.packets.get(discriminator);
		if (clazz == null)
			throw new NullPointerException("No packet registered for discriminator: " + discriminator);
		
		Packet pkt = clazz.newInstance();
		pkt.decode(ctx, payload.slice());
		
		EntityPlayer player;
		switch (FMLCommonHandler.instance().getEffectiveSide()) {
		    case CLIENT:
		        player = this.getClientPlayer();
		        pkt.handleClientSide(player);
		        break;
		
		    case SERVER:
		        INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
		        player = ((NetHandlerPlayServer) netHandler).playerEntity;
		        pkt.handleServerSide(player);
		        break;
		
		    default:
		}
		
		out.add(pkt);
	}

    /**
     * Send this message to the specified player.
     * Adapted from CPW's code in cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    public void sendTo(Packet message, EntityPlayerMP player) {
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        this.channels.get(Side.SERVER).writeAndFlush(message);
    }

    /**
     * Send this message to the server.
     * Adapted from CPW's code in cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
     *
     * @param message The message to send
     */
    public void sendToServer(Packet message) {
        this.channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.channels.get(Side.CLIENT).writeAndFlush(message);
    }
	
}
