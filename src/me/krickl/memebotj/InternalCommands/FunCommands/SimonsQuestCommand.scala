package me.krickl.memebotj.InternalCommands.FunCommands

/**
  * Created by unlink on 18/11/15.
  */
class SimonsQuestCommand(channel: String, command: String = "null", dbprefix: String = "") extends me.krickl.memebotj.CommandHandler(channel, command, dbprefix) {
  this.setUserCooldownLen(60)
  this.setCmdtype("list")
  this.setEnable(false)

  this.listContent.add("What a horrible night to have a curse")
  this.listContent.add("And so the shiver of the night has arrived")
  this.listContent.add("The morning sun has vanquished the horrible night")
  this.listContent.add("The nightmarish night has ended")
  this.listContent.add("You now prossess Dracula's Rib")
  this.listContent.add("Let's live here together")
  this.listContent.add("Take my daughter, please!!")
  this.listContent.add("Step into the shadows of the Hell House. You've arrived back here at Transylvania on business: To destroy forever the curse of the evil Count Dracula.")
  this.listContent.add("Get a Silk Bag from the Graveyard Duck to live longer")


  override def commandScript(sender: me.krickl.memebotj.UserHandler, channelHandler: me.krickl.memebotj.ChannelHandler, data: Array[String]) = {
  }
}
