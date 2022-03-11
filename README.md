## Overview

[Jackson](../../FasterXML/jackson) (Java) data format module that supports reading and writing
[NBT](NBT.txt)
("Named Binary Tag") encoded data.
Module extends standard Jackson streaming API (`JsonFactory`, `JsonParser`, `JsonGenerator`), and as such works seamlessly with all the higher level data abstractions (data binding, tree model, and pluggable extensions).

# Dependency

To use this extension on Maven-based projects, use following dependency:

```xml
<dependency>
  <groupId>com.valaphee</groupId>
  <artifactId>jackson-dataformat-nbt</artifactId>
  <version>1.1.8</version>
</dependency>
```

and for Gradle-based projects:

```kotlin
implementation("com.valaphee:jackson-dataformat-nbt:1.1.8")
```

## Usage

Basic usage is by using `NbtFactory` in places where you would usually use `JsonFactory`

```java
ObjectMapper mapper = new ObjectMapper(NbtFactory());
// and then read/write data as usual
SomeType value = ...;
byte[] nbtData = mapper.writeValueAsBytes(value);
SomeType otherValue = mapper.readValue(nbtData, SomeType.class);
```

Implementation allows use of any of 3 main operating modes:

* Streaming API (`NbtParser` and `NbtGenerator`)
* Databinding (via `ObjectMapper` / `ObjectReader` / `ObjectWriter`)
* Tree Model (using `TreeNode`, or its concrete subtype, `JsonNode` -- not JSON-specific despite the name)

and all the usual data-binding use cases exactly like when using `JSON` or `Smile` (2 canonical 100% supported Jackson data formats).
