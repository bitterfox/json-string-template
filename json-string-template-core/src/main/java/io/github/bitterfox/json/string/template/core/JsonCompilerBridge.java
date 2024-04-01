package io.github.bitterfox.json.string.template.core;

import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.CD_Map;
import static java.lang.constant.ConstantDescs.CD_Object;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;

public interface JsonCompilerBridge<T> {
    default ClassDesc jsonClass() {
        return CD_Object;
    }

    default ClassDesc jsonObjectIntermediateClass() {
        return CD_Map;
    }

    default ClassDesc jsonArrayIntermediateClass() {
        return CD_List;
    }


    // before: ...
    // after: ..., JsonObjectIntermediate
    /**
     *
     * @param context
     * @param size size of fields in JsonObject
     * @param field static field for initial key-values. if it's null, no initial key-values
     */
    void compileCreateJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code, int size, String field);
    // before: ..., JsonObjectIntermediate, key, value
    // after: ...
    void compilePutToJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code);
    // before: ..., JsonObjectIntermediate
    // after: ..., JsonObject
    void compileFinishJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code);

    boolean cacheJsonObjectIntermediate();
    boolean isJsonObjectImmutable();

    // before: ...
    // after: ..., JsonArrayIntermediate
    /**
     *
     * @param context
     * @param size size of fields in JsonObject
     * @param field static field for initial key-values. if it's null, no initial key-values
     */
    void compileCreateJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code, int size, String field);
    // before: ..., JsonArrayIntermediate, value
    // after: ...
    void compilePutToJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code);
    // before: ..., JsonArrayIntermediate
    // after: ..., JsonArray
    void compileFinishJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code);

    boolean cacheJsonArrayIntermediate();
    boolean isJsonArrayImmutable();

    // before: ..., String
    // after: ..., JsonString
    default void compileString(JsonCompilerV2Context context, CodeBuilder code) {
//        if (fragments.size() == 1) {
//            // Constant
//            code.ldc(context.classBuilder.constantPool().stringEntry(fragments.get(0)));
//        } else {
//            // TODO use string template STR
//            Iterator<String> iterator = fragments.iterator();
//            String first = iterator.next();
//            ClassDesc stringBuilder = ClassDesc.of("java.lang.StringBuilder");
//            code.new_(stringBuilder)
//                .dup()
//                .ldc(context.classBuilder.constantPool().stringEntry(first))
//                .invokespecial(stringBuilder, INIT_NAME, MethodTypeDesc.of(CD_void, CD_String));
//
//            int i = start;
//            while (iterator.hasNext()) {
//                String string = iterator.next();
//                code.aload(i)
//                    .invokevirtual(stringBuilder, "append", MethodTypeDesc.of(stringBuilder, CD_Object))
//                    .ldc(string)
//                    .invokevirtual(stringBuilder, "append", MethodTypeDesc.of(stringBuilder, CD_String));
//            }
//
//            code.invokevirtual(CD_Object, "toString", MethodTypeDesc.of(CD_String));
//        }
    }
    boolean isStringImmutable();
    boolean cacheString();

    default void compileNumber(JsonCompilerV2Context context, CodeBuilder code, String number) {

    }
    default void compileNumber(JsonCompilerV2Context context, CodeBuilder code, Number number) {

    }
    boolean isNumberImmutable();

    default void compileTrue(JsonCompilerV2Context context, CodeBuilder code) {

    }
    boolean isTrueImmutable();
    default void compileFalse(JsonCompilerV2Context context, CodeBuilder code) {

    }
    boolean isFalseImmutable();
    default void compileNull(JsonCompilerV2Context context, CodeBuilder code) {

    }
    boolean isNullImmutable();

    void compileJavaObject(JsonCompilerV2Context context, CodeBuilder processCode, int argPos);
}
