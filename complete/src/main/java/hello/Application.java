package hello;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParser.Feature;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = StockWeeklyRecordRepository.class)
@EnableScheduling
public class Application implements CommandLineRunner {

	@Autowired
	private StockWeeklyRecordRepository repository;
	@Autowired
	private Stock4WeekChangeRepository week4ChangeRepository;
	@Autowired
	private Stock2WeekChangeRepository stock2WeekChangeRepository;
	@Autowired
	private UsStockDailyRecordRepository usrepository;
	@Autowired
	private RestTemplate restTemplate;

	private static final Logger log = LoggerFactory.getLogger(Application.class);
	private static final int MINS5 = 5;
	private static final int ONEHOUR = 60;
	private static final int ONEDAY = ONEHOUR * 4;
	private static final int ONEWEEK = ONEDAY * 5;
	private String day;
	private String lastCheckTime;
	private long lastCheckTime_alphavantage;
	private boolean firstRun = true;
	private AlphaVantageDaySeries avds = null;
	private Symbol2Name s2n;
	private final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(120);
	private List<StockWeeklyRecord> uslist = new ArrayList<StockWeeklyRecord>(); 

	private String[][] chinaSymbolArray = { { "sh510050", "0510050", "510050", "50ETF" },
			{ "sz159901", "1159901", "159901", "深100ETF" }, { "sh510300", "0510300", "510300", "300ETF" },
			{ "sh510500", "0510500", "510500", "500ETF" }, { "sh512100", "0512100", "512100", "1000ETF" },
			{ "sz159915", "1159915", "159915", "创业板" }, { "sh513100", "0513100", "513100", "纳指ETF" },
			{ "sz159928", "1159928", "159928", "消费ETF" }, { "sz162411", "1162411", "162411", "华宝油气" },
			{ "sh512010", "0512010", "512010", "医药ETF" }, { "sz160216", "1160216", "160216", "国泰商品" },
			{ "sh513500", "0513500", "513500", "标普500" }, };
	private String[] usSymbolArray = { "USO", "XOP" };

