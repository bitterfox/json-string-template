package io.github.bitterfox.json.string.template.core.proto;

public interface StringTemplateProcessorFactory {
    StringTemplateProcessor createProcessor(String[] fragments);
    default boolean cacheProcessor() {
        return true;
    }
}
