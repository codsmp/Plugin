$url = "https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml"
$response = Invoke-WebRequest -Uri $url -UseBasicParsing
$response.Content | Select-String "26\.1\.2|latest|release"
