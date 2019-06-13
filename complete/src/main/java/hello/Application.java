package hello;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParser.Feature;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = StockWeekRecordRepository.class)
@EnableScheduling
public class Application implements CommandLineRunner {

	@Autowired
	private StockWeekRecordRepository repository;
	@Autowired
	private StockWeekChangeRepository weekChangeRepository;
	@Autowired
	private Week2ChangeRepository week2ChangeRepository;
	@Autowired
	private RestTemplate restTemplate;

	private static final Logger log = LoggerFactory.getLogger(Application.class);
	private static final int MINS5 = 5;
	private static final int ONEHOUR = 60;
	private static final int ONEDAY = ONEHOUR * 4;
	private static final int ONEWEEK = ONEDAY * 5;
	private String day;
	private String lastCheckTime;
	private boolean firstRun = true;

	private Symbol2Name s2n;
	private final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(120);

	private String[][] symbolArray = { { "sh510050", "0510050", "", "" }, { "sz159901", "1159901", "", "" },
			{ "sh510300", "0510300", "", "" }, { "sh510500", "0510500", "", "" }, { "sh512100", "0512100", "", "" },
			{ "sz159915", "1159915", "", "" }, { "sh513100", "0513100", "", "" }, { "sz159928", "1159928", "", "" },
			{ "sz162411", "1162411", "", "" }, { "sh512010", "0512010", "", "" }, { "sz160216", "1160216", "", "" },
			{ "sh513500", "0513500", "", "" }, };

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

		// https://stackoverflow.com/questions/43909219/spring-resttemplate-connection-timeout-is-not-working
		SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();

		factory.setReadTimeout(TIMEOUT);
		factory.setConnectTimeout(TIMEOUT);

