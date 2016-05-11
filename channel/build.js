mkdir("bin")
mkdir("doc");

javac("src", "bin")
copy("res", "bin");
javadoc("src", "doc");

mkdir("dist");
var name = "remote4j-channel";
jar("dist/" + name + ".jar", "bin");
jar("dist/" + name + "-source.jar", "src");
jar("dist/" + name + "-javadoc.jar", "doc");
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")
