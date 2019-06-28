package hello;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaVantageDaySeries {

    @JsonProperty(value = "Meta Data")
	private AlphaVantageMetaValue metaData;

    @JsonProperty(value = "Time Series (Daily)")
	private Map<String, AlphaVantageDayValue> dayValues = new HashMap<>();
    
	public AlphaVantageDaySeries() {
		// TODO Auto-generated constructor stub
	}

	public AlphaVantageMetaValue getMetaData() {
		return metaData;
	}

	public Map<String, AlphaVantageDayValue> getDayValues() {
		return dayValues;
	}

	@Override
	public String toString() {
		return "AlphaVantageDaySeries [metaData=" + metaData.getSymbol() + ", dayValues#=" + dayValues.size() + "]";
	}

}
