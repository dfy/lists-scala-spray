package com.example

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import org.scalatest.MustMatchers

class TestUrlList extends Actor with UrlList {
    def receive = urlListReceive
}

class TestUrlListLookup extends Actor with UrlListLookup {
    def receive = urlListLookupReceive
}

class UrlListSpec extends TestKit(ActorSystem("UrlListSpec"))
    with WordSpecLike
    with MustMatchers
    with ImplicitSender
    with BeforeAndAfterAll {

    import UrlList._

    override def afterAll() { system.shutdown() }

    "UrlList" should {

        "add a url to a list" in {
            val real = TestActorRef[TestUrlList].underlyingActor
            real.receive(AddUrlToList("xxx", "http://www.google.com"))
            real.urls must contain (Url("http://www.google.com"))
        }

        "return an immutable list for viewing" in {
        	val listRef = TestActorRef[TestUrlList]
        	listRef ! ViewList("xxx")
        	expectMsg(List[String]())
        }
    }

    "UrlListCreator" should {

    	"create a new list" in {
    		val lookupRef = TestActorRef[TestUrlListLookup]
    		val lookup = lookupRef.underlyingActor

    		lookup.lists must have size (0)

    		lookup.receive(CreateList("xxx", "My New List"))

    		lookup.lists must contain key "xxx"
    		lookup.lists must have size (1)
    	}

    	"forward other messages to the list" in {
    		val lookupRef = TestActorRef[TestUrlListLookup]

    		lookupRef ! CreateList("xxx", "My New List")
    		lookupRef ! AddUrlToList("xxx", "http://www.google.com")
    		lookupRef ! ViewList("xxx")

    		expectMsg(List(Url("http://www.google.com")))
    	}
    }
}