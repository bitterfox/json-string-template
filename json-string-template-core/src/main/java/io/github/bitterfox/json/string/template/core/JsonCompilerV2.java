package io.github.bitterfox.json.string.template.core;

import static java.lang.StringTemplate.RAW;
import static java.lang.constant.ConstantDescs.CD_CallSite;
import static java.lang.constant.ConstantDescs.CD_MethodHandles_Lookup;
import static java.lang.constant.ConstantDescs.CD_MethodType;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;

import java.io.IOException;
import java.io.UncheckedIOException;
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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
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
import io.github.bitterfox.json.string.template.core.JsonAST.JsonVisitor;
import io.github.bitterfox.json.string.template.core.JsonPosition.FragmentPosition;

public class JsonCompilerV2<T> {
    private static final AtomicInteger CLASS_ID = new AtomicInteger();

    private final JsonCompilerBridge<T> bridge;

    public JsonCompilerV2(JsonCompilerBridge<T> bridge) {
        this.bridge = bridge;
    }

    MethodHandle compile(int args, JsonAST json) {
        String className = "JSON" + CLASS_ID.getAndIncrement();
        ClassDesc classDesc = ClassDesc.of(className);

        MethodTypeDesc processMethodSignature = MethodTypeDesc.of(
                bridge.jsonClass(),
                Stream.generate(() -> ConstantDescs.CD_Object).limit(args).collect(Collectors.toList())
        );

        byte[] classBytes = ClassFile.of()
//                 .buildTo(Path.of(STR."/tmp/\{clazz.displayName()}.class"), clazz, classBuilder -> {
                .build(classDesc, classBuilder -> {
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
                                                                            new JsonCompilerV2Context(classDesc, classBuilder, classInitCode, processCode);

                                                                    json.visit(new Visitor(context));
                                                                    classInitCode.return_();
                                                                    processCode.areturn();
                                                                });
                                                    });
                                        });
                            });
                });

        try {
            Files.write(Path.of(STR."/tmp/\{className}.class"), classBytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try {
            Class<?> clazz = new ClassLoader() {
                @Override
                protected Class<?> findClass(String name) {
                    return defineClass(name, classBytes, 0, classBytes.length);
                }
            }.loadClass(className);

            return MethodHandles.lookup().findStatic(clazz, "process",
                                                     processMethodSignature.resolveConstantDesc(MethodHandles.lookup()));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    class Visitor extends CompileVisitor {
        private final CompileVisitor compileToStaticInitializer;

        public Visitor(JsonCompilerV2Context context) {
            super(context, context.processCode);
            compileToStaticInitializer = new CompileVisitor(context, context.classInitCode);
        }

        public String cache(ClassDesc classDesc) {
            String field = context.newField(classDesc);
            context.classInitCode.putstatic(context.clazz, field, classDesc);
            return field;
        }
        public String cacheAndGet(ClassDesc classDesc) {
            String field = cache(classDesc);
            context.processCode.getstatic(context.clazz, field, classDesc);
            return field;
        }

        @Override
        public Void visitObject(JASTObject that) {
            if (that.visit(new CheckCacheable())) {
                // Cache whole object
                compileToStaticInitializer.compileJsonObject(that.fields(), that.fields().size(), null, true);
                cacheAndGet(bridge.jsonClass());
                return null;
            }

            String field = null;
            List<Entry<JsonAST, JsonAST>> fields = that.fields();
            if (bridge.cacheJsonObjectIntermediate()) {
                Map<Boolean, List<Entry<JsonAST, JsonAST>>> biFields =
                        that.fields().stream()
                            .collect(Collectors.partitioningBy(
                                    e -> e.getKey().visit(new CheckCacheable())
                                         && e.getValue().visit(new CheckCacheable())));
                List<Entry<JsonAST, JsonAST>> cacheableFields = biFields.get(true);


                if (!cacheableFields.isEmpty()) {
                    compileToStaticInitializer.compileJsonObject(cacheableFields, cacheableFields.size(), null, false);

                    field = cache(bridge.jsonObjectIntermediateClass());
                    fields = biFields.get(false);
                }
            }

            compileJsonObject(fields, fields.size(), field, true);

            return null;
        }

        @Override
        public Void visitArray(JASTArray that) {
            System.out.println(that);
            if (that.visit(new CheckCacheable())) {
                // Cache whole object
                compileToStaticInitializer.compileJsonArray(that.values(), that.values().size(), null, true);
                cacheAndGet(bridge.jsonClass());
                return null;
            }

            String field = null;
            List<JsonAST> values = that.values();

            if (bridge.cacheJsonArrayIntermediate()) {
                Map<Boolean, List<JsonAST>> biValues =
                        that.values().stream()
                            .collect(Collectors.partitioningBy(v -> v.visit(new CheckCacheable())));
                List<JsonAST> cacheableValues = biValues.get(true);

                if (!cacheableValues.isEmpty()) {
                    compileToStaticInitializer.compileJsonArray(cacheableValues, cacheableValues.size(), null, false);

                    field = cache(bridge.jsonArrayIntermediateClass());
                    values = biValues.get(false);
                }
            }

            compileJsonArray(values, values.size(), field, true);

            return null;
        }


        @Override
        public Void visitString(JASTString that) {
            // When string.fragments is single
            if (that.visit(new CheckCacheable()) && bridge.cacheString()) {
                compileToStaticInitializer.visitString(that);

                cacheAndGet(bridge.jsonClass());
                return null;
            }

            return super.visitString(that);
        }

        @Override
        public Void visitNumber(JASTNumberString that) {
            // TODO cache
            return super.visitNumber(that);
        }

        @Override
        public Void visitNumber(JASTNumberNumber that) {
            // TODO cache
            return super.visitNumber(that);
        }

        @Override
        public Void visitTrue(JASTTrue that) {
            // TODO cache
            return super.visitTrue(that);
        }

        @Override
        public Void visitFalse(JASTFalse that) {
            // TODO cache
            return super.visitFalse(that);
        }

        @Override
        public Void visitNull(JASTNull that) {
            // TODO cache
            return super.visitNull(that);
        }
    }

    class CompileVisitor implements JsonVisitor<Void> {
        protected final JsonCompilerV2Context context;
        protected final CodeBuilder code;

        public CompileVisitor(JsonCompilerV2Context context, CodeBuilder code) {
            this.context = context;
            this.code = code;
        }

        @Override
        public Void visitObject(JASTObject that) {
            compileJsonObject(that.fields(), that.fields().size(), null, true);
            return null;
        }

        public void compileJsonObject(List<Entry<JsonAST, JsonAST>> fields, int size, String field, boolean finish) {
            bridge.compileCreateJsonObjectIntermediate(context, code, size, field);
            fields.forEach(e -> {
                code.dup();
                compileJsonObjectKey(e.getKey());
                e.getValue().visit(this);
                bridge.compilePutToJsonObjectIntermediate(context, code);
            });

            if (finish) {
                bridge.compileFinishJsonObjectIntermediate(context, code);
            }
        }

        private void compileJsonObjectKey(JsonAST that) {
            switch (that) {
                case JASTString str -> compileStringTemplate(str);
                case JASTJavaObject obj -> {
                    code.aload(obj.pos().index())
                            .checkcast(CD_String); // TODO Check CharSequence?
                    // TODO
                }
                default -> throw new IllegalStateException("Unexpected Json element in JsonObject key");
            }
        }

        private void compileStringTemplate(JASTString that) {
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
        public Void visitArray(JASTArray that) {
            compileJsonArray(that.values(), that.values().size(), null, true);

            return null;
        }

        public void compileJsonArray(List<JsonAST> values, int size, String field, boolean finish) {
            bridge.compileCreateJsonArrayIntermediate(context, code, size, field);
            values.forEach(v -> {
                code.dup();
                v.visit(this);
                bridge.compilePutToJsonArrayIntermediate(context, code);
            });

            if (finish) {
                bridge.compileFinishJsonArrayIntermediate(context, code);
            }
        }

        @Override
        public Void visitString(JASTString that) {
            if (that.fragments().size() == 1) {
                code.ldc(context.classBuilder.constantPool().stringEntry(that.fragments().get(0)));
            } else {
                compileStringTemplate(that);
            }

            bridge.compileString(context, code);
            return null;
        }

        @Override
        public Void visitNumber(JASTNumberString that) {
            bridge.compileNumber(context, code, that.number());
            return null;
        }

        @Override
        public Void visitNumber(JASTNumberNumber that) {
            bridge.compileNumber(context, code, that.number());
            return null;
        }

        @Override
        public Void visitTrue(JASTTrue that) {
            bridge.compileTrue(context, code);
            return null;
        }

        @Override
        public Void visitFalse(JASTFalse that) {
            bridge.compileFalse(context, code);
            return null;
        }

        @Override
        public Void visitNull(JASTNull that) {
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
    class CheckCacheable implements JsonVisitor<Boolean> {

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
