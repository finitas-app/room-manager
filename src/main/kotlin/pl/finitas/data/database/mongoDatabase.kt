package pl.finitas.data.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mongoClient = KMongo.createClient(
    settings = MongoClientSettings
        .builder()
        .applyConnectionString(
            ConnectionString(
                "mongodb://room-manager-mongo-db:27017"
            )
        )
        .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
        .build()
).coroutine
val mongoDatabase = mongoClient.getDatabase("room_database")