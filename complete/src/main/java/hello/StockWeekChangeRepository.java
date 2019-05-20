package hello;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockWeekChangeRepository extends MongoRepository<StockWeekChange, String> {
	    public List<StockWeekChange> findByDay(String day);
	    
}