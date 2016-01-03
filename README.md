
#Memebot is a simple, yet powerful irc bot designed specifically for use with twitch.tv.

#Compilation

To compile memebot you will need the following libraries:

- bson
- mongodb-driver
- mongodb
- json-simple

##Basic commands:

Most of the commands have an attached helptext and do not need further expalnation. Just use !help <command>.

##!command add command output

This can be used to add a new command

##!command remove command

This can be used to delete a command

##!command edit command option value

Command edit has many options available:

####name
description: changes the name of a command<br/>
possible values: any string of text

####param
description: changes the amount of parameters a command has<br/>
possible values: positive integer

####helptext
description: The description of a command<br/>
possible values: any string of text

####output
description: a commands output<br/>
possible values: any string of text

####cooldown
description: global cooldown affecting every user of the command<br/>
possible values: positive integer

####cmdtype
description: the type of command<br/>
possible values: default, list, counter, timer

####qprefix
description: Prefix string for quotes<br/>
possible values: any string of text

####qsuffix
description: suffix for quotes<br/>
possible values: any string of text

####cost
description: points cost of a command<br/>
possible values: any integer

####lock (broadcaster only)
description: Lock a command so only the broadcaster can delete it<br/>
possible values: true/false

####texttrigger
description: make a command a texttrigger (note: this disables parameters)<br/>
possible values: true/false

####access
description: the needed command power level for a command<br/>
possible values: any integer

####usercooldown
description: individual cooldown per user<br/>
possible values: positive integer

####enable
description: enable or disable a command<br/>
possible values: true/false

####allowpick
description: allow picking an index from a list<br/>
possible values: true/false

####case
description: make a command case sensitive<br/>
possible values: true/false

##!user option

!user can be used to edit user settings

###mod only options:

####modalias username new value
description: changes the nickname of a user
possible value: any string of text without space

####modremovealias username
description: removes the alias of a user

##!channel option new value

This command can be used by the broadcaster to edit the channel settings

####race
description: changes the url used by the !race command<br/>
possible values: any string of text

####allowautogreets
description: enables or disables autogreets<br/>
possible values: true/false

####maxnamelen
description: the max filename lenght<br/>
possible values: positive integer

####ppi
description: points per update<br/>
possible values: any integer

####purgelinks
description: should links be purged<br/>
possible values: true/false

####maxpoints
description: max allowed points<br/>
possible values: positive double

####local
description: localisation<br/>
possible values: engb (more soon to come)

####currname
description: currency name<br/>
possible values: any string

####curremote
description: currency emote<br/>
possible values: any string

####followannouncement
description: announcement for followers (work in progress)<br/>
possible values: any string

##Formatting options

####{sender}
description: sender's username or screenname

####{senderusername}
description: sender's username

####{points}
description: sender's points balance

####{counter}
description: command's counter

####{date}
description: current date

####{time}
description: current time (for bot's location)

####{space}
description: a blank space

####{none}
description: {none} is replaced with nothing