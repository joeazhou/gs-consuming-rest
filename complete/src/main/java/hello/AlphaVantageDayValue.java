package hello;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaVantageDayValue {

    @JsonProperty(value = "1. open")
	private String open;
    @JsonProperty(value = "2. high")
	private String high;
    @JsonProperty(value = "3. low")
	private String low;
    @JsonProperty(value = "4. close")
	private String close;
    @JsonProperty(value = "5. volume")
	private String volume;
	
	public AlphaVantageDayValue() {
		// TODO Auto-generated constructor stub
	}

	public String getOpen() {
		return open;
	}

	public String getHigh() {
		return high;
	}

	public String getLow() {
		return low;
	}

	public String getClose() {
		return close;
	}

	public String getVolume() {
		return volume;
	}

}
