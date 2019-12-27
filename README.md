# nested-udt-java

Example showing how to use nested UDT's in Cassandra with the DataStax Java Driver's `PreparedStatements`

### Prerequisites
- Cassandra instance running locally
- Java 8 installed
- Maven installed

### Running

Clone the repo
```
git clone https://github.com/csplinter/nested-udt-java.git
```

Go to the directory
```
cd nested-udt-java
```

Run it
```
mvn clean compile exec:java -Dexec.mainClass=NestedUdtExample
```

Should see a few INFO lines followed by this towards the end
```
...
READ ROW FROM CASSANDRA:{street:'100 main st',owner:{firstname:'john',lastname:'doe'}}
```
