package hello;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Stock2WeekChangeRepository extends MongoRepository<Stock2WeekChange, String> {
	    public List<Stock2WeekChange> findByDay(String day);
	    
}