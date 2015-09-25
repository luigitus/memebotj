
#Memebot is a simple, yet powerful irc bot designed specifically for use with twitch.tv.

#Compilation

To compile memebot you will need the following libraries:

- bson
- mongodb-driver
- mongodb
- json-simple

##Basic commands:

Most of the commands have an attached helptext and do not need further expalnation. Just use !help <command>.

#Edit commands:

#!editchannel:

The editchannel command is a powerful command allowing you to change certain channel settings.

possible values you can change are the following:

- "HELP_NOT_FOUND", "Could not find help for that command"
- "HELP_SYNTAX", "Syntax: {param1}"
- "ADDCOM_SYNTAX", "Syntax: {param1}"
- "CHMOD_SYNTAX", "Usage: {param1}"
- "EDITCOMMAND_OK", "Edited command {param1}. Changed {param2} to {param3}."
- "EDITCOMMAND_FAIL", "Could not edit command"
- "DELCOM_SYNTAX", "Syntax: {param1}"
- "DELCOM_NOT_FOUND", "Could not find command {param1}"
- "DELCOM_OK", "{param1} removed"
- "CHCHANNEL_SYNTAX", "Syntax: {param1}"
- "CURRENCY_NAME", "points"
- "CURRENCY_EMOTE", "points"

- race <url>, Changes the default URL for the !race command
- otherch add/remove <channel>, Adds or removes commands for other channels
- allowautogreet <true/false>, Enable or disable autogreet
- maxnamelen <integer>, change the max length of file names


