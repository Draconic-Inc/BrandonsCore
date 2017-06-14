package com.brandon3055.brandonscore.lib.datamanager;

import javax.annotation.Nonnull;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public interface IDataManagerProvider {

    @Nonnull IDataManager getDataManager();
}
