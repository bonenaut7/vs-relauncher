# vs-relauncher
Mini-app for VintageStory with a bit of QoL features.

# What it can do?
Not really a lot of things. It can send system notifications about queue progress in game, when you join/leave to and from the game and queue, ping you if someone mentioned you in game chat.
I'll add log file backups a bit later(it's already in-built in the game, but it saves up to whopping 5 log files saves, but I want to backup EVERYTHING).
That's it.

# How to use?
Download one of the [release builds](https://github.com/bonenaut7/vs-relauncher/releases) or make a build yourself (but there's no fat-jar plugins in buildscript ha-ha-ha *\*evil-laughter\**)
Oh yeah, also install yourself a [Java](https://openjdk.org/).

# What's about run arguments?
Well, there's quite a bit of them:
- `--cfg <path>` specifies configuration file path.
- `--gamepid <pid>` specifies running game process ID to watch it's lifetime, so the mini-app will shutdown along with the game. You also can specify `-1` as a process ID to run it without an running game (spoiler, it will still read logs and work).
- `--nogui` runs mini-app without gui in background.
