package hello;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsStockDailyRecordRepository extends MongoRepository<UsStockDailyRecord, MyKey> {

    public UsStockDailyRecord findByMyKey(MyKey myKey);
    public List<UsStockDailyRecord> findByMyKeyDay(String day);
    public List<UsStockDailyRecord> findByMyKeyStockId(String stockId);

}
