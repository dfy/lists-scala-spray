package com.example

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import org.scalatest.MustMatchers

class TestUrlList extends Actor with UrlList {
    def receive = urlListReceive
}

class UrlListSpec extends TestKit(ActorSystem("UrlListSpec"))
    with WordSpecLike
    with MustMatchers
    with BeforeAndAfterAll {

    import UrlList._

    override def afterAll() { system.shutdown() }

    "UrlList" should {
        "add a url to a list" in {
            val real = TestActorRef[TestUrlList].underlyingActor
            real.receive(AddUrlToList("xxx", "http://www.google.com"))
            real.urls must contain (Url("http://www.google.com"))
            // first actor creates a urllist actor
            // this one just handles messages to the urllist
        }
    }
}