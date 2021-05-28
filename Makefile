# For building the sequencetester.jar jar file the normal way


all: 	gradle/wrapper/gradle-wrapper.properties
	sh ./gradlew build

gradle/wrapper/gradle-wrapper.properties:
	gradle wrapper

# For building the javadocs

javadoc: 
	sh ./gradlew javadoc


# For running the example test

test:
	sh ./gradlew test

clean:
	sh ./gradlew clean

