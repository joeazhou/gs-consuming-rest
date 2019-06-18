package hello;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaVantageMetaValue {

    @JsonProperty(value = "1. Information")
	private String information;
    @JsonProperty(value = "2. Symbol")
	private String symbol;
    @JsonProperty(value = "3. Last Refreshed")
	private String lastRefreshed;
    @JsonProperty(value = "4. Output Size")
	private String outputSize;
    @JsonProperty(value = "5. Time Zone")
	private String timeZone;
	
	public AlphaVantageMetaValue() {
	}

	public String getInformation() {
		return information;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getLastRefreshed() {
		return lastRefreshed;
	}

	public String getOutputSize() {
		return outputSize;
	}

	public String getTimeZone() {
		return timeZone;
	}

}
