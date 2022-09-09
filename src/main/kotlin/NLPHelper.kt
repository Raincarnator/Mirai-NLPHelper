package net.reincarnatey

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info
import org.ktorm.database.Database
import org.ktorm.entity.add
import java.io.File
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object NLPHelper : KotlinPlugin(
    JvmPluginDescription(
        id = "net.reincarnatey.NLPHelper",
        name = "NLPHelper",
        version = "1.0",
    ) {
        author("Mitr-yuzr")
        info("""记录群群聊数据、简单过滤并导出NLP模型训练语料的插件。""")
    }
) {
    val db = Database.connect("jdbc:sqlite:file:${getDBFile()}")

    override fun onEnable() {
        Config.reload()
        CommandManager.registerCommand(Command)

        createTable()

        globalEventChannel().filterIsInstance<GroupMessageEvent>().filter { it.group.id in Config.group }.subscribeAlways<GroupMessageEvent> {
            var filter = true
            Config.filter.forEach {
                if (Regex(it).matches(message.content)){
                    filter = false
                }
            }
            if (filter) {
                val sb = StringBuilder()
                message.filterIsInstance<PlainText>().forEach {
                    sb.append(filterEmoji(it.content))
                }
                sb.toString().let {
                    if (it.isNotBlank() && it.isNotEmpty()){
                        db.nlph.add(Message {
                            sender = this@subscribeAlways.sender.id
                            group = this@subscribeAlways.group.id
                            bot = this@subscribeAlways.bot.id
                            content = it
                            size = it.length
                            date = LocalDate.now()
                        })
                    }
                }
            }
        }

        logger.info { "NLPHelper准备就绪! 已连接至${db.name}!" }
    }

    override fun onDisable() {
        CommandManager.unregisterCommand(Command)

        logger.info { "TimerRequester已卸载!" }
    }

    private fun filterEmoji(str: String): String{
        return str.replace(Regex("""(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)"""), "")
    }

    private fun getDBFile(): File {
        Class.forName("org.sqlite.JDBC")
        File(Config.dbDir).let {
            if (!it.exists()){
                it.mkdirs()
            }
        }
        return File(Config.dbDir + "NLPH.db")
    }

    private fun createTable() {
        db.useConnection {
            try {
                it.createStatement().execute(
                    """
CREATE TABLE IF NOT EXISTS NLPH (
  sender BIGINT not null,
  "group" BIGINT not null,
  bot BIGINT not null,
  content TEXT not null,
  size INT not null,
  "date" DATE not null
);             
                    """.trimIndent()
                )
            } catch (e: SQLException) {
                logger.error { e.toString() }
            }
        }
    }

    fun formatFileName(name: String, model: String, count: Int): String {
        return name
            .replace("\\", "")
            .replace("/", "")
            .replace(":", "")
            .replace("*", "")
            .replace("?", "")
            .replace("\"", "")
            .replace("<", "")
            .replace(">", "")
            .replace("|", "")
            .run {
                if (isEmpty() || isBlank()){
                    return@run this + "NLPHExport_{model}_{count}_{datetime}.json"
                } else {
                    return@run this
                }
            }.replace("{model}", model)
            .replace("{count}", count.toString())
            .replace("{date}", DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now()))
            .replace("{time}", DateTimeFormatter.ofPattern("HHmm").format(LocalTime.now()))
            .replace("{datetime}", DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(LocalDateTime.now()))
    }
}