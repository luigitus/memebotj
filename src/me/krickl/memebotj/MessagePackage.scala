package me.krickl.memebotj

/**
  * This file is part of memebotj.
  * Created by unlink on 17/02/16.
  */
class MessagePackage(newMessageContent: Array[String], newSender: UserHandler, newMessageType: String) {
  var messageContent: Array[String] = newMessageContent
  var sender: UserHandler = newSender
  var messageType: String = newMessageType
}
