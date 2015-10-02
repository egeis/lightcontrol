package Util;

import net.minecraft.entity.player.EntityPlayer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class Packet {
	public abstract void encode(ChannelHandlerContext ctx, ByteBuf buffer);
	public abstract void decode(ChannelHandlerContext ctx, ByteBuf buffer);
	public abstract void handleClientSide(EntityPlayer player);
	public abstract void handleServerSide(EntityPlayer player);
}