		return restTemplate;
	}

	@Override
	public void run(String... strings) throws Exception {
		// loopToFindData();
	}

	@Scheduled(fixedRate = 60000)
	private void loopToFindData() {
		if (firstRun == true) {
			// clean existing data
			// get stock symbol once and for all
			// loop for every stock
			for (String[] array : symbolArray) {
				// get symbol and chinese name and current quote. quote in format 0.??? in
				// trading hour, 0.?? if not in
				// Use 126.net to get symbol
				String symbol2Name = "http://img1.money.126.net/data/hs/kline/day/history/2019/" + array[1] + ".json";
				try {
					s2n = restTemplate.getForObject(symbol2Name, Symbol2Name.class);
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
					// e.printStackTrace();
					continue;
				}
				String[][] s = s2n.getData();
				array[2] = s2n.getSymbol();
				array[3] = s2n.getName();
				log.info("126.net data: " + array[0] + "/" + array[1] + " Symbol: " + array[2] + " Name: " + array[3]
						+ " date: " + s[s.length - 1][0]);

				Set<String> yearweekset = new HashSet<>();
				List<StockWeekRecord> recordlist = repository.findByMyKeyStockId(s2n.getSymbol());

				// Sort in des order
				Collections.sort(recordlist, new Comparator<StockWeekRecord>() {
					public int compare(StockWeekRecord p1, StockWeekRecord p2) {
						return p2.getDay().compareTo(p1.getDay());
					}
				});

				int count = 0;
				for (StockWeekRecord oneinst : recordlist) {
					String oneday = oneinst.getDay();
					log.trace("Day: " + oneday + " Year: " + getYear(oneday) + " Week: " + getWeekOfYear(oneday));
					String weekKey = getYear(oneday) + "-" + getWeekOfYear(oneday);
					if (count == 0) {
						yearweekset.add(weekKey);
						count++;
					} else if (yearweekset.contains(weekKey)) {
						repository.delete(oneinst);
						log.trace("deleting " + oneinst.getDay());
						continue;
					} else {
						yearweekset.add(weekKey);
					}
				}
				log.trace("# of record saved: " + yearweekset.size());
			}
		}
		if (!isTrading() && firstRun == false) {
			return;
		}
		firstRun = false;

		// loop for every stock
		for (String[] array : symbolArray) {
			// get historical data for 20 weeks
			// use sina.com to get data
			String fullSymbol = array[0];
			String symbol = array[2];
			String stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol="
					+ fullSymbol + "&scale=" + ONEWEEK + "&ma=no&datalen=20";
			OneWeekRecord[] owr;
			try {
				owr = restTemplate.getForObject(stockdataUrl, OneWeekRecord[].class);
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				// e.printStackTrace();
				continue;
			}
			OneWeekRecord weekdayData = owr[owr.length - 1];
			log.trace("Sina.com weekly data: " + weekdayData);

			// get five minutes data
			stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol="
					+ fullSymbol + "&scale=" + MINS5 + "&ma=no&datalen=1";
			try {
				owr = restTemplate.getForObject(stockdataUrl, OneWeekRecord[].class);
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				// e.printStackTrace();
				continue;
			}

			OneWeekRecord min5Data = owr[owr.length - 1];

			for (OneWeekRecord r : owr) {
				min5Data = r;
				log.trace("Sina.com 5 mins data: " + r);
			}

			mergeSaveThisWeekData(symbol, weekdayData, min5Data);

			List<StockWeekRecord> swbysymbol = repository.findByMyKeyStockId(symbol);

			// Sort in des order
			Collections.sort(swbysymbol, new Comparator<StockWeekRecord>() {
				public int compare(StockWeekRecord p1, StockWeekRecord p2) {
					return p1.getDay().compareTo(p2.getDay());
				}
			});

			StockWeekRecord item = swbysymbol.get(swbysymbol.size() - 1);
			StockWeekRecord itemMinus1 = swbysymbol.get(swbysymbol.size() - 2);
			StockWeekRecord itemMinus2 = swbysymbol.get(swbysymbol.size() - 3);
			StockWeekRecord itemMinus4 = swbysymbol.get(swbysymbol.size() - 5);

			item.setWeek1change(changeRate(item, itemMinus1));
			item.setWeek2change(changeRate(item, itemMinus2));
			item.setWeek4change(changeRate(item, itemMinus4));
			repository.save(item);

			// insert PUT300 record
			if (symbol.equalsIgnoreCase("510300")) {
				StockWeekRecord call300 = repository.findByMyKey(new MyKey("510300", day));
				StockWeekRecord put300 = new StockWeekRecord(new MyKey("PUT300", day), call300.getOpen(),
						call300.getClose(), call300.getLow(), call300.getHigh(), call300.getVolume());
				put300.setWeek1change(call300.getWeek1change().negate());
				put300.setWeek2change(call300.getWeek2change().negate());
				put300.setWeek4change(call300.getWeek4change().negate());
				repository.save(put300);
				StockWeekRecord oldput300 = repository.findByMyKey(new MyKey("PUT300", weekdayData.getDay()));
				if (!weekdayData.getDay().equalsIgnoreCase(day) && oldput300 != null)
					repository.delete(oldput300);
			}

			// insert PUTXOP record
			if (symbol.equalsIgnoreCase("162411")) {
				StockWeekRecord callxop = repository.findByMyKey(new MyKey("162411", day));
				StockWeekRecord PUTXOP = new StockWeekRecord(new MyKey("PUTXOP", day), callxop.getOpen(),
						callxop.getClose(), callxop.getLow(), callxop.getHigh(), callxop.getVolume());
				PUTXOP.setWeek1change(callxop.getWeek1change().negate());
				PUTXOP.setWeek2change(callxop.getWeek2change().negate());
				PUTXOP.setWeek4change(callxop.getWeek4change().negate());
				repository.save(PUTXOP);
				StockWeekRecord oldPUTXOP = repository.findByMyKey(new MyKey("PUTXOP", weekdayData.getDay()));
				if (!weekdayData.getDay().equalsIgnoreCase(day) && oldPUTXOP != null)
					repository.delete(oldPUTXOP);
			}

			// insert PUTSPY record
			if (symbol.equalsIgnoreCase("513500")) {
				StockWeekRecord call513500 = repository.findByMyKey(new MyKey("513500", day));
				StockWeekRecord PUTSPY = new StockWeekRecord(new MyKey("PUTSPY", day), call513500.getOpen(),
						call513500.getClose(), call513500.getLow(), call513500.getHigh(), call513500.getVolume());
				PUTSPY.setWeek1change(call513500.getWeek1change().negate());
				PUTSPY.setWeek2change(call513500.getWeek2change().negate());
				PUTSPY.setWeek4change(call513500.getWeek4change().negate());
				repository.save(PUTSPY);
				StockWeekRecord oldPUTSPY = repository.findByMyKey(new MyKey("PUTSPY", weekdayData.getDay()));
				if (!weekdayData.getDay().equalsIgnoreCase(day) && oldPUTSPY != null)
					repository.delete(oldPUTSPY);
			}

			// log which stock is processed
			// for (String oneName : array) {
			log.info(array[array.length - 1] + " is scanned.");
			// }
			//
			// System.out.print(" is scanned.");
			// System.out.println();
		}

		// delete 2 weeks change table and 4 weeks change table
		List<StockWeekRecord> swbyid = repository.findByMyKeyDay(day);
		weekChangeRepository.deleteAll();
		week2ChangeRepository.deleteAll();

		log.trace("today: " + day);
		for (StockWeekRecord oneinst : swbyid) {
			log.trace(oneinst.getStockId() + " " + oneinst.getDay() + " close:" + oneinst.getClose() + " 4 week: "
					+ oneinst.getWeek4change() + " 2 week: " + oneinst.getWeek2change());
		}

		// Sort in des order
		Collections.sort(swbyid, new Comparator<StockWeekRecord>() {
			public int compare(StockWeekRecord p1, StockWeekRecord p2) {
				return p2.getWeek4change().compareTo(p1.getWeek4change());
			}
		});

		log.info("Sort on week 4 changes for total stocks: " + swbyid.size());
		for (StockWeekRecord oneinst : swbyid) {
			weekChangeRepository.save(new StockWeekChange(oneinst.getStockId(), oneinst.getDay(), oneinst.getClose(),
					oneinst.getWeek4change(), oneinst.getWeek2change(), oneinst.getWeek1change()));

			log.info(oneinst.getStockId() + " " + oneinst.getDay() + " close:" + oneinst.getClose() + " 4 week: "
					+ oneinst.getWeek4change());
		}
		// Sort in aes order
		Collections.sort(swbyid, new Comparator<StockWeekRecord>() {
			public int compare(StockWeekRecord p1, StockWeekRecord p2) {
				return p2.getWeek2change().compareTo(p1.getWeek2change());
			}
		});

		log.info("Sort on week 2 changes for total stocks: " + swbyid.size());
		for (StockWeekRecord oneinst : swbyid) {
			week2ChangeRepository.save(new Week2Change(oneinst.getStockId(), oneinst.getDay(), oneinst.getClose(),
					oneinst.getWeek4change(), oneinst.getWeek2change(), oneinst.getWeek1change()));

			log.info(oneinst.getStockId() + " " + oneinst.getDay() + " close:" + oneinst.getClose() + " 2 week: "
					+ oneinst.getWeek2change());
		}
		
		log.info("Last Check Time:" + lastCheckTime);
	};

	private BigDecimal changeRate(StockWeekRecord item, StockWeekRecord itemMinus1) {
		BigDecimal bd0 = itemMinus1.getClose();
		BigDecimal bdnow = item.getClose();
		BigDecimal week4diff = bdnow.subtract(bd0).divide(bd0, 4, RoundingMode.HALF_UP);

		return week4diff;
	}

	private boolean isTrading() {
		Date date = new Date();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		calendar.setTime(date);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		boolean trading = timeInTradeHours(dayOfWeek, hour, minute);
		log.info("One loop is done");
		return trading;
	}

	private boolean timeInTradeHours(int day, int hour, int minute) {
		boolean inTradeHours = false;
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
			return inTradeHours;
		}
		switch (hour) {
		case 9:
			if (minute > 15)
				inTradeHours = true;
			break;
		case 10:
			inTradeHours = true;
			break;
		case 11:
			if (minute < 35)
				inTradeHours = true;
			break;
		case 13:
			inTradeHours = true;
			break;
		case 14:
			inTradeHours = true;
			break;
		case 15:
			if (minute < 40)
				inTradeHours = true;
			break;
		default:
			inTradeHours = false;
		}
		return inTradeHours;
	}

	private void mergeSaveThisWeekData(String symbol, OneWeekRecord weekdayData, OneWeekRecord min5Data) {
		String input = weekdayData.getDay();
		String feedin = min5Data.getDay();
		day = getDate(min5Data.getDay());
		log.info("Compare 2 days: " + weekdayData.getDay() + " and " + min5Data.getDay());
		lastCheckTime = getCheckTime(weekdayData, min5Data);
		log.info("Latest check time: " + lastCheckTime);
		
		if (inSameDay(input, feedin)) {
			log.trace("Same day. Do nothing");
		} else if (inSameWeek(input, feedin)) {
			log.trace("Same week. save latest min5 data. delete last day data");
			MyKey mk = new MyKey();
			mk.setDay(day);
			mk.setStockId(symbol);

			String close = min5Data.getClose();
			String low = min5Data.getLow();
			String volume = min5Data.getVolume();
			String high = min5Data.getHigh();
			String open = min5Data.getOpen();

			OneWeekRecord owr = new OneWeekRecord();
			owr.setDay(day);
			owr.setClose(close);
			owr.setLow(low);
			owr.setVolume(volume);
			owr.setHigh(high);
			owr.setOpen(open);

			log.trace("saving " + symbol + " " + owr);
			saveToMongoDB(symbol, owr);

			MyKey deleteKey = new MyKey(symbol, weekdayData.getDay());
			repository.deleteById(deleteKey);
		} else {
			log.trace("Different week. save latest min5 data. ");
			// not in the same week, save latest record
			MyKey mk = new MyKey();
			mk.setDay(day);
			mk.setStockId(symbol);

			String close = min5Data.getClose();
			String low = min5Data.getLow();
			String volume = min5Data.getVolume();
			String high = min5Data.getHigh();
			String open = min5Data.getOpen();

			OneWeekRecord owr = new OneWeekRecord();
			owr.setDay(day);
			owr.setClose(close);
			owr.setLow(low);
			owr.setVolume(volume);
			owr.setHigh(high);
			owr.setOpen(open);

			saveToMongoDB(symbol, owr);
		}
	}

	private String getCheckTime(OneWeekRecord weekdayData, OneWeekRecord min5Data) {
		Date lastOne = new Date();
		String format = "yyyy-MM-dd";
		SimpleDateFormat df = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = df.parse(weekdayData.getDay());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date);
		long firstTime = cal2.getTimeInMillis();
		
		try {
			date = df.parse(min5Data.getDay());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		cal2 = Calendar.getInstance();
		cal2.setTime(date);
		long secondTime = cal2.getTimeInMillis();
		if (firstTime < secondTime)
			return min5Data.getDay();
		else
			return weekdayData.getDay();
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
		// System.out.println(date);
		return date;
	}

	private boolean inSameDay(String input, String feedin) {
		int week = getDayOfMonth(input);
		int feedinweek = getDayOfMonth(feedin);

		if (week == feedinweek)
			return true;
		else
			return false;
	}

	private int getDayOfMonth(String input) {
		String format = "yyyy-MM-dd";
		SimpleDateFormat df = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = df.parse(input);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date);
		int week = cal2.get(Calendar.DAY_OF_MONTH);
		return week;
	}

	private int getWeekOfYear(String input) {
		String format = "yyyy-MM-dd";
		SimpleDateFormat df = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = df.parse(input);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date);
		int week = cal2.get(Calendar.WEEK_OF_YEAR);
		return week;
	}

	private int getYear(String input) {
		String format = "yyyy-MM-dd";
		SimpleDateFormat df = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = df.parse(input);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date);
		int week = cal2.get(Calendar.YEAR);
		return week;
	}

	private boolean inSameWeek(String input, String feedin) {
		int week = getWeekOfYear(input);
		int feedinweek = getWeekOfYear(feedin);

		if (week == feedinweek)
			return true;
		else
			return false;
	}

	private void saveTodaySampleDataToMongoDB() {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		String formatDate = ft.format(dNow);

		MyKey mKey = new MyKey("162411", formatDate);
		BigDecimal open = new BigDecimal("2");
		BigDecimal close = new BigDecimal("2.22");
		BigDecimal high = new BigDecimal("2.33");
		BigDecimal low = new BigDecimal("2");
		BigDecimal volume = new BigDecimal("23432432");
		repository.save(new StockWeekRecord(mKey, open, close, low, high, volume));
		log.trace("mKey hashCode: " + mKey.hashCode());
	}

	private void saveToMongoDB(String symbol, OneWeekRecord owr) {
		MyKey mKey = new MyKey(symbol, owr.getDay());
		BigDecimal open = new BigDecimal(owr.getOpen());
		BigDecimal close = new BigDecimal(owr.getClose());
		BigDecimal high = new BigDecimal(owr.getHigh());
		BigDecimal low = new BigDecimal(owr.getLow());
		BigDecimal volume = new BigDecimal(owr.getVolume());
		log.trace("mKey: " + mKey);
		log.trace("mKey exists? " + repository.existsById(mKey));
		StockWeekRecord swr = repository.findByMyKey(mKey);
		if (swr == null) {
			repository.save(new StockWeekRecord(mKey, open, close, low, high, volume));
			log.info("Create Symbol: " + symbol + " close: " + close);
		} else {
			swr.setOpen(open);
			swr.setClose(close);
			swr.setHigh(high);
			swr.setLow(low);
			swr.setVolume(volume);
			repository.save(swr);
			log.info("Update Symbol: " + symbol + " close: " + close);
		}
	}
}