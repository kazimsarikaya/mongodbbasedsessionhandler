MongoDB Based Session Management implementation.
The session information is stored inside mongodb for 15 minutes. Before the 
session information expires, if a new connection performed, the due time is 
postphoned. The 15 minutes value is hard coded.

The maven repository is:

<repository>
        <id>sanaldiyar-snapshot</id>
        <name>Sanal Diyar Snapshot</name>
        <url>http://maven2.sanaldiyar.com/snap-repo/</url>
</repository>

Kazım SARIKAYA
kazimsarikaya@sanaldiyar.com
