package com.example

import org.scalatest._
import spray.testkit.ScalatestRouteTest
import spray.http.HttpEntity
import spray.http.ContentTypes
import spray.can.server.Stats
import spray.http.StatusCodes._
import org.json4s._

class MainSpec extends FreeSpec with Matchers with ScalatestRouteTest with MyService {
  def actorRefFactory = system

  "The MyService Route" - {
    "when listing entities" - {
      "returns a JSON list" in {
        import Json4sProtocol._
        import UrlList._

        Get("/list/xxx001") ~> myRoute ~> check {
          assert(contentType.mediaType.isApplication)

          // check content type
          contentType.toString should include("application/json")

          // check the sample data
          val response = responseAs[List[Url]]
          response.size should equal(2)
          response(0).url should equal("http://www.google.co.uk")
          response(0).url should equal("http://www.bbc.co.uk")

          //Check http status
          status should equal(OK)
        }
      }
    }
  }
}

