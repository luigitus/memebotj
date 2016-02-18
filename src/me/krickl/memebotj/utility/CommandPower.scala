package me.krickl.memebotj.Utility

/**
  * This class describes the difference in command power
  * Created by unlink on 24/12/15.
  */
object CommandPower {
  final val viewerAbsolute = 10
  final val viewer = viewerAbsolute
  final val modAbsolute = 25
  final val mod = modAbsolute - viewerAbsolute
  final val broadcasterAbsolute = 50
  final val broadcaster = broadcasterAbsolute - viewerAbsolute
  final val adminAbsolute = 75
  final val admin = adminAbsolute - viewerAbsolute
}
