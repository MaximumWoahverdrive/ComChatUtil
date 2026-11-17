> **Moved to https://git.foe.rip/SWU/ComChatUtil** – this GitHub repository is archived and no longer updated.

# Com Chat Utils
This is a Minecraft Mod to solve the confusion of (not) being in community or staff chat, now developed primarily for survivewith.us (https://survivewith.us/), which tells the mod which chat you are in through the server integration below. It colors the text in your chat field green (or whatever color you choose) while you are in community chat, and red while you are in staff chat.

It was originally made for The OG SMP (http://theogsmp.com/) and Back2Basics (http://back2basics.gg/); on servers without the integration it still works by guessing the community chat state from the commands you type.

## Server Integration
By default the mod guesses whether you're in community chat from the commands you type. A server can instead tell it, by sending a plugin message on the channel `comchatutil:chat_state` (only clients with the mod register the channel, so vanilla clients get nothing):

| Byte | Meaning |
|------|---------|
| 0 | Version, `1` |
| 1 | Chats the server reports: bit 0 community chat, bit 1 staff chat |
| 2 | Chats that are on, same bits |

Send it whenever the state changes, and once the client registers the channel. Chats that aren't reported are left to the mod's guessing (staff chat is then shown as off). Clients skip any further bytes, so fields can be appended without a new version.

## Reporting Bugs
Found a bug? A crash log is mentioning this mod? Managed to desync the community chat state?
Great! Kinda.

Either raise an issue on this repo, or let me know on discord, you will find me on the OG SMP/b2b Discord.

When reporting a bug, try and reproduce it first, there is little I can do without that.

When reporting a crash, make sure to append the crash report.

Managed to desync, and the mod is adamant you are/not in community chat? Relog.
After that, try and reproduce it, then report it as a bug.

## Notes
This mod is currently published nowhere except this repo, if you find it somewhere else - dont trust it. If the publishing state changes, I will amend that here.
