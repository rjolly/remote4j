rmiregistry -J-Djava.rmi.server.codebase=file:lib/remote4j-1.2.jar

java -classpath lib/remote4j-1.2.jar:build/classes demo.Bulb bulb343

jrunscript -classpath lib/remote4j-1.2.jar:build/classes
s=new Packages.demo.Switch("bulb343")
s.on()
s.off()

s=new Packages.demo.ConnectableSwitch("switch481")
s.on()

java -classpath lib/remote4j-1.2.jar:build/classes demo.Connect bulb343 switch481

s.on()
s.off()

run("demo.Connect", "classes", "bulb343", "switch481")

java -jar mediator/remote4j-websocket-mediator.jar

java -classpath lib/remote4j-1.2.jar:lib/remote4j-websocket-1.2.jar:lib/javax.websocket-api-1.1.jar:lib/tyrus-client-1.12.jar:lib/tyrus-spi-1.12.jar:lib/tyrus-container-jdk-client-1.12.jar:lib/tyrus-core-1.12.jar:build/classes demo.ws.Bulb bulb343

jrunscript -classpath lib/remote4j-1.2.jar:lib/remote4j-websocket-1.2.jar:lib/javax.websocket-api-1.1.jar:lib/tyrus-client-1.12.jar:lib/tyrus-spi-1.12.jar:lib/tyrus-container-jdk-client-1.12.jar:lib/tyrus-core-1.12.jar:build/classes
s=new Packages.demo.ws.Switch("bulb343")
s.on()
s.off()

s=new Packages.demo.ws.ConnectableSwitch("switch481")
s.on()

java -classpath lib/remote4j-1.2.jar:lib/remote4j-websocket-1.2.jar:lib/javax.websocket-api-1.1.jar:lib/tyrus-client-1.12.jar:lib/tyrus-spi-1.12.jar:lib/tyrus-container-jdk-client-1.12.jar:lib/tyrus-core-1.12.jar:build/classes demo.ws.Connect bulb343 switch481

s.on()
s.off()
