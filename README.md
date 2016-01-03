
#Memebot is a simple, yet powerful irc bot designed specifically for use with twitch.tv.

#Compilation

To compile memebot you will need the following libraries:

- bson
- mongodb-driver
- mongodb
- json-simple

##Basic commands:

Most of the commands have an attached helptext and do not need further expalnation. Just use !help <command>.

##!command add <command> <output>

This can be used to add a new command

##!command remove <command>

This can be used to delete a command

##!command edit <command> <option> <value>

Command edit has many options available:

####name
description: changes the name of a command
possible values: any string of text

####param
description: changes the amount of parameters a command has
possible values: positive integer

####helptext
description: The description of a command
possible values: any string of text

####output
description: a commands output
possible values: any string of text

####cooldown
description: global cooldown affecting every user of the command
possible values: positive integer

####cmdtype
description: the type of command
possible values: default, list, counter, timer

####qprefix
description: Prefix string for quotes
possible values: any string of text

####qsuffix
description: suffix for quotes
possible values: any string of text

####cost
description: points cost of a command
possible values: any integer

####lock (broadcaster only)
description: Lock a command so only the broadcaster can delete it
possible values: true/false

####texttrigger
description: make a command a texttrigger (note: this disables parameters)
possible values: true/false

####access
description: the needed command power level for a command
possible values: any integer

####usercooldown
description: individual cooldown per user
possible values: positive integer

####enable
description: enable or disable a command
possible values: true/false

####allowpick
description: allow picking an index from a list
possible values: true/false

####case
description: make a command case sensitive
possible values: true/false

##!user <option>

!user can be used to edit user settings

###mod only options:

####modalias <username> <new value>
description: changes the nickname of a user
possible value: any string of text without space

####modremovealias <username>
description: removes the alias of a user

