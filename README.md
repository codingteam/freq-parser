# Parser of HTML logs produced by freqbot #

Why? For the glory of [Horta](https://github.com/codingteam/horta-hell/) of course!

# Usage #

Download the logs using `wget -m` and put them in a folder from where
you're going to execute the converter. Put a prepared h2 database
file in the same folder. The database should contain a `log` table
defined like
[this](https://github.com/codingteam/horta-hell/blob/master/src/main/resources/db/log/V2__Create-log-table.sql). And
finally do `sbt run`. The converter recursively finds all files that
match `\d{2}\.html` and converts them into the database.

You may also pass a path to logs as a first parameter of `sbt run`:

    $ sbt 'run path/to/logs' 

**NOTE:** due to naive implementation of /me messages parsing it is
recommended to convert one conference at a time.
