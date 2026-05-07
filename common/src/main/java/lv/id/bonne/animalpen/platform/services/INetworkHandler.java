//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.platform.services;


import java.util.List;

import lv.id.bonne.animalpen.network.PacketSpec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public interface INetworkHandler
{
    /**
     * Registers a new network packet type.
     * This method associates a specific {@link CustomPacketPayload} type with a packet specification,
     * ensuring that network handling logic is correctly bound for both client and server sides.
     *
     * @param spec The {@link PacketSpec} containing the details for the payload, parameterized by the payload type T and player type P.
     * @param <T> The type of the custom network payload.
     * @param <P> The expected type of the player involved in the packet transmission.
     */
    <T extends CustomPacketPayload, P extends Player> void register(PacketSpec<T, P> spec);


    /**
     * Sends a custom packet payload to a specific {@link ServerPlayer}.
     * This is the primary method for one-to-one secure communication between the server and client.
     *
     * @param player The target {@link ServerPlayer} who will receive the packet.
     * @param packet The {@link CustomPacketPayload} containing the data to be sent.
     * @param <T> The type of the custom payload.
     */
    <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T packet);


    /**
     * Sends a custom packet payload to each player in given list.
     *
     * @param playerList The list of players who will receive the packet.
     * @param packet The {@link CustomPacketPayload} containing the data to be sent to the server.
     * @param <T> The type of the custom payload.
    */
    default <T extends CustomPacketPayload> void sendToPlayers(List<ServerPlayer> playerList, T packet)
    {
        playerList.forEach(player -> sendToPlayer(player, packet));
    }


    /**
     * Sends a custom packet payload to a server.
     * This is the primary method for one-to-one secure communication between the client and server.
     *
     * @param packet The {@link CustomPacketPayload} containing the data to be sent.
     * @param <T> The type of the custom payload.
     */
    <T extends CustomPacketPayload> void sendToServer(T packet);
}