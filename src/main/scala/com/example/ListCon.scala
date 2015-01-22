package com.example

import akka.actor.{ Actor, ActorLogging }
import scala.collection.mutable.ListBuffer

object UrlList {
	case class Url(url: String)
	case class AddUrlToList(uuid: String, url: String)
}

trait UrlList { this: Actor =>
	import UrlList._

	val urls = ListBuffer[Url]()

	def urlListReceive: Receive = {
		case AddUrlToList(_, url) =>
			urls += Url(url)
	}
}

class UrlListManager extends Actor with ActorLogging with UrlList {

	def receive = urlListReceive
}

/**
 *  CreateList(uuid, title)
 *  AddUrlToList(uuid, url)
 */