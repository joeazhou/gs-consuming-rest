package hello;


import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity 
@Table(name = "stockweekrecord")
@IdClass(MyKey.class)
public class StockWeekRecord extends MyKey{
	private static final long serialVersionUID = -3405970650225408913L;
	@Id
	@EmbeddedId
	private MyKey myKey;
	
//	@Id
//	@Column(name = "stockId")
//	private String stockId;
//
//	@Id
//	@Column(name = "day")
//	private String day;
	
    @Column(name = "open") 
    private java.math.BigDecimal open;
    

    @Column(name = "close") 
    private java.math.BigDecimal close;
    

    @Column(name = "low") 
    private java.math.BigDecimal low;

    @Column(name = "high") 
    private java.math.BigDecimal high;

    @Column(name = "volume") 
    private java.math.BigDecimal volume;

    public StockWeekRecord() {    	
    }

    public StockWeekRecord(MyKey mykey, BigDecimal open, BigDecimal close, BigDecimal low, BigDecimal high, BigDecimal volume ) {
    	this.myKey = mykey;
//    	this.day = day;
    	this.open = open;
    	this.close = close;
    	this.low = low;
    	this.high = high;
    	this.volume = volume;
    }

//	public String getStockId() {
//		return stockId;
//	}
//
//	public void setStockId(String stockId) {
//		this.stockId = stockId;
//	}
//
//	public String getDay() {
//		return day;
//	}
//
//	public void setDay(String day) {
//		this.day = day;
//	}
    
	public java.math.BigDecimal getOpen() {
		return open;
	}

	public void setOpen(java.math.BigDecimal open) {
		this.open = open;
	}

	public java.math.BigDecimal getClose() {
		return close;
	}

	public void setClose(java.math.BigDecimal close) {
		this.close = close;
	}

	public java.math.BigDecimal getLow() {
		return low;
	}

	public void setLow(java.math.BigDecimal low) {
		this.low = low;
	}

	public java.math.BigDecimal getHigh() {
		return high;
	}

	public void setHigh(java.math.BigDecimal high) {
		this.high = high;
	}

	public java.math.BigDecimal getVolume() {
		return volume;
	}

	public void setVolume(java.math.BigDecimal volume) {
		this.volume = volume;
	}
    

    /** Your getters and setters **/
    
}
