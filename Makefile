# -*- Mode: Makefile -*-
#
# Makefile - Java version
#
# use: make 
# or:  make test
#

JAVA = /usr/bin/java
JAVAC = /usr/bin/javac

all: RouterSimulator.class

RouterSimulator.class: RouterSimulator.java
	-@$(JAVAC) RouterSimulator.java


clean:
	-@touch ./abc~ core
	-@rm -f *~ core 

clobber: clean
	-@touch ./abc.class 
	-@rm -f *.class 

test: RouterSimulator.class
	$(JAVA) -DTrace=3 RouterSimulator

install3DV:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@make clobber
	-@cp test/RouterSimulator3DV.java RouterSimulator.java

install4DV:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@make clobber
	-@cp test/RouterSimulator4DV.java RouterSimulator.java

install5DV:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	
	-@make clobber
	-@cp test/RouterSimulator5DV.java RouterSimulator.java


install3LS:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@make clobber
	-@cp test/RouterSimulator3LS.java RouterSimulator.java

install4LS:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@make clobber
	-@cp test/RouterSimulator4LS.java RouterSimulator.java

install5LS:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@make clobber
	-@cp test/RouterSimulator5LS.java RouterSimulator.java


installRingDV:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	
	-@make clobber
	-@cp test/RouterSimulatorRingDV.java RouterSimulator.java

installRingLS:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@make clobber
	-@cp test/RouterSimulatorRingLS.java RouterSimulator.java

installStarDV:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	
	-@make clobber
	-@cp test/RouterSimulatorStarDV.java RouterSimulator.java

installStarLS:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@make clobber
	-@cp test/RouterSimulatorStarLS.java RouterSimulator.java

