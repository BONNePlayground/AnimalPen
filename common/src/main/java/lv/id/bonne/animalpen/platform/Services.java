package lv.id.bonne.animalpen.platform;


import java.util.ServiceLoader;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.platform.services.IItemTransferHelper;
import lv.id.bonne.animalpen.platform.services.INetworkHandler;
import lv.id.bonne.animalpen.platform.services.IPlatformHelper;
import lv.id.bonne.animalpen.platform.services.IRegistryHelper;


public class Services
{
    public static <T> T load(Class<T> clazz)
    {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        AnimalPen.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);

    public static final IItemTransferHelper ITEM_TRANSFER = load(IItemTransferHelper.class);

    public static final INetworkHandler NETWORK = load(INetworkHandler.class);
}