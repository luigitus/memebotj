package me.krickl.memebotj

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.logging.Logger
import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import com.mongodb.Block
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import me.krickl.memebotj.InternalCommands.APIInformationCommand
import me.krickl.memebotj.InternalCommands.AboutCommand
import me.krickl.memebotj.InternalCommands.AddCommandHandler
import me.krickl.memebotj.InternalCommands.AliasCommand
import me.krickl.memebotj.InternalCommands.AutogreetCommand
import me.krickl.memebotj.InternalCommands.ChannelInfoCommand
import me.krickl.memebotj.InternalCommands.CommandList
import me.krickl.memebotj.InternalCommands.CommandManager
import me.krickl.memebotj.InternalCommands.DampeCommand
import me.krickl.memebotj.InternalCommands.DebugCommand
import me.krickl.memebotj.InternalCommands.DeletCommandHandler
import me.krickl.memebotj.InternalCommands.EditChannel
import me.krickl.memebotj.InternalCommands.EditCommand
import me.krickl.memebotj.InternalCommands.EditUserCommand
import me.krickl.memebotj.InternalCommands.FilenameCommand
import me.krickl.memebotj.InternalCommands.GiveAwayPollCommand
import me.krickl.memebotj.InternalCommands.HelpCommand
import me.krickl.memebotj.InternalCommands.HugCommand
import me.krickl.memebotj.InternalCommands.HypeCommand
import me.krickl.memebotj.InternalCommands.JoinCommand
import me.krickl.memebotj.InternalCommands.ModeratorsCommand
import me.krickl.memebotj.InternalCommands.MujuruGame
import me.krickl.memebotj.InternalCommands.PartCommand
import me.krickl.memebotj.InternalCommands.PointsCommand
import me.krickl.memebotj.InternalCommands.PyramidCommand
import me.krickl.memebotj.InternalCommands.QuitCommand
import me.krickl.memebotj.InternalCommands.RaceCommand
import me.krickl.memebotj.InternalCommands.SaveCommand
import me.krickl.memebotj.InternalCommands.SendMessageCommand
import me.krickl.memebotj.InternalCommands.SpeedrunCommand
import me.krickl.memebotj.InternalCommands.UptimeCommand
import me.krickl.memebotj.InternalCommands.WhoisCommand

/** *
  * This class is used to slowly convert ChannelHandler to scala
  * once ChannelHandler is fully converted this class will replace the old ChannelHandler
  * @param channelNew
  * @param connectionNew
  */
class ChannelHandlerScala(channelNew: String, connectionNew: ConnectionHandler) {
  private val log: Logger = Logger.getLogger(classOf[ChannelHandler].getName)
  private var channel: String = null
  private var connection: ConnectionHandler = null
  private var broadcaster: String = null
  private var userList: HashMap[String, UserHandler] = new HashMap[String, UserHandler]
  private var updateCooldown: Cooldown = new Cooldown(60)
  private var channelCommands: ArrayList[CommandHandler] = new ArrayList[CommandHandler]
  private var internalCommands: ArrayList[CommandHandler] = new ArrayList[CommandHandler]
  private var followerNotification: String = ""
  private var channelInfoURL: String = ""
  private var channelFollowersURL: String = ""
  private var raceBaseURL: String = "http://kadgar.net/live"
  private var greetMessage: String = "Hello I'm {appname} {version} build {build} the dankest irc bot ever RitzMitz"
  private var currentRaceURL: String = ""
  private var fileNameList: ArrayList[String] = new ArrayList[String]
  private var aliasList: ArrayList[String] = new ArrayList[String]
  private var maxFileNameLen: Int = 8
  private var currentFileName: String = ""
  private var streamStartTime: Int = 0
  private var builtInStrings: HashMap[String, String] = new HashMap[String, String]
  private var channelPageURL: String = null
  private var channelPageBaseURL: String = null
  private var htmlDir: String = null
  private var youtubeAPIURL: String = "https://www.googleapis.com/youtube/v3/videos?id={videoid}&part=contentDetails&key=" + Memebot.youtubeAPIKey
  private var otherLoadedChannels: ArrayList[String] = new ArrayList[String]
  private var autogreetList: HashMap[String, String] = new HashMap[String, String]
  private var channelCollection: MongoCollection[Document] = null
  private var pointsPerUpdate: Double = 1.0f
  private var t: Thread = null
  private var isJoined: Boolean = true
  private var allowAutogreet: Boolean = true
  private var isLive: Boolean = false
  private var currentMessageCount: Int = 0
  private var messageLimitCooldown: Cooldown = new Cooldown(30)
  private var preventMessageCooldown: Cooldown = new Cooldown(30)
  private var currentGame: String = "Not Playing"
  private var privateKey: String = ""
  private var random: SecureRandom = new SecureRandom
  private var apiConnectionIP: String = ""
  private var urlRegex: String = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
  private var purgeURLS: Boolean = false
  private var purgeURLSNewUsers: Boolean = false
  private var linkTimeout: Int = 1


}
