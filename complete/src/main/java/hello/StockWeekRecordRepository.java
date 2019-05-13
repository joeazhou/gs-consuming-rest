package hello;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface StockWeekRecordRepository extends MongoRepository<StockWeekRecord, MyKey> {

    public StockWeekRecord findByMyKey(MyKey myKey);
//    public List<StockWeekRecord> findByStockSymbol(String stockSymbol);

}
