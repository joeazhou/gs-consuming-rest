package hello;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParser.Feature;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = StockWeekRecordRepository.class)
@EnableScheduling
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
		mappingJackson2HttpMessageConverter
		.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		// 允许使用未带引号的字段名
		mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		// 允许使用单引号
		mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);

		mappingJackson2HttpMessageConverter.setObjectMapper(mapper);
		restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
		return restTemplate;
	}

	private static final int MINS5 = 5;
	private static final int ONEHOUR = 60;
	private static final int ONEDAY = ONEHOUR * 4;
	private static final int ONEWEEK = ONEDAY * 5;

//	@Scheduled(fixedRate = 50000)
	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			String[][] symbolArray = { { "sh510050", "0510050" }, { "sz162411", "1162411" } };

			String symbol2Name = "http://img1.money.126.net/data/hs/kline/day/history/2019/" + symbolArray[0][1]
					+ ".json";
			Symbol2Name s2n = restTemplate.getForObject(symbol2Name, Symbol2Name.class);
			log.info("Symbol: " + s2n.getSymbol());
			log.info("Name: " + s2n.getName());
			String[][] s = s2n.getData();
			log.info("data[date]: " + s[s.length - 1][0]);
			log.info("data[open]: " + s[s.length - 1][1]);
			log.info("data[close]: " + s[s.length - 1][2]);
//			log.info("data[high]: " + s[s.length - 1][3]);
//			log.info("data[low]: " + s[s.length - 1][4]);

			System.out.println("Working on data: " + symbolArray[0][0] + "/" + symbolArray[0][1]);
			String fullSymbol = symbolArray[0][0];
			String stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol="
					+ fullSymbol + "&scale=" + ONEWEEK + "&ma=no&datalen=20";
			OneWeekRecord[] owr = restTemplate.getForObject(stockdataUrl, OneWeekRecord[].class);
			OneWeekRecord weekdayData = owr[owr.length - 1];

			for (OneWeekRecord r : owr) {
				weekdayData = r;
				System.out.println(r);
				saveToMongoDB(s2n.getSymbol(), r);
			}

			System.out.println();
			stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol="
					+ fullSymbol + "&scale=" + MINS5 + "&ma=no&datalen=1";
			owr = restTemplate.getForObject(stockdataUrl, OneWeekRecord[].class);

			OneWeekRecord min5Data = owr[owr.length - 1];

			for (OneWeekRecord r : owr) {
				min5Data = r;
				System.out.println(r);
			}

			mergeSaveThisWeekData(s2n.getSymbol(), weekdayData, min5Data);
		};
	}

	private void mergeSaveThisWeekData(String symbol, OneWeekRecord weekdayData, OneWeekRecord min5Data) {
		String input = weekdayData.getDay();
		String feedin = min5Data.getDay();

		if (inSameWeek(input, feedin)) {
			MyKey mk = new MyKey();
			mk.setDay(getDate(min5Data.getDay()));
			mk.setStockId(symbol);

//			System.out.println("same week at" + getDate(min5Data.getDay()));
			
			String close = min5Data.getClose();
			String low = weekdayData.getLow();
			String volume =weekdayData.getVolume();
			String high = weekdayData.getHigh();
			String open = weekdayData.getOpen();
			
			OneWeekRecord owr = new OneWeekRecord();
			owr.setDay(getDate(min5Data.getDay()));
			owr.setClose(close);
			owr.setLow(low);
			owr.setVolume(volume);
			owr.setHigh(high);
			owr.setOpen(open);
			
			saveToMongoDB(symbol, owr);
			
			MyKey deleteKey = new MyKey(symbol, weekdayData.getDay());
			repository.deleteById(deleteKey);
		}
	}

	private String getDate(String day) {
		String format = "yyyy-MM-dd";
		SimpleDateFormat df = new SimpleDateFormat(format);

		Date min5date = new Date();
		try {
			min5date = df.parse(day);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String date = df.format(min5date);
//		System.out.println(date);
		return date;
	}

	private boolean inSameWeek(String input, String feedin) {
		String format = "yyyy-MM-dd";
		SimpleDateFormat df = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = df.parse(input);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date);
		int week = cal2.get(Calendar.WEEK_OF_YEAR);
		System.out.println(input + " is in week #" + week);

		Date min5date = new Date();
		try {
			min5date = df.parse(feedin);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Calendar cal3 = Calendar.getInstance();
		cal3.setTime(min5date);
		int feedinweek = cal3.get(Calendar.WEEK_OF_YEAR);
		System.out.println(feedin + " is in week #" + feedinweek);

		if (week == feedinweek)
			return true;
		else
			return false;
	}

	private void saveTodaySampleDataToMongoDB() {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		String formatDate = ft.format(dNow);
//		System.out.println("Current Date: " + formatDate);

		MyKey mKey = new MyKey("162411", formatDate);
		BigDecimal open = new BigDecimal("2");
		BigDecimal close = new BigDecimal("2.22");
		BigDecimal high = new BigDecimal("2.33");
		BigDecimal low = new BigDecimal("2");
		BigDecimal volume = new BigDecimal("23432432");
		repository.save(new StockWeekRecord(mKey, open, close, low, high, volume));
		System.out.println("mKey hashCode: " + mKey.hashCode());
	}

	private void saveToMongoDB(String symbol, OneWeekRecord owr) {
		MyKey mKey = new MyKey(symbol, owr.getDay());
		BigDecimal open = new BigDecimal(owr.getOpen());
		BigDecimal close = new BigDecimal(owr.getClose());
		BigDecimal high = new BigDecimal(owr.getHigh());
		BigDecimal low = new BigDecimal(owr.getLow());
		BigDecimal volume = new BigDecimal(owr.getVolume());
		repository.save(new StockWeekRecord(mKey, open, close, low, high, volume));
	}
}