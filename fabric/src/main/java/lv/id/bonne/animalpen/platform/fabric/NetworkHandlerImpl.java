package lv.id.bonne.animalpen.platform.fabric;


import java.util.HashSet;
import java.util.Set;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.network.PacketSpec;
import lv.id.bonne.animalpen.platform.services.INetworkHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class NetworkHandlerImpl implements INetworkHandler
{
    @Override
    public <T extends CustomPacketPayload, P extends Player>
    void register(PacketSpec<T, P> spec)
    {

        Identifier id = spec.type().id();

        if (!PACKETS.add(id))
        {
            throw new IllegalStateException("Duplicate packet: " + id);
        }

        switch (spec.direction())
        {
            case PLAY_TO_SERVER ->
            {
                // Register payload type
                PayloadTypeRegistry.serverboundPlay().register(spec.type(), spec.codec());

                // Register handler
                ServerPlayNetworking.registerGlobalReceiver(spec.type(), (payload, context) ->
                {
                    context.server().execute(() ->
                        spec.handler().accept(payload, (P) context.player())
                    );
                });
            }
            case PLAY_TO_CLIENT ->
            {
                // Register payload type
                PayloadTypeRegistry.clientboundPlay().register(spec.type(), spec.codec());

                // Only register client receiver on client
                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
                {
                    ClientPlayNetworking.registerGlobalReceiver(spec.type(), (payload, context) ->
                    {
                        context.client().execute(() ->
                            spec.handler().accept(payload, (P) context.player())
                        );
                    });
                }
            }
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

        ServerPlayNetworking.send(player, packet);
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

        ClientPlayNetworking.send(packet);
    }

    private final Set<Identifier> PACKETS = new HashSet<>();
}