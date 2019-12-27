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

### Schema Used
```
CREATE KEYSPACE IF NOT EXISTS ks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
CREATE TYPE IF NOT EXISTS ks.person (firstname text, lastname text);
CREATE TYPE IF NOT EXISTS ks.address (street text, owner frozen<person>);
CREATE TABLE IF NOT EXISTS ks.addresses (id int PRIMARY KEY, address frozen<address>);
```

### Driver stuff
Get definition of UDT from driver metadata
```
private static UserDefinedType getUdt(CqlSession session, String udt_name){
        return session.getMetadata()
                        .getKeyspace("ks")
                        .flatMap(ks -> ks.getUserDefinedType(udt_name))
                        .orElseThrow(() -> new IllegalArgumentException("Missing UDT definition"));
    }
```

Create new UdtValues 
```
UserDefinedType addressUdt = getUdt(session, "address");
UserDefinedType personUdt = getUdt(session, "person");
UdtValue newPerson = personUdt.newValue("john", "doe");
UdtValue newAddress = addressUdt.newValue("100 main st", newPerson);
```

Bind & Execute
```
BoundStatement boundStmt =
                    prepStmt.boundStatementBuilder()
                            .setInt("id", 0)
                            .setUdtValue("address", newAddress)
                            .build();
session.execute(boundStmt);
```

The imports
```
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
```
