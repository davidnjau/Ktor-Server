package com.example.Routes

import com.example.API_VERSION
import com.example.auth.JwtService
import com.example.auth.MySession
import com.example.helperClass.isValid
import com.example.repository.Repository
import com.example.response.RegisterUser
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set

const val USERS = "$API_VERSION/users"

const val USER_LOGIN = "$USERS/login"
const val USER_LOGOUT = "$USERS/logout"
const val USER_CREATE = "$USERS/create"
const val USER_DELETE = "$USERS/delete"

@KtorExperimentalLocationsAPI
@Location(USER_LOGIN)
class UserLoginRoute

@KtorExperimentalLocationsAPI
@Location(USER_LOGOUT)
class UserLogoutRoute

@KtorExperimentalLocationsAPI
@Location(USER_CREATE)
class UserCreateRoute

@KtorExperimentalLocationsAPI
@Location(USER_DELETE)
class UserDeleteRoute

@KtorExperimentalLocationsAPI
fun Route.users(db: Repository, jwtService: JwtService, hashFunction: (String) -> String) {
    //User login to db
    post<UserLoginRoute> {

        val signinParameters = call.receive<Parameters>()
        val password = signinParameters["password"] ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")
        val email = signinParameters["email"] ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")

        val hash = hashFunction(password)

        if (email != "" && password != ""){

            val isValid = isValid().emailChecker(email)

            if (isValid){

                try {
                    val currentUser = db.findUserByEmail(email)
                    if (currentUser == null){

                        val code = HttpStatusCode.NotFound

                        call.respond(
                            RegisterUser(code, "User is not registered.")
                        )

                    }else{

                        currentUser?.userId?.let {

                            if (currentUser.passwordHash == hash) {

                                call.sessions.set(MySession(it))
//                            call.respondText(jwtService.generateToken(currentUser))

                                val jwtAuth = jwtService.generateToken(currentUser)
                                val code = HttpStatusCode.OK

                                call.respond(
                                    RegisterUser(code, jwtAuth)
                                )

                            } else {

                                val code = HttpStatusCode.BadRequest
                                call.respond(
                                    RegisterUser(code, "Problems retrieving User")
                                )

                            }

                        }
                    }

                } catch (e: Throwable) {

                    val code = HttpStatusCode.BadRequest

                    call.respond(
                        RegisterUser(code, "Problems retrieving User")
                    )

                }

            }else{

                val code = HttpStatusCode.BadRequest

                call.respond(
                    RegisterUser(code, "Email format is invalid.")
                )

            }


        }else{

            var message : String = ""

            if (email == "")message = "$message email is required."
            if (password == "")message = "$message password is required."

            val code = HttpStatusCode.BadRequest

            call.respond(
                RegisterUser(code, message)
            )

        }


    }

    //User Logout from system
    post<UserLogoutRoute> {
        val signinParameters = call.receive<Parameters>()
        val email = signinParameters["email"] ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")

        try {
            val currentUser = db.findUserByEmail(email)
            currentUser?.userId?.let {
                call.sessions.clear(call.sessions.findName(MySession::class))
                call.respond(HttpStatusCode.OK)
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
        }
    }

    //Delete user from Database
    delete<UserDeleteRoute> {
        val signinParameters = call.receive<Parameters>()

        val email = signinParameters["email"] ?: return@delete call.respond(HttpStatusCode.Unauthorized, "Missing Fields")
        val password = signinParameters["password"] ?: return@delete call.respond(HttpStatusCode.Unauthorized, "Missing Fields")

        val hash = hashFunction(password)

        if (email != "" && password != ""){

            val currentUser = db.findUserByEmail(email)

            if (currentUser != null){

                try {
                    currentUser?.userId?.let {

                        if (currentUser.passwordHash == hash){

                            db.deleteUser(it)
                            call.sessions.clear(call.sessions.findName(MySession::class))
//                            call.respond(HttpStatusCode.OK)

                            val code = HttpStatusCode.OK

                            call.respond(
                                RegisterUser(code, "User deleted successfully.")
                            )

                        }else{

                            val code = HttpStatusCode.Unauthorized

                            call.respond(
                                RegisterUser(code, "You cannot delete the user.")
                            )


                        }

                    }
                } catch (e: Throwable) {

                    call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
                }

            }else{

                val code = HttpStatusCode.NotFound

                call.respond(
                    RegisterUser(code, "User is not registered.")
                )

            }



        }else{

            var message : String = ""

            if (email == "")message = "$message email is required."
            if (password == "")message = "$message password is required."

            val code = HttpStatusCode.BadRequest

            call.respond(
                RegisterUser(code, message)
            )
        }


    }

    //Register user into the system
    post<UserCreateRoute> {

        val signupParameters = call.receive<Parameters>()

        val password = signupParameters["password"] ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")
        val displayName = signupParameters["displayName"] ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")
        val email = signupParameters["email"] ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Fields")

        if (email != "" && displayName != "" && password != ""){

            val isValid = isValid().emailChecker(email)
            if (isValid){

                val hash = hashFunction(password)

                try {

                    //Creates new user
                    val newUser = db.addUser(email, displayName, hash)
                    newUser?.userId?.let {
                        call.sessions.set(MySession(it))

                        val jwtAuth = jwtService.generateToken(newUser)
                        val code = HttpStatusCode.Created
                        call.respond(
                            RegisterUser(code, jwtAuth)
                        )


                    }
                } catch (e: Throwable) {

                    val code = HttpStatusCode.BadRequest

                    call.respond(
                        RegisterUser(code,"Problems registering User")
                    )
                }

            }else{

                val code = HttpStatusCode.BadRequest

                call.respond(
                    RegisterUser(code, "Email format is invalid.")
                )

            }

        }else{

            var message : String = ""

            if (email == "")message = "$message email is required."
            if (displayName == "")message = "$message displayName is required."
            if (password == "")message = "$message password is required."

            val code = HttpStatusCode.BadRequest


            call.respond(
                RegisterUser(code, message)
            )

        }


    }

}