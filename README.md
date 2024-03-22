# json-string-template

[![Static Badge](https://img.shields.io/badge/Java-21-blue)](https://jdk.java.net/21/)
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.bitterfox/json-string-template-core)](https://central.sonatype.com/artifact/io.github.bitterfox/json-string-template-core)
[![GitHub License](https://img.shields.io/github/license/bitterfox/json-string-template)](https://www.apache.org/licenses/LICENSE-2.0)
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/bitterfox/json-string-template/gradle.yml)](https://github.com/bitterfox/json-string-template/actions/workflows/gradle.yml)

Java 21 introduced the [String Template](https://openjdk.org/jeps/430) feature as a new preview feature.
This feature allows users to create String literals with embedded expressions.
It also enables developers to define their own processor with a conversion logic to any Java object.

The JEP discusses [the possibility of String Template for JSON](https://openjdk.org/jeps/430#The-template-processor-API), as shown below:

> ```java
> var JSON = StringTemplate.Processor.of(
>         (StringTemplate st) -> new JSONObject(st.interpolate())
>     );
>
> String name    = "Joan Smith";
> String phone   = "555-123-4567";
> String address = "1 Maple Drive, Anytown";
> JSONObject doc = JSON."""
>     {
>         "name":    "\{name}",
>         "phone":   "\{phone}",
>         "address": "\{address}"
>     };
>     """;
> ```
> Users of this hypothetical JSON processor never see the String produced by st.interpolate(). However, using st.interpolate() in this way risks propagating injection vulnerabilities into the JSON result. We can be prudent and revise the code to check the template's values first and throw a checked exception, JSONException, if a value is suspicious:

As the JEP mentions, there is an issue with the above expression when the embedded expression contains symbols that need to be escaped, such as `"`.

However, no existing JSON library has been compatible with String Template because the parser does not expect a String to terminate in the middle of JSON and cannot handle embedded expressions. This library implements a safe JSON String Template with a parser that can handle embedded expressions.

Additionally, the core library, including the parser, does not depend on other JSON libraries. This independence allows it to support any JSON library for JSON String Template functionality.

Currently, the following JSON libraries are integrated with this JSON String Template extension:

- [Jakarta JSON Processing™ API](https://projects.eclipse.org/projects/ee4j.jsonp)
  - [io.github.bitterfox:json-string-template-jakarta-json:0.21.4](https://central.sonatype.com/artifact/io.github.bitterfox/json-string-template-jakarta-json)
- [JSON in Java a.k.a. org.json](https://github.com/stleary/JSON-java)
  - [io.github.bitterfox:json-string-template-org-json:0.21.4](https://central.sonatype.com/artifact/io.github.bitterfox/json-string-template-org-json)
- [Jackson](https://github.com/FasterXML/jackson)
  - [io.github.bitterfox:json-string-template-jackson:0.21.4](https://central.sonatype.com/artifact/io.github.bitterfox/json-string-template-jackson)

## Disclaimer
Until String Template is released and this library becomes version 1.x.x, any part of feature in this library
might be changed.
Also, this is not yet battle tested in the real world, use this library at your own risks and
please give me a feedback!

# How to use JSON String Template

First, add one of the bridge libraries mentioned above to your project.

```kotlin
// build.gradle.kts

dependencies {
  implementation("io.github.bitterfox:json-string-template-jakarta-json:0.21.4")
  implementation("io.github.bitterfox:json-string-template-org-json:0.21.4")
  implementation("io.github.bitterfox:json-string-template-jackson:0.21.4")
}
```

Also, ensure your project uses Java 21 and that the preview feature is enabled.

```kotlin
// build.gradle.kts

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.toVersion("21")
  targetCompatibility = JavaVersion.toVersion("21")
}

tasks.compileJava {
  options.compilerArgs.add("--enable-preview")
}

tasks.compileTestJava {
  options.compilerArgs.add("--enable-preview")
}

tasks.test {
  useJUnitPlatform()
  jvmArgs("--enable-preview")
}

// If you use application plugin
application {
  applicationDefaultJvmArgs = listOf("--enable-preview")
}
```

Now you are ready to use JSON String Template!
Import the JSON String Template Processor static field and try it out.

```java
import static io.github.bitterfox.json.string.template.jakarta.json.JsonStringTemplate.JSON;

import jakarta.json.Json;
import jakarta.json.JsonValue;

class Main {
  public static void main(String[] args) {
    String value = "te\nst";
    JsonValue json =
            JSON."""
            {
                "test": \{value}
            }
            """;
    // This is equivalent to
    // Json.createObjectBuilder()
    //     .add("test", value)
    //     .build()
    // For example, the Json content will be {"test": "te\nst"}
  }
}
```

Note that if there is a character that needs to be escaped, it will be handled correctly.
Characters that need to be escaped will still be escaped in the JSON object.

You can also use embedded expressions in the JSON string, or even use them as keys in the JSON object.

```java
String key = "message";
String name = "Duke";
JsonValue json =
        JSON."""
        {
            \{key}: "Hello, my name is \{name}!"
        }
        """;


// This is equivalent to
// Json.createObjectBuilder()
//     .add(key, STR."Hello, my name is \{name}!")
//     .build()
// For example, the Json content will be {"message": "Hello, my name is Duke!"}
```

# Object Mapping

To write JSON String Template more naturally in Java code,
This library supports the conversion of Java objects to JSON values and
write JSON objects in JSON values.

## Conversion for Java values

This library supports converting the following Java values to JSON values:

- Numerical (subtypes of `Number` or auto-boxed values) are converted to JSON numbers:
  - `Integer`
  - `Long`
  - `Double`
  - `BigInteger`
  - `BigDecimal`
  - (Other Number subtypes might not be supported and could cause exceptions)
- Boolean values
  - `true`
  - `false`
- Java array to JSON Array
- `Collection` to JSON Array
- `null`
- JSON Objects of underlying JSON library
- Other objects will be converted to JSON Strings using `Object.toString()`

To illustrate the above, let's look at some examples.
```java
String name = "Java";
int age = 28;
LocalDate dateOfBirth = LocalDate.of(1996, 1, 23);
List<String> supportedVersions = List.of("22", "21", "17");

JsonValue father =
        JSON."""
        {
            "name": "James Arthur Gosling"
        }
        """;

JsonValue json =
        JSON."""
        {
            "name": \{name},
            "age": \{age},
            "dateOfBirth": \{dateOfBirth},
            "supportedVersions": \{supportedVersions},
            "father": \{father}
        }
        """;

// Above equals to
// Json.createObjectBuilder()
//         .add("name", "Java")
//         .add("age", 28)
//         // LocalDate is converted to JSON String using Object.toString
//         // It's recommended to use DateTimeFormatter for such purpose
//         .add("dateOfBirth", "1996-01-23")
//         .add("supportedVersions", Json.createArrayBuilder().add("22").add("21").add("17"))
//         // Note that father is a JSON object of underlying JSON library, Jakarta JSON Processing API
//         // So it's embedded as a value as-is without any conversion
//         .add("father", Json.createObjectBuilder()
//                 .add("name", "James Arthur Gosling"))
//        .build();
```

## Integration with ObjectMapper in Jackson
If you use Jackson with this library, you can leverage the powerful conversion capabilities of Jackson's ObjectMapper.
For example:

```java
record Profile(
        int id,
        String name,
        String status) {}

Profile profile = new Profile(101, "Duke", "Hello world!");
String token = "xxxx";

JsonNode json = JSON."""
                {
                    "token": \{token},
                    "body": \{profile}
                }
                """;
// This is equivalent to
// {
//     "token": "xxxx",
//     "body": {
//         "id": 101,
//         "name": "Duke",
//         "status": "Hello world!"
//     }
// }
```

These conversion features are useful for writing ad-hoc JSON with Java objects.
