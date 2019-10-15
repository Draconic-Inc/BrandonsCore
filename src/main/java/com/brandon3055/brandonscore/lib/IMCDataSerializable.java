package com.brandon3055.brandonscore.lib;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;

/**
 * Created by brandon3055 on 18/9/19.
 */
public interface IMCDataSerializable {

    void serializeMCD(MCDataOutput output);

    void deSerializeMCD(MCDataInput input);
}
