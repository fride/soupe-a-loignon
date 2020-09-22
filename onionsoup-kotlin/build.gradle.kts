import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
  id("org.jetbrains.kotlin.jvm") version Version.kotlin apply false
  id("org.jlleitschuh.gradle.ktlint") version Version.ktlintPlugin apply false
  id("org.flywaydb.flyway") version Version.flyway apply false
  id("dev.bombinating.jooq-codegen") version "1.7.0" apply false
  id("org.springframework.boot") version Version.springBoot apply false
  kotlin("kapt") version Version.kotlin apply false
  id("org.jetbrains.dokka") version Version.dokka apply false
}
repositories {
  mavenCentral()
  mavenLocal()
  jcenter()
  maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
  maven(url = "https://dl.bintray.com/konform-kt/konform")
}

configure(subprojects.apply {}) {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "org.jetbrains.kotlin.kapt")
//  apply(plugin = "org.jlleitschuh.gradle.ktlint") -- macht nur ärger!
  apply(plugin = "org.jetbrains.dokka")

  repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven(url = "https://dl.bintray.com/konform-kt/konform")
  }

  configurations {
    all {
      resolutionStrategy.eachDependency {
        if (requested.group == "org.slf4") {
          useVersion(Version.slf4j)
          because("use single slf4j version")
        }
        if (requested.group == "io.micrometer") {
          useVersion(Version.micrometer)
          because("use single micrometer version")
        }
        if (requested.group == "org.jetbrains.kotlin") {
          useVersion(Version.kotlin)
          because("use single kotlin version")
        }
      }
    }
  }

  tasks.withType<DokkaTask> {
    dokkaSourceSets {
      register("main") {
//        moduleDisplayName = "Dokka Gradle Example"
//        includes = listOf("Module.md")
        sourceLink {
          path = "src/main/kotlin"
          url = "https://github.com/fride/onoipnsoup/tree/master/" +
            "gradle/dokka/dokka-gradle-example/src/main/kotlin"
          lineSuffix = "#L"
        }
      }
    }
  }

//  configure<KtlintExtension> {
//    version.set(Version.ktlint)
//    verbose.set(true)
//    outputToConsole.set(true)
//    coloredOutput.set(true)
//    reporters {
//      reporter(ReporterType.CHECKSTYLE)
//      reporter(ReporterType.JSON)
//      reporter(ReporterType.HTML)
//    }
//    filter {
//      exclude("**/generated/**")
//      exclude("**/build.gradle.kts")
//      include("**/kotlin/**")
//    }
//  }

  tasks.withType<JavaCompile> {
    sourceCompatibility = "8"
    targetCompatibility = "8"
    options.encoding = "UTF-8"
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs = listOf(
        "-Xjsr305=strict",
        "-XXLanguage:+InlineClasses"
      )
    }
  }
  tasks.withType<Test> {
    useJUnitPlatform()
    outputs.upToDateWhen { false }

    testLogging {
      events("passed", "failed", "skipped")
      exceptionFormat = TestExceptionFormat.FULL
    }
  }
  dependencies {
    implementation.let {
      it(platform("org.jetbrains.kotlin:kotlin-bom"))
      it("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
      it("org.jetbrains.kotlin:kotlin-reflect")
      it("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")

      // arrow
      it("io.arrow-kt:arrow-fx:${Version.arrow}")
      it("io.arrow-kt:arrow-fx-mtl:${Version.arrow}")
      it("io.arrow-kt:arrow-mtl-data:${Version.arrow}")
      it("io.arrow-kt:arrow-core-data:${Version.arrow}")
      it("io.arrow-kt:arrow-syntax:${Version.arrow}")
    }
    testImplementation.let {
      it("org.jetbrains.kotlin:kotlin-test")
      it("org.jetbrains.kotlin:kotlin-test-junit")
      it("io.kotest:kotest-runner-junit5-jvm:${Version.kotestVersion}") // for kotest framework
      it("io.kotest:kotest-assertions-core-jvm:${Version.kotestVersion}") // for kotest core jvm assertions
      it("io.kotest:kotest-property-jvm:${Version.kotestVersion}") // for kotest property test
      it("io.kotest:kotest-assertions-arrow:${Version.kotestVersion}") // for kotest arrow test
    }
    kapt.let {
      it("io.arrow-kt:arrow-meta:${Version.arrow}")
    }
  }
}

project(":core") {
  apply(plugin = "org.gradle.java-library")
  dependencies {
    api.let {
      it(Libs.slf4j)
    }
  }
}

project(":springboot") {
  apply(plugin = "org.flywaydb.flyway")

  dependencies {
    implementation.let {
      it(project(":core"))
      it(project(":persistence"))
      it(platform("org.springframework.boot:spring-boot-dependencies:${Version.springBoot}"))
      it(Starters.actuator)
      it(Starters.jooq)
      it(Starters.jdbc)
      it(Starters.validation)
      it(Starters.web) {
        exclude(
          group = "org.springframework.boot",
          module = "spring-boot-starter-tomcat"
        )
      }
      implementation("org.jooq", "jooq", "3.13.2") // why ???
      implementation(group = "com.zaxxer", name = "HikariCP", version = "3.4.5")
      it(Starters.undertow)

      it(Libs.jacksonKotlin)
    }

    runtime(Libs.postgresql)

    testImplementation.let {
      it(Libs.jsonSchemaValidator)
      it(Libs.restassured)
    }
  }
}
