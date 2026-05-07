//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.network;


import lv.id.bonne.animalpen.network.packets.*;
import lv.id.bonne.animalpen.platform.services.INetworkHandler;


public final class NetworkPackets
{
    public static void register(INetworkHandler handler)
    {
        handler.register(new PacketSpec<>(
            UpdateDisplayAnimalData.ID,
            UpdateDisplayAnimalData.STREAM_CODEC,
            PacketSpec.NetworkDirection.PLAY_TO_SERVER,
            UpdateDisplayAnimalData::handle));
        handler.register(new PacketSpec<>(
            RemoveDisplayAnimalData.ID,
            RemoveDisplayAnimalData.STREAM_CODEC,
            PacketSpec.NetworkDirection.PLAY_TO_SERVER,
            RemoveDisplayAnimalData::handle));
        handler.register(new PacketSpec<>(
            UpdateConfigurationData.ID,
            UpdateConfigurationData.STREAM_CODEC,
            PacketSpec.NetworkDirection.PLAY_TO_SERVER,
            UpdateConfigurationData::handle));

        handler.register(new PacketSpec<>(AnimalInteractionSyncStartPacket.ID,
            AnimalInteractionSyncStartPacket.STREAM_CODEC,
            PacketSpec.NetworkDirection.PLAY_TO_CLIENT,
            AnimalInteractionSyncStartPacket::handle));
        handler.register(new PacketSpec<>(AnimalInteractionSyncEntityPacket.ID,
            AnimalInteractionSyncEntityPacket.STREAM_CODEC,
            PacketSpec.NetworkDirection.PLAY_TO_CLIENT,
            AnimalInteractionSyncEntityPacket::handle));
        handler.register(new PacketSpec<>(AnimalInteractionSyncEndPacket.ID,
            AnimalInteractionSyncEndPacket.STREAM_CODEC,
            PacketSpec.NetworkDirection.PLAY_TO_CLIENT,
            AnimalInteractionSyncEndPacket::handle));
        handler.register(new PacketSpec<>(UpdateVariantScreenData.ID,
            UpdateVariantScreenData.STREAM_CODEC,
            PacketSpec.NetworkDirection.PLAY_TO_CLIENT,
            UpdateVariantScreenData::handle));
    }
}
