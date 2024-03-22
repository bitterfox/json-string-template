package io.github.bitterfox.json.string.template.core;

public class JsonStringTemplateProcessorV1Test extends AbstractJsonStringTemplateProcessorTest {
    @SuppressWarnings("removal")
    public JsonStringTemplateProcessorV1Test() {
        super(JsonStringTemplateProcessor.ofV1(new JavaObjectJsonBridge()));
    }
}
