package hello;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity 
@Table(name = "UsStockDailyRecord")
@IdClass(MyKey.class)
public class UsStockDailyRecord extends MyKey{
	private static final long serialVersionUID = -34059750225408913L;
	@Id
	@EmbeddedId
	private MyKey myKey;
	
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

    @Column(name = "week1change") 
    private java.math.BigDecimal week1change;

    @Column(name = "week2change") 
    private java.math.BigDecimal week2change;

    @Column(name = "week4change") 
    private java.math.BigDecimal week4change;
    
    public UsStockDailyRecord() {
    }

    public UsStockDailyRecord(MyKey mykey, BigDecimal open, BigDecimal close, BigDecimal low, BigDecimal high, BigDecimal volume ) {
    	this.myKey = mykey;
    	this.open = open;
    	this.close = close;
    	this.low = low;
    	this.high = high;
    	this.volume = volume;
    }

    public UsStockDailyRecord(MyKey mykey, BigDecimal open, BigDecimal close, BigDecimal low, BigDecimal high, BigDecimal volume, BigDecimal week1change, BigDecimal week2change, BigDecimal week4change  ) {
    	this.myKey = mykey;
    	this.open = open;
    	this.close = close;
    	this.low = low;
    	this.high = high;
    	this.volume = volume;
    	this.week1change = week1change;
    	this.week2change = week2change;
    	this.week4change = week4change;
    }

    public String getDay() {
    	return myKey.getDay();
    }

    public String getStockId() {
    	return myKey.getStockId();
    }
    
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

	public java.math.BigDecimal getWeek1change() {
		return week1change;
	}

	public void setWeek1change(java.math.BigDecimal week1change) {
		this.week1change = week1change;
	}

	public java.math.BigDecimal getWeek2change() {
		return week2change;
	}

	public void setWeek2change(java.math.BigDecimal week2change) {
		this.week2change = week2change;
	}

	public java.math.BigDecimal getWeek4change() {
		return week4change;
	}

	public void setWeek4change(java.math.BigDecimal week4change) {
		this.week4change = week4change;
	}
}
