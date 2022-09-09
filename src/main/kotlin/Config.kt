package net.reincarnatey

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("NLPHConfig") {
    @ValueDescription("要开启记录消息的群")
    var group: MutableList<Long> by value()

    @ValueDescription("当检测到以下内容时不记录该消息，支持正则表达式")
    var filter by value(mutableListOf("/", "#", "http"))

    @ValueDescription("sql查询语句的配置，数据表详细与简单的sql教程在README.md")
    var config by value(mutableMapOf(
        "default" to ExportConfig("""SELECT * FROM NLPH;""", "json"),
        "json" to ExportConfig("""SELECT * FROM NLPH;""", "json"),
        "gpt2" to ExportConfig("""SELECT * FROM NLPH;""", "gpt2")
    ))

    @ValueDescription("默认导出数据的位置")
    var outDir by value("./NLPH/out/")

    @ValueDescription("存放数据库的位置，更改后记得移动NLPH.db至新的位置，重启生效")
    var dbDir by value("./NLPH/data/")

    @ValueDescription("默认文件名，模板参数请读README.md")
    var defaultFileName by value("NLPHExport_{model}_{count}.json")
}

@Serializable
data class ExportConfig(
    var sql: String,
    var type: String
)