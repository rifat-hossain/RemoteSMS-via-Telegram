# RemoteSMS via Telegram
Remotely get SMSs from another android phone through Telegram. Follow `Getting Started` section to proceed.

## Getting Started
### Creating your bot
Follow the instructions from [here](https://core.telegram.org/bots#3-how-do-i-create-a-bot) and get a bot token.

### Using the bot token
- Download the source in local machine and open with your preferred IDE (suggested to use Android Studio).
- Open `<Project Directory>/RemoteSMS-via-Telegram/app/src/main/res/values/strings.xml` and replace sample bot token with the token of your bot

Example:
```xml
<!--Original-->
<string name="bot_token">xxxxxxxxxx:xxxxxxxx-xxxxxxxxxxxxxxxxxxxxxxxxxx</string>

<!--Replaced-->
<string name="bot_token">110201543:AAHdqTcvCH1vGWJxfSeofSAs0K5PALDsaw</string>
```

### Finishing Up
Build apk and run it in your target android phone. Follow app instructions to get SMSs remotely.
