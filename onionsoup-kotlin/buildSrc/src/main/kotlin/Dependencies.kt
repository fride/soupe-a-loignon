object Version {
  const val spek = "2.0.9"
  const val arrow = "0.10.5"
  const val flyway = "6.4.3"
  const val jacksonKotlin = "2.11.0"
  const val jasypt = "1.9.3"
  const val java = "1.8"
  const val jaxb = "2.3.1"
  const val jjwt = "0.9.1"
  const val kotlin = "1.4.0"
  const val ktlint = "0.36.0"
  const val ktlintPlugin = "9.2.1"
  const val restAssured = "4.3.0"
  const val kotestVersion = "4.2.0.RC1"
  const val springBoot = "2.3.0.RELEASE"
  const val versionsPlugin = "0.28.0"
  const val slf4j = "1.7.30"
  const val micrometer = "1.5.4"
  const val dokka = "1.4.0-rc"
  const val config4k = "0.4.2"
}

object Libs {
  const val arrowFx = "io.arrow-kt:arrow-fx:${Version.arrow}"
  const val arrowMtl = "io.arrow-kt:arrow-mtl:${Version.arrow}"
  const val arrowSyntax = "io.arrow-kt:arrow-syntax:${Version.arrow}"

  const val jacksonKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jacksonKotlin}"
  const val jasypt = "org.jasypt:jasypt:${Version.jasypt}"
  const val jaxb = "javax.xml.bind:jaxb-api:${Version.jaxb}"
  const val jjwt = "io.jsonwebtoken:jjwt:${Version.jjwt}"
  const val jsonSchemaValidator = "io.rest-assured:json-schema-validator:${Version.restAssured}"
  const val junitJupiter = "org.junit.jupiter:junit-jupiter"
  const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Version.kotlin}"
  const val kotlinStd = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.kotlin}"
  const val postgresql = "org.postgresql:postgresql"
  const val restassured = "io.rest-assured:rest-assured:${Version.restAssured}"
  const val slf4j = "org.slf4j:slf4j-api:${Version.slf4j}"
  const val micrometer = "io.micrometer:micrometer-core:${Version.micrometer}"
}

object Starters {
  const val actuator = "org.springframework.boot:spring-boot-starter-actuator"
  const val jdbc = "org.springframework.boot:spring-boot-starter-jdbc"
  const val test = "org.springframework.boot:spring-boot-starter-test"
  const val undertow = "org.springframework.boot:spring-boot-starter-undertow"
  const val validation = "org.springframework.boot:spring-boot-starter-validation"
  const val web = "org.springframework.boot:spring-boot-starter-web"
  const val jooq = "org.springframework.boot:spring-boot-starter-jooq"
}

// now this is madness!
//https://stackoverflow.com/questions/58669082/unresolved-reference-implementation-by-using-subprojects-in-kotlin-gradle
const val implementation = "implementation"
const val testImplementation = "testImplementation"
const val runtime = "runtime"
const val api = "api"
const val kapt = "kapt"
