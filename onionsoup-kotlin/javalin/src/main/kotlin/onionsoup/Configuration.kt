package onionsoup

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.config4k.extract
import org.jooq.Configuration
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration

data class HikariDataSourceConfig(
  val uri: String,
  val username: String,
  val password: String,
  val dialect: SQLDialect,
  val props: Map<String, String>) {

  fun jooqConfiguration(): Configuration =
    datasource().let {
      val config = DefaultConfiguration()
      config.setSQLDialect(dialect)
      config.setDataSource(datasource())
      config
    }

  private fun datasource(): HikariDataSource = HikariConfig().let {
    it.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres")
    it.setUsername("postgres")
    it.setPassword("postgres")
    it.addDataSourceProperty("cachePrepStmts", "true")
    it.addDataSourceProperty("prepStmtCacheSize", "250")
    it.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    return HikariDataSource(it)
  }
}

data class Configuration(
  val datasourceConfig: HikariDataSourceConfig) {
  companion object {
    operator fun invoke() : onionsoup.Configuration {
      val config = ConfigFactory.load("application")
      val hikariConfig = config.extract<HikariDataSourceConfig>("data-source")
      return Configuration(hikariConfig)
    }
  }
}
