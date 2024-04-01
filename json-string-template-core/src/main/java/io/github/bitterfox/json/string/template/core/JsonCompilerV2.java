package io.github.bitterfox.json.string.template.core;

import static java.lang.StringTemplate.RAW;
import static java.lang.constant.ConstantDescs.CD_Boolean;
import static java.lang.constant.ConstantDescs.CD_CallSite;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.CD_Map;
import static java.lang.constant.ConstantDescs.CD_MethodHandles_Lookup;
import static java.lang.constant.ConstantDescs.CD_MethodType;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_void;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DirectMethodHandleDesc.Kind;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.bitterfox.json.string.template.core.JsonAST.JASTArray;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTFalse;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTJavaObject;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNull;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNumberNumber;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNumberString;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTObject;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTString;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTTrue;
import io.github.bitterfox.json.string.template.core.JsonPosition.FragmentPosition;

public class JsonCompilerV2<T> {
    private static final AtomicInteger CLASS_ID = new AtomicInteger();

    private final JsonCompilerBridge<T> bridge;

    public JsonCompilerV2(JsonCompilerBridge<T> bridge) {
        this.bridge = bridge;
    }

    void compile(int args, JsonAST json) throws IOException {
        ClassDesc clazz = ClassDesc.of("JSON" + CLASS_ID.getAndIncrement());

        MethodTypeDesc processMethodSignature = MethodTypeDesc.of(
                bridge.jsonClass(),
                Stream.generate(() -> ConstantDescs.CD_Object).limit(args).collect(Collectors.toList())
        );

        ClassFile.of()
                .buildTo(Path.of(STR."/tmp/\{clazz.displayName()}.class"), clazz, classBuilder -> {
                    classBuilder.withMethod(
                            ConstantDescs.CLASS_INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void), Modifier.PUBLIC | Modifier.STATIC,
                            classInitMethod -> {
                                classInitMethod.withCode(
                                        classInitCode -> {
                                            classBuilder.withMethod(
                                                    "process", processMethodSignature, Modifier.PUBLIC | Modifier.STATIC,
                                                    processMethod -> {

                                                        processMethod.withCode(
                                                                processCode -> {
                                                                    JsonCompilerV2Context context =
                                                                            new JsonCompilerV2Context(clazz, classBuilder, classInitCode, processCode);

                                                                    json.visit(new Visitor(context));
                                                                    classInitCode.return_();
                                                                    processCode.areturn();
                                                                });
                                                    });
                                        });
                            });
                });
    }

    class Visitor implements JsonAST.JsonVisitor<Void> {
        private JsonCompilerV2Context context;
        private CodeBuilder code;
        private boolean caching = false;

        public Visitor(JsonCompilerV2Context context) {
            this.context = context;
            this.code = context.processCode;
        }

        @Override
        public Void visitObject(JASTObject that) {
            if (!caching && that.visit(new CheckCacheable())) {
                caching = true;
                try {
                    // Cache whole object
                    bridge.compileCreateJsonObjectIntermediate(context, context.classInitCode,
                                                               that.fields().size(), null);
                    that.fields().forEach(e -> {
                        context.classInitCode.dup();
                        compileJsonObjectKey(e.getKey(), context.classInitCode);
                        code = context.classInitCode;
                        try {
                            e.getValue().visit(this);
                        } finally {
                            code = context.processCode;
                        }
                        bridge.compilePutToJsonObjectIntermediate(context, context.classInitCode);
                    });
                    bridge.compileFinishJsonObjectIntermediate(context, context.classInitCode);
                    String field = context.newField(bridge.jsonClass());
                    context.classInitCode.putstatic(context.clazz, field, bridge.jsonClass());
                    context.processCode.getstatic(context.clazz, field, bridge.jsonClass());
                    return null;
                } finally {
                    caching = false;
                }
            }

            if (!caching && bridge.cacheJsonObjectIntermediate()) {
                Map<Boolean, List<Entry<JsonAST, JsonAST>>> fields =
                        that.fields().stream()
                            .collect(Collectors.partitioningBy(
                                    e -> e.getKey().visit(new CheckCacheable())
                                         && e.getValue().visit(new CheckCacheable())));
                List<Entry<JsonAST, JsonAST>> cacheableFields = fields.get(true);


                if (!cacheableFields.isEmpty()) {
                    caching = true;
                    String field;
                    try {
                        bridge.compileCreateJsonObjectIntermediate(context, context.classInitCode,
                                                                   cacheableFields.size(), null);
                        cacheableFields.forEach(e -> {
                            context.classInitCode.dup();
                            compileJsonObjectKey(e.getKey(), context.classInitCode);
                            code = context.classInitCode;
                            try {
                                e.getValue().visit(this);
                            } finally {
                                code = context.processCode;
                            }
                            bridge.compilePutToJsonObjectIntermediate(context, context.classInitCode);
                        });

                        field = context.newField(bridge.jsonObjectIntermediateClass());
                        context.classInitCode.putstatic(context.clazz, field,
                                                        bridge.jsonObjectIntermediateClass());
                    } finally {
                        caching = false;
                    }

                    bridge.compileCreateJsonObjectIntermediate(context, context.processCode,
                                                               that.fields().size(), field);
                    fields.get(false).forEach(e -> {
                        context.processCode.dup();
                        compileJsonObjectKey(e.getKey(), context.processCode);
                        e.getValue().visit(this);
                        bridge.compilePutToJsonObjectIntermediate(context, context.processCode);
                    });
                    bridge.compileFinishJsonObjectIntermediate(context, context.processCode);
                }

                return null;
            }

            bridge.compileCreateJsonObjectIntermediate(context, code, that.fields().size(), null);
            that.fields().forEach(e -> {
                code.dup();
                compileJsonObjectKey(e.getKey(), code);
                e.getValue().visit(this);
                bridge.compilePutToJsonObjectIntermediate(context, code);
            });
            bridge.compileFinishJsonObjectIntermediate(context, code);

            return null;
        }

        private void compileJsonObjectKey(JsonAST that, CodeBuilder code) {
            switch (that) {
                case JASTString str -> compileStringTemplate(str, code);
                case JASTJavaObject obj -> {
                    // TODO
                }
                default -> throw new IllegalStateException("Unexpected Json element in JsonObject key");
            }
        }

        @Override
        public Void visitArray(JASTArray that) {
            if (!caching && that.visit(new CheckCacheable())) {
                caching = true;
                try {
                    // Cache whole object
                    bridge.compileCreateJsonArrayIntermediate(context, context.classInitCode,
                                                               that.values().size(), null);
                    that.values().forEach(v -> {
                        context.classInitCode.dup();
                        code = context.classInitCode;
                        try {
                            v.visit(this);
                        } finally {
                            code = context.processCode;
                        }
                        bridge.compilePutToJsonArrayIntermediate(context, context.classInitCode);
                    });
                    bridge.compileFinishJsonArrayIntermediate(context, context.classInitCode);
                    String field = context.newField(bridge.jsonClass());
                    context.classInitCode.putstatic(context.clazz, field, bridge.jsonClass());
                    context.processCode.getstatic(context.clazz, field, bridge.jsonClass());
                    return null;
                } finally {
                    caching = false;
                }
            }

            if (!caching && bridge.cacheJsonArrayIntermediate()) {
                Map<Boolean, List<JsonAST>> values =
                        that.values().stream()
                            .collect(Collectors.partitioningBy(v -> v.visit(new CheckCacheable())));
                List<JsonAST> cacheableValues = values.get(true);

                if (!cacheableValues.isEmpty()) {
                    caching = true;
                    String field;
                    try {
                        bridge.compileCreateJsonArrayIntermediate(context, context.classInitCode,
                                                                  cacheableValues.size(), null);
                        cacheableValues.forEach(v -> {
                            context.classInitCode.dup();
                            code = context.classInitCode;
                            try {
                                v.visit(this);
                            } finally {
                                code = context.processCode;
                            }
                            bridge.compilePutToJsonArrayIntermediate(context, context.classInitCode);
                        });

                        field = context.newField(bridge.jsonArrayIntermediateClass());
                        context.classInitCode.putstatic(context.clazz, field,
                                                        bridge.jsonArrayIntermediateClass());
                    } finally {
                        caching = false;
                    }

                    bridge.compileCreateJsonArrayIntermediate(context, context.processCode,
                                                              that.values().size(), field);
                    values.get(false).forEach(v -> {
                        context.processCode.dup();
                        v.visit(this);
                        bridge.compilePutToJsonArrayIntermediate(context, context.processCode);
                    });
                    bridge.compileFinishJsonArrayIntermediate(context, context.processCode);

                    return null;
                }
            }

            bridge.compileCreateJsonObjectIntermediate(context, code, that.values().size(), null);
            that.values().forEach(v -> {
                code.dup();
                v.visit(this);
                bridge.compilePutToJsonArrayIntermediate(context, code);
            });
            bridge.compileFinishJsonArrayIntermediate(context, code);

            return null;
        }

        private void compileStringTemplate(JASTString that, CodeBuilder code) {
            if (that.fragments().size() == 1) {
                code.ldc(context.classBuilder.constantPool().stringEntry(that.fragments().get(0)));
                bridge.compileString(context, code);
                return;
            }

            StringBuilder recipe = new StringBuilder();
            List<String> constants = new ArrayList<>();
            for (int i = 0; i < that.fragments().size(); i++) {
                if (i != 0) {
                    recipe.append('\u0001');
                }

                String fragment = that.fragments().get(i);
                if (fragment.contains("\u0001")) {
                    recipe.append('\u0002');
                    constants.add(fragment);
                } else {
                    recipe.append(fragment);
                }
            }

            ConstantDesc[] bootstrapArgs =
                    Stream.concat(Stream.of(recipe.toString()), constants.stream())
                          .toArray(ConstantDesc[]::new);

            int start = ((FragmentPosition) that.pos().startInclusive()).index();
            List<ClassDesc> params = new ArrayList<>();
            for (int i = 0; i < that.fragments().size() - 1; i++) {
                code.aload(code.parameterSlot(start + i));
                params.add(CD_Object);
            }

            DirectMethodHandleDesc makeConcatWithConstants =
                    MethodHandleDesc.ofMethod(Kind.STATIC, ClassDesc.of("java.lang.invoke.StringConcatFactory"),
                                              "makeConcatWithConstants",
                                              MethodTypeDesc.of(CD_CallSite, CD_MethodHandles_Lookup, CD_String,
                                                                CD_MethodType, CD_String, CD_Object.arrayType()));
            code.invokedynamic(
                    DynamicCallSiteDesc.of(
                            makeConcatWithConstants,
                            "makeConcatWithConstants",
                            MethodTypeDesc.of(CD_String, params),
                            bootstrapArgs));
        }

        @Override
        public Void visitString(JASTString that) {
            if (!caching && that.visit(new CheckCacheable())) {
                if (bridge.cacheString()) {
                    context.classInitCode.ldc(context.classBuilder.constantPool().stringEntry(that.fragments().get(0)));
                    bridge.compileString(context, context.classInitCode);
                    String field = context.newField(bridge.jsonClass());
                    context.classInitCode.putstatic(context.clazz, field, bridge.jsonClass());

                    context.processCode.getstatic(context.clazz, field, bridge.jsonClass());
                    return null;
                }
            }

            compileStringTemplate(that, code);
            bridge.compileString(context, code);
            return null;
        }

        @Override
        public Void visitNumber(JASTNumberString that) {
            // TODO cache
            bridge.compileNumber(context, code, that.number());
            return null;
        }

        @Override
        public Void visitNumber(JASTNumberNumber that) {
            // TODO cache
            bridge.compileNumber(context, code, that.number());
            return null;
        }

        @Override
        public Void visitTrue(JASTTrue that) {
            // TODO cache
            bridge.compileTrue(context, code);
            return null;
        }

        @Override
        public Void visitFalse(JASTFalse that) {
            // TODO cache
            bridge.compileFalse(context, code);
            return null;
        }

        @Override
        public Void visitNull(JASTNull that) {
            // TODO cache
            bridge.compileNull(context, code);
            return null;
        }

        @Override
        public Void visitJavaObject(JASTJavaObject that) {
            bridge.compileJavaObject(context, code, that.pos().index());
            return null;
        }
    }

    /**
     * Cacheable true when elements are constant (no refer to value of StringTemplate) and it's immutable in JsonWorld
     */
    class CheckCacheable implements JsonAST.JsonVisitor<Boolean> {

        @Override
        public Boolean visitObject(JASTObject that) {
            return bridge.isJsonObjectImmutable()
                   && that.fields()
                          .stream()
                          .allMatch(v -> v.getKey().visit(this) && v.getValue().visit(this));
        }

        @Override
        public Boolean visitArray(JASTArray that) {
            return bridge.isJsonArrayImmutable() && that.values().stream().allMatch(v -> v.visit(this));
        }

        @Override
        public Boolean visitString(JASTString that) {
            return bridge.isStringImmutable() && that.fragments().size() == 1;
        }

        @Override
        public Boolean visitNumber(JASTNumberString that) {
            return bridge.isNumberImmutable();
        }

        @Override
        public Boolean visitNumber(JASTNumberNumber that) {
            return bridge.isNumberImmutable();
        }

        @Override
        public Boolean visitTrue(JASTTrue that) {
            return bridge.isTrueImmutable();
        }

        @Override
        public Boolean visitFalse(JASTFalse that) {
            return bridge.isFalseImmutable();
        }

        @Override
        public Boolean visitNull(JASTNull that) {
            return bridge.isNullImmutable();
        }

        @Override
        public Boolean visitJavaObject(JASTJavaObject that) {
            return false;
        }
    }

    static class JavaJsonCompilerBridge implements JsonCompilerBridge<Object> {

        public static void main(String[] args) throws IOException {
            String name = "";
//            StringTemplate template = RAW."'Hello \{name} world'";
//            StringTemplate template = RAW."'Hello world'";
            StringTemplate template = RAW."{'text': 'hello world'}";
            JsonParserV2 parser = new JsonParserV2(new JsonTokenizer(template , JsonStringTemplateConfiguration.DEFAULT), JsonStringTemplateConfiguration.DEFAULT);
            JsonAST ast = parser.parseJson();

            JsonCompilerV2<Object> compiler = new JsonCompilerV2<>(new JavaJsonCompilerBridge());
            compiler.compile(1, ast);
        }

        @Override
        public ClassDesc jsonClass() {
            return CD_Object;
        }

        @Override
        public ClassDesc jsonObjectIntermediateClass() {
            return CD_Map;
        }

        @Override
        public ClassDesc jsonArrayIntermediateClass() {
            return CD_List;
        }

        @Override
        public void compileCreateJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code,
                                                        int size, String field) {
            code.new_(ClassDesc.of("java.util.HashMap"))
                .dup()
                .ldc(context.classBuilder.constantPool().intEntry(size))
                .invokespecial(ClassDesc.of("java.util.HashMap"), ConstantDescs.INIT_NAME, MethodTypeDesc.of(CD_void, CD_int));

            if (field != null) {
                code.dup()
                    .getstatic(context.clazz, field, jsonClass())
                    .invokevirtual(CD_Map, "putAll", MethodTypeDesc.of(CD_void, CD_Map));
            }
        }

        @Override
        public void compilePutToJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code) {

        }

        @Override
        public void compileFinishJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code) {

        }

        @Override
        public boolean cacheJsonObjectIntermediate() {
            return true;
        }

        @Override
        public boolean isJsonObjectImmutable() {
            return false;
        }

        @Override
        public void compileCreateJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code,
                                                       int size, String field) {

        }

        @Override
        public void compilePutToJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code) {

        }

        @Override
        public void compileFinishJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code) {

        }

        @Override
        public boolean cacheJsonArrayIntermediate() {
            return true;
        }

        @Override
        public boolean isJsonArrayImmutable() {
            return false;
        }

        @Override
        public void compileString(JsonCompilerV2Context context, CodeBuilder code) {
        }

        @Override
        public boolean isStringImmutable() {
            return true;
        }

        @Override
        public boolean cacheString() {
            return false;
        }

        @Override
        public void compileNumber(JsonCompilerV2Context context, CodeBuilder code, String number) {
            JsonCompilerBridge.super.compileNumber(context, code, number);
        }

        @Override
        public boolean isNumberImmutable() {
            return true;
        }

        @Override
        public void compileTrue(JsonCompilerV2Context context, CodeBuilder code) {
            code.getstatic(CD_Boolean, "TRUE", CD_Boolean);
        }

        @Override
        public boolean isTrueImmutable() {
            return true;
        }

        @Override
        public void compileFalse(JsonCompilerV2Context context, CodeBuilder code) {
            code.getstatic(CD_Boolean, "FALSE", CD_Boolean);
        }

        @Override
        public boolean isFalseImmutable() {
            return true;
        }

        @Override
        public void compileNull(JsonCompilerV2Context context, CodeBuilder code) {
            code.aconst_null();
        }

        @Override
        public boolean isNullImmutable() {
            return true;
        }

        @Override
        public void compileJavaObject(JsonCompilerV2Context context, CodeBuilder processCode, int argPos) {
            processCode.aload(processCode.parameterSlot(argPos));
        }
    }

    public static void main(String[] args) throws IOException {
        String name = "";
        JsonParserV2 parser = new JsonParserV2(new JsonTokenizer(RAW."\"hello, \{name} world\"", JsonStringTemplateConfiguration.DEFAULT), JsonStringTemplateConfiguration.DEFAULT);
        JsonAST ast = parser.parseJson();
//        JsonCompilerV2<Object> compiler = new JsonCompilerV2<>();
//        compiler.bridge = new JsonCompilerBridge<Object>() {
//        };
//        compiler.compile(1, ast);

//        ClassDesc classDesc = ClassDesc.of("Strings");
//
//        ClassFile clazz = java.lang.classfile.ClassFile.of();
//        clazz.buildTo(Path.of("/tmp/Strings.class"), classDesc, builder -> {
//            builder.withField("builder", ClassDesc.of("java.lang.StringBuilder"),
//                              Modifier.PRIVATE | Modifier.STATIC);
//            builder.withMethod(ConstantDescs.CLASS_INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void),
//                               Modifier.PUBLIC | Modifier.STATIC,
//                               cinit -> {
//                                   cinit.withCode(cinit_code -> {
//                                       cinit_code.new_(ClassDesc.of("java.lang.StringBuilder"))
//                                                         .dup()
//                                                 .invokespecial(
//                                                         ClassDesc.of("java.lang.StringBuilder"),
//                                                         ConstantDescs.INIT_NAME,
//                                                         MethodTypeDesc.of(ConstantDescs.CD_void))
//                                                         .putstatic(cinit_code.constantPool().fieldRefEntry(classDesc, "builder", ClassDesc.of("java.lang.StringBuilder")))
//                                               .return_();
//
//
//
//                                       builder.withMethod(
//                                               "concat", MethodTypeDesc.of(
//                                                       ConstantDescs.CD_String,
//                                                       ConstantDescs.CD_String,
//                                                       ConstantDescs.CD_String),
//                                               Modifier.PUBLIC | Modifier.STATIC,
//                                               method -> {
//                                                   method.withCode(code -> {
//                                                       code
//                                                               .new_(ClassDesc.of("java.lang.StringBuilder"))
//                                                               .dup()
//                                                               .aload(code.parameterSlot(0))
//                                                               .invokespecial(
//                                                                       ClassDesc.of("java.lang.StringBuilder"),
//                                                                       ConstantDescs.INIT_NAME,
//                                                                       MethodTypeDesc.of(ConstantDescs.CD_void,
//                                                                                         ConstantDescs.CD_String))
//                                                               .aload(code.parameterSlot(1))
//                                                               .invokevirtual(
//                                                                       code.constantPool().methodRefEntry(
//                                                                               ClassDesc.of(
//                                                                                       "java.lang.StringBuilder"),
//                                                                               "append",
//                                                                               MethodTypeDesc.of(ClassDesc.of(
//                                                                                                         "java.lang.StringBuilder"),
//                                                                                                 ConstantDescs.CD_String)))
//                                                               .invokevirtual(
//                                                                       code.constantPool().methodRefEntry(
//                                                                               ClassDesc.of("java.lang.Object"),
//                                                                               "toString",
//                                                                               MethodTypeDesc.of(
//                                                                                       ConstantDescs.CD_String)))
//                                                               .areturn();
//                                                   });
//                                               });
//
//                                       builder.withMethod(
//                                               "append", MethodTypeDesc.of(
//                                                       ConstantDescs.CD_String,
//                                                       ConstantDescs.CD_String),
//                                               Modifier.PUBLIC | Modifier.STATIC,
//                                               method -> {
//                                                   method.withCode(code -> {
//                                                       code
//                                                               .getstatic(classDesc, "builder", ClassDesc.of("java.lang.StringBuilder"))
//                                                               .aload(code.parameterSlot(0))
//                                                               .invokevirtual(ClassDesc.of("java.lang.StringBuilder"), "append", MethodTypeDesc.of(
//                                                                       ClassDesc.of("java.lang.StringBuilder"),
//                                                                       ConstantDescs.CD_String
//                                                               ))
//                                                               .invokevirtual(
//                                                                       code.constantPool().methodRefEntry(
//                                                                               ClassDesc.of("java.lang.Object"),
//                                                                               "toString",
//                                                                               MethodTypeDesc.of(
//                                                                                       ConstantDescs.CD_String)))
//                                                               .areturn();
//                                                   });
//                                               });
//                                   });
//                               });
//        });

    }
}
