# ------- config -------
SRC_DIR := .
OUT_DIR := out
MAIN_CLASS := Main
JAR := app.jar

JAVA := java
JAVAC := javac

# Find all .java files under SRC_DIR
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# ------- phony targets -------
.PHONY: build run jar clean

build: $(OUT_DIR)/.compiled

$(OUT_DIR)/.compiled: $(SOURCES)
	@mkdir -p $(OUT_DIR)
	$(JAVAC) -d $(OUT_DIR) $(SOURCES)
	@touch $@

run: build
	$(JAVA) -cp $(OUT_DIR) $(MAIN_CLASS)

jar: build
	@echo "Main-Class: $(MAIN_CLASS)" > MANIFEST.MF
	@cd $(OUT_DIR) && jar cfm ../$(JAR) ../MANIFEST.MF .
	@rm -f MANIFEST.MF
	@echo "Built $(JAR)"

clean:
	rm -rf $(OUT_DIR) $(JAR)
