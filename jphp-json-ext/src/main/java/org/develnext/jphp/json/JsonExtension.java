package org.develnext.jphp.json;

import org.develnext.jphp.json.classes.JsonProcessor;
import php.runtime.env.CompileScope;
import php.runtime.ext.support.Extension;

public class JsonExtension extends Extension {
    @Override
    public String getVersion() {
        return "~";
    }

    @Override
    public void onRegister(CompileScope scope) {
        registerNativeClass(scope, JsonSerializable.class);
        registerNativeClass(scope, JsonProcessor.class);
    }
}
