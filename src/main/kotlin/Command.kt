package net.reincarnatey

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter
import com.fasterxml.jackson.databind.ObjectMapper
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.isNotConsole
import net.mamoe.mirai.utils.error
import net.reincarnatey.NLPHelper.formatFileName
import org.ktorm.database.asIterable
import java.io.File
import java.sql.SQLException


object Command: CompositeCommand(
    NLPHelper, "NLPHelper", "nlph", description = "NLPH相关命令"
) {
    @SubCommand("addGroup")
    suspend fun addGroup(group: Long) {
        Config.group.add(group)
    }

    @SubCommand("removeGroup")
    suspend fun removeGroup(group: Long) {
        Config.group.remove(group)
    }

    @SubCommand("addFilter")
    suspend fun addFilter(filter: String) {
        Config.filter.add(filter)
    }

    @SubCommand("removeFilter")
    suspend fun removeFilter(filter: String) {
        Config.filter.remove(filter)
    }

    @SubCommand("addConfig")
    suspend fun addConfig(name: String, sql: String, type: String = "json") {
        Config.config[name] = ExportConfig(sql, type)
    }

    @SubCommand("removeConfig")
    suspend fun removeConfig(name: String) {
        Config.config.remove(name)
    }

    @SubCommand("defaultFileName")
    suspend fun defaultFileName(fileName: String) {
        Config.defaultFileName = fileName
    }

    @SubCommand("outDir")
    suspend fun outDir(path: String) {
        Config.outDir = path
    }

    @SubCommand("query")
    @Description("查询当前已保存数据的数量")
    suspend fun CommandSender.query(sql: String = """SELECT * FROM NLPH;""") {
        if (isNotConsole()){
            return
        }
        NLPHelper.db.useConnection { conn ->
            try {
                sendMessage("共查询到 " + conn.prepareStatement(sql).executeQuery().asIterable().count().toString() + " 条数据。")
            } catch (e: SQLException) {
                NLPHelper.logger.error { e.toString() }
            }
        }
    }

    @SubCommand("execute")
    @Description("执行SQL指令")
    suspend fun CommandSender.execute(sql: String) {
        if (isNotConsole()){
            return
        }
        NLPHelper.db.useConnection { conn ->
            try {
                sendMessage("执行成功，共 " + conn.prepareStatement(sql).executeQuery().asIterable().count().toString() + " 条数据受到影响。")
            } catch (e: SQLException) {
                NLPHelper.logger.error { e.toString() }
            }
        }
    }

    @SubCommand("exportBySQL")
    @Description("导出数据")
    suspend fun CommandSender.exportBySQL(sql: String = """SELECT * FROM NLPH;""", type: String = "json", outDir: String = Config.outDir, fileName: String = Config.defaultFileName) {
        if (isNotConsole()){
            return
        }
        NLPHelper.db.useConnection { conn ->
            try {
                var count = 0
                val result = conn.prepareStatement(sql).executeQuery().let {
                    when(type){
                        "gpt2" -> {
                            val list = mutableListOf<String>()
                            while (it.next()) {
                                it.getString("content")?.let { ct ->
                                    list.add(ct)
                                }
                                count++
                            }
                            list
                        }
                        "json" -> {
                            val list = mutableListOf<Map<String, String>>()
                            while (it.next()){
                                val map = mutableMapOf<String, String>()
                                for (i in 1 .. it.metaData.columnCount){
                                    it.metaData.getColumnLabel(i).let { label ->
                                        map[label] = it.getObject(label).toString()
                                    }
                                }
                                list.add(map)
                                count++
                            }
                            list
                        }
                        else -> {
                            listOf<String>()
                        }
                    }
                }
                File(outDir).let {
                    if (!it.exists()){
                        it.mkdirs()
                    }
                }
                if ("json" == type){
                    val indenter: Indenter = DefaultIndenter("    ", DefaultIndenter.SYS_LF)
                    val printer = DefaultPrettyPrinter()
                    printer.indentObjectsWith(indenter) // Indent JSON objects
                    printer.indentArraysWith(indenter) // Indent JSON arrays
                    ObjectMapper().writer(printer).writeValue(File(outDir + formatFileName(fileName, type, count)), result)
                } else {
                    ObjectMapper().writeValue(File(outDir + formatFileName(fileName, type, count)), result)
                }
                sendMessage("导出成功，共导出 $count 条数据，已保存至 ${outDir+formatFileName(fileName, type, count)}。")
            } catch (e: SQLException) {
                NLPHelper.logger.error { e.toString() }
            }
        }
    }

    @SubCommand("export", "exportByConfig")
    @Description("导出数据")
    suspend fun CommandSender.exportByConfig(config: String = "default", outDir: String = Config.outDir, fileName: String = Config.defaultFileName) {
        if (isNotConsole()){
            return
        }
        Config.config[config].let {
            if (it == null){
                exportBySQL(outDir = outDir, fileName = fileName)
            } else {
                exportBySQL(sql = it.sql, type = it.type, outDir = outDir, fileName = fileName)
            }
        }
    }
}