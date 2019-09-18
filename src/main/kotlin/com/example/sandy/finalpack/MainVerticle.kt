package com.example.sandy.finalpack

import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.parse


class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
// Main router to configure all the endpoint calls
    val router = mainRouter()
    vertx
      .createHttpServer()
      .requestHandler (router)
      .listen(8888) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8888")
        } else {
          startPromise.fail(http.cause())
        }
      }

  }

  // Orchestrate the endpoints to the respective handler methods
   fun mainRouter()=Router.router(vertx).apply {
    //POST /person, accepting content-type application/JSON: should parse the JSON body into a Person object.
    // Should use BodyHandler to accept the JSOn request body in the post request
    route().handler(BodyHandler.create()).path("/person")
    post("/person").handler(addPerson)
   // get("/person").handler(firstMethod)
    get("/person").handler(retrivePersons)
    get("/person/:id").handler(fetchById)
    route().failureHandler(this::handleFailure)
  }

  val firstMethod = Handler<RoutingContext> { req ->
    req.response().end("Welcome!")
  }

  val retrivePersons = Handler<RoutingContext> { req ->
    req.response().end(Json.stringify(Person.serializer().list,personlist.sortedBy { it.id }))
  }

  val addPerson = Handler<RoutingContext>{req->
    val successCode = 201
    val exceptionCode= 409
    val errorCode =400
    try {

      val person = Json.parse<Person>(Person.serializer(), req.bodyAsString)
      val idcheck = personlist.map { it }.distinct().filter { it.id == person.id }
      if (idcheck.isNotEmpty())
        req.response().setStatusCode(exceptionCode).end("ID is already Present ")
      else
        personlist.add(person)
      req.response().setStatusCode(successCode).end()
      }
    catch (e: Exception ){
        req.response().setChunked(true).setStatusCode(errorCode).end()
      }

  }

  val personlist = mutableListOf<Person>(
    Person(1, "sherlock"),
    Person(2, "sandy")

)


  //Fetching the
  val fetchById= Handler<RoutingContext>{req->
    val id= req.request().getParam("id")
    val failCode=404
    // Retrun the person object when the id matches or send the sorted list on empty id
    val person= personlist.map{it}.distinct().filter{
      it.id == null && it.id.equals(id)}
      .sortedBy { id }
    // When person is empty then there is no matching record
    if(person.isEmpty())
      req.fail(failCode)
    else
      req.response().end(Json.stringify(Person.serializer().list,person))
    }


  @Serializable
  data class Person(val id :Int, val name:String)
}