	public static void main(String args[]) {
		SpringApplication.run(Application.class);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		TrustStrategy acceptingTrustStrategy = (java.security.cert.X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext;
		sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);

		RestTemplate restTemplate = new RestTemplate(requestFactory);

		// RestTemplate restTemplate = new RestTemplate();
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
		HttpComponentsClientHttpRequestFactory factory = (HttpComponentsClientHttpRequestFactory) restTemplate
				.getRequestFactory();

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
			for (String[] array : chinaSymbolArray) {
				// get symbol and chinese name and current quote. quote in format 0.??? in
				// trading hour, 0.?? if not in
				// Use 126.net to get symbol
				// String symbol2Name =
				// "http://img1.money.126.net/data/hs/kline/day/history/2019/" + array[1] +
				// ".json";
				// try {
				// log.info("Get sybol from " + symbol2Name);
				// s2n = restTemplate.getForObject(symbol2Name, Symbol2Name.class);
				// } catch (Exception e) {
				// log.error(e.getLocalizedMessage());
				// // e.printStackTrace();
				// continue;
				// }
				// String[][] s = s2n.getData();
				// array[2] = s2n.getSymbol();
				// array[3] = s2n.getName();
				// log.info("126.net data: " + array[0] + "/" + array[1] + " Symbol: " +
				// array[2] + " Name: " + array[3]
				// + " date: " + s[s.length - 1][0]);

				// get historical data for 20 weeks, run only 1 time each run
				// use sina.com to get data
				String fullSymbol = array[0];
				String symbol = array[2];
				String stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol="
						+ fullSymbol + "&scale=" + ONEWEEK + "&ma=no&datalen=20";
				SinaDayRecord[] owr;
				try {
					log.debug("Get stock from " + stockdataUrl);
					owr = restTemplate.getForObject(stockdataUrl, SinaDayRecord[].class);
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
					// e.printStackTrace();
					continue;
				}
				for (SinaDayRecord weekdayData : owr) {
					log.trace("Sina.com weekly data: " + weekdayData);
					saveToMongoDB(symbol, weekdayData);
				}

				Set<String> yearweekset = new HashSet<>();
				List<StockWeeklyRecord> recordlist = repository.findByMyKeyStockId(array[2]);

				// Sort in des order
				Collections.sort(recordlist, new Comparator<StockWeeklyRecord>() {
					public int compare(StockWeeklyRecord p1, StockWeeklyRecord p2) {
						return p2.getDay().compareTo(p1.getDay());
					}
				});

				int count = 0;
				for (StockWeeklyRecord oneinst : recordlist) {
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
		for (String[] array : chinaSymbolArray) {
			// get historical data for 20 weeks
			// use sina.com to get data
			String fullSymbol = array[0];
			String symbol = array[2];
			String stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol="
					+ fullSymbol + "&scale=" + ONEWEEK + "&ma=no&datalen=20";
			SinaDayRecord[] owr;
			try {
				log.info("Get stock from " + stockdataUrl);
				owr = restTemplate.getForObject(stockdataUrl, SinaDayRecord[].class);
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				// e.printStackTrace();
				continue;
			}
			SinaDayRecord weekdayData = owr[owr.length - 1];
			log.trace("Sina.com weekly data: " + weekdayData);

			// get five minutes data
			stockdataUrl = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol="
					+ fullSymbol + "&scale=" + MINS5 + "&ma=no&datalen=1";
			try {
				log.info("Get stock from " + stockdataUrl);
				owr = restTemplate.getForObject(stockdataUrl, SinaDayRecord[].class);
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				// e.printStackTrace();
				continue;
			}

			SinaDayRecord min5Data = owr[owr.length - 1];

			for (SinaDayRecord r : owr) {
				min5Data = r;
				log.trace("Sina.com 5 mins data: " + r);
			}

			mergeSaveThisWeekData(symbol, weekdayData, min5Data);

			List<StockWeeklyRecord> swbysymbol = repository.findByMyKeyStockId(symbol);

			// Sort in des order
			Collections.sort(swbysymbol, new Comparator<StockWeeklyRecord>() {
				public int compare(StockWeeklyRecord p1, StockWeeklyRecord p2) {
					return p1.getDay().compareTo(p2.getDay());
				}
			});

			StockWeeklyRecord item = swbysymbol.get(swbysymbol.size() - 1);
			if (swbysymbol.size() > 4) {
				StockWeeklyRecord itemMinus1 = swbysymbol.get(swbysymbol.size() - 2);
				StockWeeklyRecord itemMinus2 = swbysymbol.get(swbysymbol.size() - 3);
				StockWeeklyRecord itemMinus4 = swbysymbol.get(swbysymbol.size() - 5);

				item.setWeek1change(changeRate(item, itemMinus1));
				item.setWeek2change(changeRate(item, itemMinus2));
				item.setWeek4change(changeRate(item, itemMinus4));
				repository.save(item);
			}

			// insert PUT300 record
			if (symbol.equalsIgnoreCase("510300")) {
				StockWeeklyRecord call300 = repository.findByMyKey(new MyKey("510300", day));
				StockWeeklyRecord put300 = new StockWeeklyRecord(new MyKey("PUT300", day), call300.getOpen(),
						call300.getClose(), call300.getLow(), call300.getHigh(), call300.getVolume());
				if (call300.getWeek4change() != null) {
					put300.setWeek1change(call300.getWeek1change().negate());
					put300.setWeek2change(call300.getWeek2change().negate());
					put300.setWeek4change(call300.getWeek4change().negate());
				}
				repository.save(put300);
				StockWeeklyRecord oldput300 = repository.findByMyKey(new MyKey("PUT300", weekdayData.getDay()));
				if (!weekdayData.getDay().equalsIgnoreCase(day) && oldput300 != null)
					repository.delete(oldput300);
			}
 
			// insert PUTSPY record
			if (symbol.equalsIgnoreCase("513500")) {
				StockWeeklyRecord call513500 = repository.findByMyKey(new MyKey("513500", day));
				StockWeeklyRecord PUTSPY = new StockWeeklyRecord(new MyKey("PUTSPY", day), call513500.getOpen(),
						call513500.getClose(), call513500.getLow(), call513500.getHigh(), call513500.getVolume());
				if (call513500.getWeek4change() != null) {
					PUTSPY.setWeek1change(call513500.getWeek1change().negate());
					PUTSPY.setWeek2change(call513500.getWeek2change().negate());
					PUTSPY.setWeek4change(call513500.getWeek4change().negate());
				}
				repository.save(PUTSPY);
				StockWeeklyRecord oldPUTSPY = repository.findByMyKey(new MyKey("PUTSPY", weekdayData.getDay()));
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
		List<StockWeeklyRecord> swbyid = repository.findByMyKeyDay(day);
		week4ChangeRepository.deleteAll();
		stock2WeekChangeRepository.deleteAll();

		// some symbol cannot be found at alpha vantage
		// SZ url format
		// https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=159915.SZ&apikey=TJI42OA10GGU4DXL
		// SH url format
		// https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=510300.SS&apikey=TJI42OA10GGU4DXL
		// SH url format
		// https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=USO&apikey=TJI42OA10GGU4DXL

		if (lastCheckTime_alphavantage == 0 || (System.currentTimeMillis() > (lastCheckTime_alphavantage + 3600000))) {
			uslist = new ArrayList<StockWeeklyRecord>();
			for (String usSymbol : usSymbolArray) {
				String alphavantageurl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="
						+ usSymbol + "&apikey=TJI42OA10GGU4DXL";
				try {
					avds = restTemplate.getForObject(alphavantageurl, AlphaVantageDaySeries.class);

					log.info("alpha vantage daily series data: " + avds);
					if (avds != null) {
						AlphaVantageMetaValue avmv = avds.getMetaData();
						log.info("alpha vantage metadata symbol: " + avmv.getSymbol());
						log.info("alpha vantage metadata Information: " + avmv.getInformation());
						log.info("alpha vantage metadata TimeZone: " + avmv.getTimeZone());
						Map<String, AlphaVantageDayValue> avdv = avds.getDayValues();
//						log.info("alpha vantage map #1 value: " + avdv.get("2019-06-17").getClose());
						Set<Entry<String, AlphaVantageDayValue>> entry2 = avdv.entrySet();
						SortedMap<String, AlphaVantageDayValue> sortedMap = new TreeMap<String, AlphaVantageDayValue>();
						for (Entry<String, AlphaVantageDayValue> temp : entry2) {
							sortedMap.put(temp.getKey(), temp.getValue());
						}
						entry2 = sortedMap.entrySet();
						String lastAlphaVantageRecord = "";
						String weekId = "0000-00";
						SortedMap<String, String> weeklymap = new TreeMap<String, String>();
						
						for (Entry<String, AlphaVantageDayValue> temp : entry2) {
							if ( getWeekOfYear(temp.getKey()) <=9 ) 
								weekId =  getYear(temp.getKey()) + "0" + getWeekOfYear(temp.getKey());
							else
								weekId =  "" + getYear(temp.getKey()) + getWeekOfYear(temp.getKey());

							log.info("Week #: " + weekId + " is No." + getDayOfWeek(temp.getKey())+ " day in a week");
							if( getDayOfWeek(temp.getKey()) != 6)
								weeklymap.put(weekId, temp.getKey());
							
							lastAlphaVantageRecord = temp.getKey() + " " + temp.getValue().getClose();
							MyKey mk = new MyKey();
							mk.setDay(temp.getKey());
							mk.setStockId(usSymbol);
							BigDecimal open = BigDecimal.valueOf(Double.parseDouble(temp.getValue().getOpen()));
							BigDecimal close = BigDecimal.valueOf(Double.parseDouble(temp.getValue().getClose()));
							BigDecimal high = BigDecimal.valueOf(Double.parseDouble(temp.getValue().getHigh()));
							BigDecimal low = BigDecimal.valueOf(Double.parseDouble(temp.getValue().getLow()));
							BigDecimal volume= BigDecimal.valueOf(Double.parseDouble(temp.getValue().getVolume()));

							UsStockDailyRecord usdr = new UsStockDailyRecord(mk,  open,  close,  low,  high, 
									 volume ) ;
							usrepository.save(usdr);
							
							log.info(lastAlphaVantageRecord);
						}

					    Stack<String> stack = new Stack<String>();
						for (Entry<String, String> temp : weeklymap.entrySet() ) {
							log.debug(temp.getKey() + ":" + temp.getValue());
							stack.push(temp.getValue());
						}
						log.info(stack.toString());
						String usNow =stack.pop();
						String us1WeekBack  =stack.pop();
						String us2WeekBack = stack.pop();
						stack.pop();
						String us4WeekBack = stack.pop();

						StockWeeklyRecord XOPORUSO = new StockWeeklyRecord(new MyKey(usSymbol, usNow), 
								new BigDecimal(sortedMap.get(usNow).getOpen()),
								new BigDecimal(sortedMap.get(usNow).getClose()), 
								new BigDecimal(sortedMap.get(usNow).getLow()), 
								new BigDecimal(sortedMap.get(usNow).getHigh()), 
								new BigDecimal(sortedMap.get(usNow).getVolume()));
  
						XOPORUSO.setWeek1change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us1WeekBack).getClose()));
						XOPORUSO.setWeek2change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us2WeekBack).getClose()));
						XOPORUSO.setWeek4change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us4WeekBack).getClose())); 
						
