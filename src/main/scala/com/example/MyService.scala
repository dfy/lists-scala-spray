package com.example

import scala.concurrent.duration._

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import spray.routing._
import spray.http._
import MediaTypes._

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{ read, write }
import spray.httpx.Json4sSupport

/* Used to mix in Spray's Marshalling Support with json4s */
object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  import Json4sProtocol._
  import UrlList._

  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)
  
  case class Video(url: String)
  case class OtherThing(url:String, count:Integer)

  val lookupId = "xxx001"
  val lookup = actorRefFactory.actorOf(Props[UrlListLookupManager], "list-lookup")
  lookup ! CreateList(lookupId, "First list")
  lookup ! AddUrlToList(lookupId, "http://www.google.co.uk")
  lookup ! AddUrlToList(lookupId, "http://www.bbc.co.uk")

  val myRoute =
    path("") {
      get {
        complete {
          List()
        }
      }
    } ~
    path ("entity" / Segment / "child" / Segment) { (id, childId) =>
      get {
        complete(
          List(
            Video(s"detail ${id}, ${childId}"), 
            OtherThing("Some random stuff", 10), 
            List("a", "x")
            )
          )
      }
    } ~
    path ("list" / Segment) { (listId) =>
      get {
        complete {
          lookup
            .ask(ViewList(listId))
            .mapTo[List[String]]
        }
      }
    }
}
