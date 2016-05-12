mkdir("bin")
javac("src", "bin")

mkdir("dist");
var name = "remote4j-websocket-mediator";
jar("dist/" + name + ".jar", "bin");

publish("dist")
