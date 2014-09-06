$ErrorActionPreference = 'Stop'

$logs = 'T:\Temp\Logs'
$databasePath = 'D:\Vagrant\vagrant-horta-hell\'
$databaseFile = 'hell.h2.db'

if (Test-Path $logs) {
	Remove-Item -Recurse $logs
}

New-Item -ItemType Directory $logs
Push-Location $logs
try {
	wget -nv -m -np `
	  http://0xd34df00d.me/logs/chat/codingteam@conference.jabber.ru/
} finally {
	Pop-Location
}

Copy-Item -Force $databasePath\$databaseFile .\$databaseFile

sbt "run `"$logs`""

Copy-Item -Force .\$databaseFile $databasePath\$databaseFile