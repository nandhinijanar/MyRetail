package myretail.repository;

import myretail.dto.Price;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface ProductRepository extends CassandraRepository<Price, Integer> {

}
