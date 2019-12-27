import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;


public class NestedUdtExample {

    public static void main(String[] args) throws Exception {

        try (CqlSession session = CqlSession.builder().build()) {
            createSchema(session);
            PreparedStatement prepStmt = session.prepare("INSERT INTO ks.addresses (id, address) VALUES (:id, :address);");
            UserDefinedType addressUdt = getUdt(session, "address");
            UserDefinedType personUdt = getUdt(session, "person");
            UdtValue newPerson = personUdt.newValue("john", "doe");
            UdtValue newAddress = addressUdt.newValue("100 main st", newPerson);
            BoundStatement boundStmt =
                    prepStmt.boundStatementBuilder()
                            .setInt("id", 0)
                            .setUdtValue("address", newAddress)
                            .build();
            session.execute(boundStmt);
            Row r = session.execute("SELECT * FROM ks.addresses WHERE id = 0").one();
            UdtValue udtValue = r.getUdtValue("address");
            System.out.println();
            System.out.print("READ ROW FROM CASSANDRA:");
            System.out.println(udtValue.getFormattedContents());
        }
    }


    private static void createSchema(CqlSession session) {

        session.execute(
                "CREATE KEYSPACE IF NOT EXISTS ks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");

        session.execute("CREATE TYPE IF NOT EXISTS ks.person (firstname text, lastname text)");
        session.execute("CREATE TYPE IF NOT EXISTS ks.address (street text, owner frozen<person>)");
        session.execute(
                "CREATE TABLE IF NOT EXISTS ks.addresses (id int PRIMARY KEY, address frozen<address>)");
    }

    private static UserDefinedType getUdt(CqlSession session, String udt_name){
        return session.getMetadata()
                        .getKeyspace("ks")
                        .flatMap(ks -> ks.getUserDefinedType(udt_name))
                        .orElseThrow(() -> new IllegalArgumentException("Missing UDT definition"));
    }

}