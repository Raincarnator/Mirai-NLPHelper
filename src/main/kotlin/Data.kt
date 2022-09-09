package net.reincarnatey

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate

interface Message: Entity<Message> {
    companion object : Entity.Factory<Message>()
    var sender: Long
    var group: Long
    var bot: Long
    var content: String
    var size: Int
    var date: LocalDate
}

object NLPH : Table<Message>("NLPH") {
    val sender = long("sender").bindTo { it.sender }
    val group = long("group").bindTo { it.group }
    val bot = long("bot").bindTo { it.bot }
    val content = text("content").bindTo { it.content }
    val size = int("size").bindTo { it.size }
    val date = date("date").bindTo { it.date }
}

val Database.nlph get() = this.sequenceOf(NLPH)