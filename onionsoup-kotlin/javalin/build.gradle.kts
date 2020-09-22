plugins {
  application
  `java-library`
}
dependencies {
  implementation(project(":core"))
  implementation(project(":persistence"))

  // thouse should come with :persistence?
  implementation("org.jooq", "jooq", "3.13.2") // why ???
  implementation(group = "com.zaxxer", name = "HikariCP", version = "3.4.5")

  implementation("io.javalin:javalin:3.9.1")
  implementation("io.pebbletemplates:pebble:3.1.2") // templates
  implementation("org.slf4j:slf4j-simple:1.8.0-beta4")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.10.3")
  implementation("io.micrometer:micrometer-core:1.5.4")

  implementation("io.github.config4k:config4k:${Version.config4k}")

  implementation("com.auth0:java-jwt:3.10.3")


  testImplementation("org.seleniumhq.selenium:htmlunit-driver:2.43.1")
  testImplementation("io.github.bonigarcia:webdrivermanager:3.6.2")

}

application {
  mainClass.set("onionsoup.MainKt")
}
