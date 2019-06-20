package hello;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockWeeklyRecordRepository extends MongoRepository<StockWeeklyRecord, MyKey> {

    public StockWeeklyRecord findByMyKey(MyKey myKey);
    public List<StockWeeklyRecord> findByMyKeyDay(String day);
    public List<StockWeeklyRecord> findByMyKeyStockId(String stockId);

}
