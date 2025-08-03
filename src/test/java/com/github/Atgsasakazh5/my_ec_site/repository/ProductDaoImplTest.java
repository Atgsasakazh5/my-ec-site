package com.github.Atgsasakazh5.my_ec_site.repository;

import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(ProductDaoImpl.class)
class ProductDaoImplTest {


}