						uslist.add(XOPORUSO);
						
						if(usSymbol.equalsIgnoreCase("XOP")) {
							XOPORUSO = new StockWeeklyRecord(new MyKey("PUTXOP", usNow), 
									new BigDecimal(sortedMap.get(usNow).getOpen()),
									new BigDecimal(sortedMap.get(usNow).getClose()), 
									new BigDecimal(sortedMap.get(usNow).getLow()), 
									new BigDecimal(sortedMap.get(usNow).getHigh()), 
									new BigDecimal(sortedMap.get(usNow).getVolume()));
	  
							XOPORUSO.setWeek1change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us1WeekBack).getClose()).negate());
							XOPORUSO.setWeek2change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us2WeekBack).getClose()).negate());
							XOPORUSO.setWeek4change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us4WeekBack).getClose()).negate()); 
							
							uslist.add(XOPORUSO);
						} else if (usSymbol.equalsIgnoreCase("USO")) {
							XOPORUSO = new StockWeeklyRecord(new MyKey("PUTUSO", usNow), 
									new BigDecimal(sortedMap.get(usNow).getOpen()),
									new BigDecimal(sortedMap.get(usNow).getClose()), 
									new BigDecimal(sortedMap.get(usNow).getLow()), 
									new BigDecimal(sortedMap.get(usNow).getHigh()), 
									new BigDecimal(sortedMap.get(usNow).getVolume()));
	  
							XOPORUSO.setWeek1change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us1WeekBack).getClose()).negate());
							XOPORUSO.setWeek2change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us2WeekBack).getClose()).negate());
							XOPORUSO.setWeek4change(changeRateString(sortedMap.get(usNow).getClose(), sortedMap.get(us4WeekBack).getClose()).negate()); 
							
							uslist.add(XOPORUSO);
						}
					}
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
					// e.printStackTrace();
					// continue;
				}
			}
			lastCheckTime_alphavantage = System.currentTimeMillis();
		} else
			log.info("Do not visit alpha vantage website this time");
		

		// start to sort 4weeks change and 2weeks change
		log.trace("today: " + day);
		for (StockWeeklyRecord oneinst : swbyid) {
			log.info(oneinst.getStockId() + " " + oneinst.getDay() + " close:" + oneinst.getClose() + " 4 week: "
					+ oneinst.getWeek4change() + " 2 week: " + oneinst.getWeek2change());
		}

		for (StockWeeklyRecord oneinst : uslist) {
			swbyid.add(oneinst);
			log.info(oneinst.getStockId() + " " + oneinst.getDay() + " close:" + oneinst.getClose() + " 4 week: "
					+ oneinst.getWeek4change() + " 2 week: " + oneinst.getWeek2change());
		}
		
		// Sort in des order
		Collections.sort(swbyid, new Comparator<StockWeeklyRecord>() {
			public int compare(StockWeeklyRecord p1, StockWeeklyRecord p2) {
				return p2.getWeek4change().compareTo(p1.getWeek4change());
			}
		});

		log.info("Sort on week 4 changes for total stocks: " + swbyid.size());
		for (StockWeeklyRecord oneinst : swbyid) {
			week4ChangeRepository.save(new Stock4WeekChange(oneinst.getStockId(), oneinst.getDay(), oneinst.getClose(),
					oneinst.getWeek4change(), oneinst.getWeek2change(), oneinst.getWeek1change()));

			log.info(oneinst.getStockId() + " " + oneinst.getDay() + " close:" + oneinst.getClose() + " 4 week: "
					+ oneinst.getWeek4change());
		}
		// Sort in aes order
		Collections.sort(swbyid, new Comparator<StockWeeklyRecord>() {
			public int compare(StockWeeklyRecord p1, StockWeeklyRecord p2) {
				return p2.getWeek2change().compareTo(p1.getWeek2change());
			}
		});

		log.info("Sort on week 2 changes for total stocks: " + swbyid.size());
		for (StockWeeklyRecord oneinst : swbyid) {
			stock2WeekChangeRepository.save(new Stock2WeekChange(oneinst.getStockId(), oneinst.getDay(),
					oneinst.getClose(), oneinst.getWeek4change(), oneinst.getWeek2change(), oneinst.getWeek1change()));

			log.info(oneinst.getStockId() + " " + oneinst.getDay() + " close:" + oneinst.getClose() + " 2 week: "
					+ oneinst.getWeek2change());
		}

		log.info("Last Check Time:" + lastCheckTime);
	};

	private BigDecimal changeRate(StockWeeklyRecord item, StockWeeklyRecord itemMinus1) {
		BigDecimal bd0 = itemMinus1.getClose();
		BigDecimal bdnow = item.getClose();
		BigDecimal week4diff = bdnow.subtract(bd0).divide(bd0, 4, RoundingMode.HALF_UP);

		return week4diff;
	}

	private BigDecimal changeRateString(String item, String itemMinus1) {
		BigDecimal bd0 = new BigDecimal(itemMinus1);
		BigDecimal bdnow = new BigDecimal(item); 
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

	private void mergeSaveThisWeekData(String symbol, SinaDayRecord weekdayData, SinaDayRecord min5Data) {
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

			SinaDayRecord owr = new SinaDayRecord();
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

			SinaDayRecord owr = new SinaDayRecord();
			owr.setDay(day);
			owr.setClose(close);
			owr.setLow(low);
			owr.setVolume(volume);
			owr.setHigh(high);
			owr.setOpen(open);

			saveToMongoDB(symbol, owr);
		}
	}

	private String getCheckTime(SinaDayRecord weekdayData, SinaDayRecord min5Data) {
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

	private int getDayOfWeek(String input) {
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
		int week = cal2.get(Calendar.DAY_OF_WEEK);
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
		repository.save(new StockWeeklyRecord(mKey, open, close, low, high, volume));
		log.trace("mKey hashCode: " + mKey.hashCode());
	}

	private void saveToMongoDB(String symbol, SinaDayRecord owr) {
		MyKey mKey = new MyKey(symbol, owr.getDay());
		BigDecimal open = new BigDecimal(owr.getOpen());
		BigDecimal close = new BigDecimal(owr.getClose());
		BigDecimal high = new BigDecimal(owr.getHigh());
		BigDecimal low = new BigDecimal(owr.getLow());
		BigDecimal volume = new BigDecimal(owr.getVolume());
		log.trace("mKey: " + mKey);
		log.trace("mKey exists? " + repository.existsById(mKey));
		StockWeeklyRecord swr = repository.findByMyKey(mKey);
		if (swr == null) {
			repository.save(new StockWeeklyRecord(mKey, open, close, low, high, volume));
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