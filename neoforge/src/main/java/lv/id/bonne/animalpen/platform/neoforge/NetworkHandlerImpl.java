package lv.id.bonne.animalpen.platform.neoforge;


import java.util.*;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.network.PacketSpec;
import lv.id.bonne.animalpen.platform.services.INetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


public class NetworkHandlerImpl implements INetworkHandler
{
    public void init(PayloadRegistrar registrar)
    {
        this.registrar = registrar;
        this.pending.forEach(c -> c.accept(registrar));
        this.pending.clear();
    }


    @Override
    public <T extends CustomPacketPayload, P extends Player> void register(PacketSpec<T, P> spec)
    {

        Identifier id = spec.type().id();

        if (!PACKETS.add(id))
        {
            throw new IllegalStateException("Duplicate packet: " + id);
        }

        Consumer<PayloadRegistrar> action = reg ->
        {
            switch (spec.direction())
            {
                case PLAY_TO_CLIENT -> reg.playToClient(
                    spec.type(),
                    spec.codec(),
                    (payload, ctx) ->
                        ctx.enqueueWork(() -> spec.handler().accept(payload, (P) ctx.player()))
                );

                case PLAY_TO_SERVER -> reg.playToServer(
                    spec.type(),
                    spec.codec(),
                    (payload, ctx) ->
                        ctx.enqueueWork(() -> spec.handler().accept(payload, (P) ctx.player()))
                );
            }
        };

        if (this.registrar != null)
        {
            action.accept(this.registrar);
        }
        else
        {
            this.pending.add(action);
        }
    }


    @Override
    public <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T packet)
    {
        final Identifier id = packet.type().id();

        if (!PACKETS.contains(id))
        {
            AnimalPen.LOGGER.error("Attempted to send unregistered packet {} to player {}.", id, player);
            throw new IllegalStateException("Attempted to send unregistered packet " + id + " to player " + player);
        }

        PacketDistributor.sendToPlayer(player, packet);
    }


    @Override
    public <T extends CustomPacketPayload> void sendToServer(T packet)
    {

        final Identifier id = packet.type().id();

        if (!PACKETS.contains(id))
        {
            AnimalPen.LOGGER.error("Attempted to send unregistered packet {} to the server.", id);
            throw new IllegalStateException("Attempted to send unregistered packet " + id + " to the server.");
        }

        if (Minecraft.getInstance().player == null)
        {
            AnimalPen.LOGGER.error("Attempted to send packet {} to the server before a player instance is available.",
                id);
            throw new IllegalStateException(
                "Attempted to send packet " + id + " to the server before a player instance is available.");
        }

        Objects.requireNonNull(Minecraft.getInstance().getConnection()).getConnection().
            send(new ServerboundCustomPayloadPacket(packet));
    }

    private final List<Consumer<PayloadRegistrar>> pending = new ArrayList<>();

    private final Set<Identifier> PACKETS = new HashSet<>();

    private PayloadRegistrar registrar;
}