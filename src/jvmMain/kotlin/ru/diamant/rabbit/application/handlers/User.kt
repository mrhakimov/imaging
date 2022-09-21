package ru.diamant.rabbit.application.handlers

import at.favre.lib.crypto.bcrypt.BCrypt
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import ru.diamant.rabbit.application.db.Config
import ru.diamant.rabbit.application.db.models.User
import ru.diamant.rabbit.common.model.LoginStatus
import ru.diamant.rabbit.common.model.RegisterStatus
import ru.diamant.rabbit.common.model.UserCredentials

suspend fun hasSuchUser(login: String): Boolean {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase(Config.DATABASE_NAME)
    val usersCol = database.getCollection<User>()

    return usersCol.findOne(User::login eq login) != null
}

suspend fun login(userCredentials: UserCredentials): LoginStatus {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase(Config.DATABASE_NAME)
    val usersCol = database.getCollection<User>()

    val user = usersCol.findOne(User::login eq userCredentials.login) ?: return LoginStatus.NO_SUCH_USER
    val verified = BCrypt.verifyer().verify(userCredentials.password.toCharArray(), user.passwordHash).verified

    return if (verified) {
        LoginStatus.SUCCESS
    } else {
        LoginStatus.INCORRECT_PASSWORD
    }
}

suspend fun register(userCredentials: UserCredentials): RegisterStatus {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase(Config.DATABASE_NAME)
    val usersCol = database.getCollection<User>()

    val user = usersCol.findOne(User::login eq userCredentials.login)
    if (user != null) {
        return RegisterStatus.ALREADY_REGISTERED
    }

    val passwordHash = BCrypt.withDefaults().hashToString(12, userCredentials.password.toCharArray())
    usersCol.insertOne(User(userCredentials.login, passwordHash)) // not storing password itself, only hash

    return RegisterStatus.SUCCESS
}
