package io.github.bitterfox.json.string.template.core.proto;

public class StringTemplateRuntime {
    private StringTemplateProcessor processor = null;
    public Object process(StringTemplateProcessorFactory f, String[] fragments, Object[] values) {
        if (!f.cacheProcessor()) {
            return f.createProcessor(fragments).process(values);
        }

        if (processor == null) {
            synchronized (this) {
                if (processor == null) {
                    processor = f.createProcessor(fragments);
                }
            }
        }
        return processor.process(values);
    }
}
