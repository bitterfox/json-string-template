package io.github.bitterfox.json.string.template.core;

public class JsonStringTemplateProcessorV2Test extends AbstractJsonStringTemplateProcessorTest {
    @SuppressWarnings("removal")
    public JsonStringTemplateProcessorV2Test() {
        super(JsonStringTemplateProcessor.ofV2(new JavaObjectJsonBridge()));
    }
}
