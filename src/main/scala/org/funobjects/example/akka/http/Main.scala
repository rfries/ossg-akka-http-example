/*
 * Copyright 2015 Functional Objects, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.funobjects.example.akka.http

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.server.Directives._
import akka.http.model._
import akka.stream.{FlowMaterializer, ActorFlowMaterializer}
import com.typesafe.config.{ConfigFactory, Config}

import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal

// Simple nested case class to demonstrate unmarshalling
case class Outer(a: String, b: Option[String], inner: Inner, maybe: Option[Inner] )
case class Inner(n: Int, m: Option[Int])


object Main extends App with Server {

  val akkaConfig: Config = ConfigFactory.parseString("""
      akka.loglevel = INFO
      akka.log-dead-letters = off""")

  // many operations require some or all of these implicits
  override implicit val sys = ActorSystem("akka-http-example", akkaConfig)
  override implicit val flows = ActorFlowMaterializer()
  override implicit val exec = sys.dispatcher

  val serverBinding = Http(sys).bind(interface = "localhost", port = 3434).startHandlingWith(router)
}

trait Server {
  implicit val sys: ActorSystem
  implicit val flows: FlowMaterializer
  implicit val exec: ExecutionContext

  // default json4s extractors
  implicit val formats = org.json4s.DefaultFormats

  val router = {
    logRequestResult("akka-http-example") {
      // simple response
      path("shutdown") {
        complete {
          sys.scheduler.scheduleOnce(1.second) { sys.shutdown() }
          HttpResponse(StatusCodes.OK)
        }
      } ~
        // combination of directives, extraction of entity w/ json4s
        (post & path("outer") & extract(_.request.entity)) { entity =>
          complete {
            entity.toStrict(1.second) map { strict =>
              Try {
                val outer = parse(strict.data.utf8String).extract[Outer]
                println(outer)
                HttpResponse(StatusCodes.OK)
              } recover {
                case NonFatal(ex) => HttpResponse(StatusCodes.InternalServerError, entity = s"Non-optimal execution: $ex\n")
              }
            }
          }
        } ~
        // form extraction
        path("form") {
          formFields('a, "b", "c".?) { (a, b, c) =>
            complete {
              println(s"form: $a $b $c")
              HttpResponse(StatusCodes.OK)
            }
          }
        } ~
        // query string and path extraction
        path("entity" / JavaUUID) { uuid =>
          parameters("type", "opt".?) { (typeParm, optParm) =>
            println(s"entity: $uuid, type = $typeParm, opt = $optParm")
            complete(HttpResponse(StatusCodes.OK))
          }
        } ~
        // composition, streaming
        pathPrefix("stream") { Streamer.routes }
    }
  }
}
