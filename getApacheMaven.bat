powershell -Command "Invoke-WebRequest https://archive.apache.org/dist/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.zip -OutFile apacheMaven.zip"
powershell -Command "Expand-Archive apacheMaven.zip -DestinationPath apacheMaven"
echo "Set proxy in \apache-maven\conf\settings.xml <proxies>"
pause
