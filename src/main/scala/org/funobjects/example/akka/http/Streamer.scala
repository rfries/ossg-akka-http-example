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
import akka.http.model._
import akka.http.server.Directives._
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext

/**
 * Example of connecting a route to a stream.
 */

object Streamer {

  /**
   * A slow iterator (next() takes 1 second)
   */
  class SlowBoundedIterator(max: Int) extends Iterator[Int] {
    var it = (1 to max).reverseIterator
    override def hasNext: Boolean = it.hasNext
    override def next(): Int = { Thread.sleep(1000); it.next() }
  }

  val slowSrc = Source(() => new SlowBoundedIterator(5)).map(n => HttpEntity.ChunkStreamPart(s"$n\n"))

  def routes(implicit sys: ActorSystem, flows: FlowMaterializer, executionContext: ExecutionContext) = {
    path("slow" ) {
      complete {
        HttpResponse(StatusCodes.OK, entity = HttpEntity.Chunked(ContentTypes.`text/plain`, slowSrc))
      }
    }
  }
}

