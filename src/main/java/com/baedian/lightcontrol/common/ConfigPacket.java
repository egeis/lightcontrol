package com.baedian.lightcontrol.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Property;
import Util.Packet;

public class ConfigPacket extends Packet {

	public Map<String, Property> blockList;
	
	public ConfigPacket() {}
	
	public ConfigPacket(Map<String, Property> list)
	{
		this.blockList = list;
	}
	
	@Override
	public void encode(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		try {
			byte[] src = serialize(blockList);
			int a = src.length;
			
			buffer.writeInt(a);
			buffer.writeBytes(src);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void decode(ChannelHandlerContext ctx, ByteBuf buffer)
	{	
		int a = buffer.readInt();
		byte[] dst = new byte[a];
		
		buffer.readBytes(dst);
		
		try {
			this.blockList = (Map<String, Property>) deserialize(dst);
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player) {}

	@Override
	public void handleServerSide(EntityPlayer player) {}
	
	public static byte[] serialize(Object obj) throws IOException
	{
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
	}
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException 
	{
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
	}

}
