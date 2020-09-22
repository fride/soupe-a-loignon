import dev.bombinating.gradle.jooq.JooqTask
import dev.bombinating.gradle.jooq.database
import dev.bombinating.gradle.jooq.generator
import dev.bombinating.gradle.jooq.jdbc
import dev.bombinating.gradle.jooq.target

val genDir = "$projectDir/build/generated/src/main/java"
val dbUrl: String by project
val dbUsername: String by project
val dbPassword: String by project

plugins {
  java
  id("org.flywaydb.flyway")
  id("dev.bombinating.jooq-codegen")
}

dependencies {
  api(project(":core"))
  api(group = "org.postgresql", name = "postgresql", version = "42.2.6")
  api(group = "com.zaxxer", name = "HikariCP", version = "3.4.5")
  api("org.jooq", "jooq", "3.13.2")
  jooqRuntime(group = "org.postgresql", name = "postgresql", version = "42.2.6")
  implementation("org.apache.commons:commons-lang3:3.11")
  implementation(Libs.jacksonKotlin)
  implementation(Libs.micrometer)
}

flyway {
  url = dbUrl
  user = dbUsername
  password = dbPassword
}

sourceSets["main"].java {
  srcDir(genDir)
}

tasks.register<JooqTask>("onionsoup") {
  jdbc {
    url = dbUrl
    username = dbUsername
    password = dbPassword
  }
  generator {
    database {
      excludes = "flyway_schema_history"
      includes = "public.*"
    }
    target {
      directory = genDir
      packageName = "onionsoup.loanapplication.persistence.sql"
    }
  }
}

task<Delete>("cleanGenerated") {
  delete(genDir)
}

tasks.getByName("clean").dependsOn(tasks.getByName("cleanGenerated"))
tasks.getByName("onionsoup").dependsOn(tasks.getByName("flywayMigrate"))
tasks.getByName("compileKotlin").dependsOn(tasks.getByName("onionsoup"))

