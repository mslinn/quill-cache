package model

import java.util.Properties
import com.typesafe.config.{Config, ConfigFactory}
import com.zaxxer.hikari.HikariDataSource
import io.getquill.JdbcContextConfig

class QuillConfigTest extends TestSpec {
  "ClassName" should {
    "correct" in {
      import model.dao._
      UserDAO.className   shouldBe "User"
      BrokenDAO.className shouldBe "Token"
      TokenDAO.className  shouldBe "Token"
    }
  }

  "HikariCP" should {
    "be configured properly" in {
      val config: Config = ConfigFactory.load.getConfig("quill-cache.h2")
      val jdbcContextConfig = JdbcContextConfig(config)
      val dataSource: HikariDataSource = jdbcContextConfig.dataSource
      val properties: Properties = dataSource.getDataSourceProperties
      properties.keySet.size should be >= 1
    }
  }
}
