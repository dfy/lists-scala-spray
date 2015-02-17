package com.example

import akka.actor.{ Actor, ActorRef, ActorLogging, Props }
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

object UrlList {
	case class Url(url: String)
	case class CreateList(uuid:String, title:String)
	case class AddUrlToList(uuid: String, url: String)
	case class ViewList(uuid: String)
}

trait UrlList { this: Actor =>
	import UrlList._

	val urls = ListBuffer[Url]()

	def urlListReceive: Receive = {
		case AddUrlToList(_, url) =>
			urls += Url(url)
		case ViewList(_) =>
			sender ! urls.toList
	}
}

class UrlListManager extends Actor with ActorLogging with UrlList {

	def receive = urlListReceive
}

trait UrlListLookup { this: Actor =>
	import UrlList._

	val lists = Map[String, ActorRef]()
	
	def urlListLookupReceive: Receive = {
		case CreateList(uuid, title) =>
			val newList = context.actorOf(Props[UrlListManager], s"url-list-${uuid}")
			lists += (uuid -> newList)
		case addUrl: AddUrlToList =>
			lists.getOrElse(addUrl.uuid, {throw new Exception("Cannot find list for adding url")}) forward addUrl
		case viewList: ViewList =>
			lists.getOrElse(viewList.uuid, {throw new Exception("Cannot find list for viewing")}) forward viewList
	}
}

class UrlListLookupManager extends Actor with UrlListLookup {
	def receive = urlListLookupReceive
}

/**
 *  CreateList(uuid, title)
 *  AddUrlToList(uuid, url)
 */