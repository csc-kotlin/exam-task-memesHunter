package ru.diamant.rabbit.application

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import ru.diamant.rabbit.application.plugins.configureRouting
import ru.diamant.rabbit.application.plugins.configureSecurity
import ru.diamant.rabbit.application.plugins.configureSerialization
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.eq

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        configureRouting()
        configureSecurity()
        configureSerialization()
    }.start(wait = true)
}

// TODO remove this method, it is an example of work with MongoDB
// TODO the example is taken from https://litote.org/kmongo/quick-start/
// TODO you can check if you install everything correctly by calling this method
private fun databaseExample() {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase("test")
    val col = database.getCollection<Jedi>()

    runBlocking {
        col.insertOne(Jedi("Luke Skywalker", 19))

        val yoda : Jedi? = col.findOne(Jedi::name eq "Yoda")
        val luke : Jedi? = col.findOne(Jedi::name eq "Luke Skywalker")

        println(yoda)
        println(luke)
    }
}

// TODO remove it, it is made as an example only
data class Jedi(val name: String, val age: Int)