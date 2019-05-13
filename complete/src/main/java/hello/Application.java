package hello;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParser.Feature;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = StockWeekRecordRepository.class)
public class Application {

	@Autowired
	private StockWeekRecordRepository repository;
	
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String args[]) {
		SpringApplication.run(Application.class);
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		RestTemplate restTemplate = new RestTemplate();
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		//允许使用未带引号的字段名
		mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		//允许使用单引号
		mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);

		mappingJackson2HttpMessageConverter.setObjectMapper(mapper);
		restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
		return restTemplate;
//		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			String stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sh510050&scale=1200&ma=no&datalen=20";
	        OneWeekRecord [] owr = restTemplate.getForObject(stockdataUrl, OneWeekRecord[].class);
	        
	        for (OneWeekRecord employee : owr) {
	        	  System.out.println(employee);
	        }
	        
	        stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sz162411&scale=240&ma=no&datalen=20";
	        owr = restTemplate.getForObject(stockdataUrl, OneWeekRecord[].class);
	        
	        for (OneWeekRecord employee : owr) {
	        	  System.out.println(employee);
	        }
	        
	        String symbol2Name = "http://img1.money.126.net/data/hs/kline/day/history/2019/1162411.json";
	        Symbol2Name s2n = restTemplate.getForObject(symbol2Name, Symbol2Name.class);
	        log.info("Symbol: " + s2n.getSymbol() );
	        log.info("Name: " + s2n.getName() );
	        
	        Date dNow = new Date( );
	        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
	        String formatDate = ft.format(dNow);
	        System.out.println("Current Date: " + formatDate);
	        
//			// save a couple of customers
	        MyKey mKey = new MyKey("162411", formatDate);
	        BigDecimal open = new BigDecimal("2");
	        BigDecimal close = new BigDecimal("2.22");
	        BigDecimal high = new BigDecimal("2.33");
	        BigDecimal low = new BigDecimal("2");
	        BigDecimal volume = new BigDecimal("23432432");
			repository.save(new StockWeekRecord(mKey, open, close, low, high, volume)) ;
//			repository.save(new Customer("Bob", "Smith"));
		};
	}
}