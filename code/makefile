%.class: *.java
	javac *.java -d "out" && cp manifest.txt out

jar: $(wildcard out/*.class)
	cd out && jar cfm QuickFPS.jar manifest.txt *.class && cd../