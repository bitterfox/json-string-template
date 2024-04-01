package io.github.bitterfox.json.string.template.core.proto;

import java.util.List;

public class StringTemplateSTRDebug implements StringTemplateProcessorFactory {
    public static final StringTemplateProcessorFactory STR = new StringTemplateSTRDebug();

    @Override
    public StringTemplateProcessor createProcessor(String[] fragments) {
        System.out.println("Create new processor, some processor may parse fragments, so slow");
        return values -> {
            System.out.println("Process string template");
            return StringTemplate.interpolate(List.of(fragments), List.of(values));
        };
    }
}
