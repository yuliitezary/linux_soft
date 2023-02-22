#!/bin/sh

# История одного buildnumber
echo -n $(($(cat buildnumber | cut -d ',' -f 1)+1)), $(date +'%d.%m.%Y') > buildnumber.txt
mv buildnumber.txt buildnumber

# Build Launcher.jar
echo Building Launcher.jar...
jar -uf Launcher.jar buildnumber
java -jar build/proguard.jar @Launcher.pro
rm Launcher.jar
mv Launcher-obf.jar Launcher.jar
# java -jar build/stringer.jar -configFile Launcher.stringer Launcher.jar Launcher.jar
pack200 -E9 -Htrue -mlatest -Upass -r Launcher.jar
jarsigner -keystore build/keeperjerry.jks -storepass PSP1448 -sigfile LAUNCHER Launcher.jar keeperjerry
pack200 Launcher.pack.gz Launcher.jar

# Build LaunchServer.jar
echo Building LaunchServer.jar...
jar -uf LaunchServer.jar Launcher.pack.gz buildnumber
pack200 -E9 -Htrue -mlatest -Upass -r LaunchServer.jar
jarsigner -keystore build/keeperjerry.jks -storepass PSP1448 -sigfile LAUNCHER LaunchServer.jar keeperjerry
rm Launcher.pack.gz