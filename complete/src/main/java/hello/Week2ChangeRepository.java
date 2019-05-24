package hello;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Week2ChangeRepository extends MongoRepository<Week2Change, String> {
	    public List<Week2Change> findByDay(String day);
	    
}