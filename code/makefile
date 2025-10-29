%.class: *.java
	javac *.java -d "out" && copy manifest.txt out

jar: $(wildcard out/*.class)
	cd out && jar cfm QuickFPS.jar manifest.txt *.class && cd../