package com.reigindustries.catalyst.internal;

import com.reigindustries.catalyst.internal.config.Config;
import com.reigindustries.catalyst.internal.config.Option;

public class Internal {

    public void setOption(Option key, String value) {
        Config.set(key, value);
    }

}
