package model

import java.util.Properties
import com.typesafe.config.{Config, ConfigFactory}
import com.zaxxer.hikari.HikariDataSource
import io.getquill.JdbcContextConfig

class QuillConfigTest extends TestSpec {
  "ClassName" should {
    "correct" in {
      import model.dao._
      Users.className   shouldBe "User"
      Brokens.className shouldBe "Token"
      Tokens.className  shouldBe "Token"
    }
  }

  "HikariCP" should {
    "be configured properly" in {
      val config: Config = ConfigFactory.load.getConfig("quill-cache.postgres")
      config.getInt("maximumPoolSize") shouldBe 100

      val jdbcContextConfig = JdbcContextConfig(config)
      val dataSource: HikariDataSource = jdbcContextConfig.dataSource
      val properties: Properties = dataSource.getDataSourceProperties
      assert(properties.keySet.size==7)
    }
  }
}
