package lv.id.bonne.animalpen.network;


import java.util.function.BiConsumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;


/**
 * A record used to encapsulate all necessary information for registering a network packet
 * within the game mod. It specifies the packet type, its streaming codec, the direction
 * of travel, and the handler logic to process the payload.
 *
 * @param <T> The type of the custom payload implementing {@link CustomPacketPayload}.
 * @param <P> The type of the player context used for handling the packet.
 */
public record PacketSpec<T extends CustomPacketPayload, P extends Player>(
    CustomPacketPayload.Type<T> type,
    StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
    NetworkDirection direction,
    BiConsumer<T, P> handler)
{
    /**
     * Defines the possible network directions for a packet.
     * This dictates whether the packet originates from the client (Player) going to the server,
     * or from the server going to the client (Player).
     */
    public enum NetworkDirection
    {
        /**
         * Indicates the packet travels from the player client to the game server.
         * */
        PLAY_TO_CLIENT,
        /**
         *  Indicates the packet travels from the game server to the player client.
         *  */
        PLAY_TO_SERVER
    }
}


