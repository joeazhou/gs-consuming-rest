package hello;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Stock4WeekChangeRepository extends MongoRepository<Stock4WeekChange, String> {
	    public List<Stock4WeekChange> findByDay(String day);
	    
}