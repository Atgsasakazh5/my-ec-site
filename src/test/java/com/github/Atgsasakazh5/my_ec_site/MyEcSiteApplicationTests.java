package com.github.Atgsasakazh5.my_ec_site;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@SpringBootTest
@Sql(scripts = {"/schema-mysql.sql", "/data-mysql.sql"})
class MyEcSiteApplicationTests {

    @Test
    void contextLoads() {
    }

}
