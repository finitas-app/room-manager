package pl.finitas.data.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.LoggerFactory
import pl.finitas.data.model.Room

private val logger = LoggerFactory.getLogger("ConfigureMongoDb")

private val connectionString = run {
    val profile = System.getProperty("profile")
    logger.info("Profile for mongodb: $profile")
    when (profile) {
        "docker" -> "mongodb://room-manager-mongo-db:27017"
        "kub" -> "mongodb://rm-db-service:27017"
        else -> "mongodb://localhost:27018"
    }
}


val mongoClient = KMongo.createClient(
    settings = MongoClientSettings
        .builder()
        .applyConnectionString(
            ConnectionString(
                connectionString
            )
        )
        .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
        .build()
).coroutine

val mongoDatabase = mongoClient.getDatabase("room_database")

val roomCollection = mongoDatabase.getCollection<Room>()
