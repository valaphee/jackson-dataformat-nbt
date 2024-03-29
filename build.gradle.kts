/*
 * Copyright (c) 2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.palantir.git-version") version "0.12.3"
    kotlin("jvm") version "1.7.22"
    `maven-publish`
    signing
}

group = "com.valaphee"
val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
version = "${details.lastTag}.${details.commitDistance}"

repositories { mavenCentral() }

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

tasks { withType<Test> { useJUnitPlatform() } }

java {
    withJavadocJar()
    withSourcesJar()
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom.apply {
                name.set("Jackson Dataformat: NBT")
                description.set("Support for reading and writing NBT-encoded data via Jackson abstractions.")
                url.set("https://valaphee.com")
                scm {
                    connection.set("https://github.com/valaphee/jackson-dataformat-nbt.git")
                    developerConnection.set("https://github.com/valaphee/jackson-dataformat-nbt.git")
                    url.set("https://github.com/valaphee/jackson-dataformat-nbt")
                }
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://raw.githubusercontent.com/valaphee/jackson-dataformat-nbt/master/LICENSE.txt")
                    }
                }
                developers {
                    developer {
                        id.set("valaphee")
                        name.set("Valaphee")
                        email.set("iam@valaphee.com")
                        roles.add("owner")
                    }
                }
            }

            from(components["java"])
        }
    }
}
