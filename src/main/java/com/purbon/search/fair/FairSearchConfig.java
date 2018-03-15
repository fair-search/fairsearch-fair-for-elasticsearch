package com.purbon.search.fair;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

import java.io.IOException;
import java.nio.file.Path;

public class FairSearchConfig {


    static final Setting<String> SIMPLE_SETTING = Setting.simpleString("custom.simple", Property.NodeScope, Property.Dynamic);
    static final Setting<Float>  FLOAT_SETTING = Setting.floatSetting("custom.float", 10, Property.NodeScope);

    private String _simple;
    private Float _float;

    FairSearchConfig(final Environment env, final Settings settings) {

        Path configDir = env.configFile();
        Path pluginConfig = configDir.resolve("fairsearch-plugin-config.yml");
        Settings customSettings = settings;

          //  customSettings = settings; //Settings.builder().loadFromPath(pluginConfig).build();

        this._simple = SIMPLE_SETTING.get(customSettings);
        this._float  = FLOAT_SETTING.get(customSettings);

    }

    public String getSimple() {
        return _simple;
    }

    public Float getFloat() {
        return _float;
    }
}
