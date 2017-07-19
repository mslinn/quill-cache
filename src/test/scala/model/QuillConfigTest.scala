package model

import java.util.Properties
import com.typesafe.config.{Config, ConfigFactory}
import com.zaxxer.hikari.HikariDataSource
import io.getquill.JdbcContextConfig

class QuillConfigTest extends TestSpec {
  "ClassName" should {
    "correct" in {
      val x = model.dao.Users.className
      x shouldBe "User"
    }
  }

  "HikariCP" should {
    "be configured properly" in {
      val config: Config = ConfigFactory.load.getConfig("quill-cache.postgres")
      assert(config.entrySet.size==12)

      val jdbcContextConfig = JdbcContextConfig(config)
      val dataSource: HikariDataSource = jdbcContextConfig.dataSource
      val properties: Properties = dataSource.getDataSourceProperties
      assert(properties.keySet.size==7)
    }
  }
}
