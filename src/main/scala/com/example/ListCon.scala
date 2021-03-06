package com.example

import akka.actor.{ Actor, ActorRef, ActorLogging, Props }
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

object UrlList {
	case class Url(url: String)

	trait UrlListCommand {
		val uuid: String
	}
	case class CreateList(uuid:String, title:String) extends UrlListCommand
	case class AddUrlToList(uuid: String, url: String) extends UrlListCommand
	case class ViewList(uuid: String) extends UrlListCommand
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
		case listCommand: UrlListCommand =>
			lists.getOrElse(listCommand.uuid, {throwListNotFound(listCommand)}) forward listCommand
	}

	def throwListNotFound(listCommand: UrlListCommand) = {
		throw new Exception(s"Cannot find list '${listCommand.uuid}'")
	}
}

class UrlListLookupManager extends Actor with UrlListLookup {
	def receive = urlListLookupReceive
}

/**
 *  CreateList(uuid, title)
 *  AddUrlToList(uuid, url)
 */