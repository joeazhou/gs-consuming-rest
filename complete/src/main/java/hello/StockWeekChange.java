package hello;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
              
public class StockWeekChange {

    @Id
    public String id;
    
    public String stockId;
    public String day;
    public BigDecimal close;
    public BigDecimal week4change;
    public BigDecimal week2change;
    public BigDecimal week1change;
    public String flag4week;
    public String flag2week;
    public String flag1week;
    public String flag2buy;
    
	public String getFlag4week() {
		return flag4week;
	}

	public void setFlag4week(String flag4week) {
		this.flag4week = flag4week;
	}

	public String getFlag2week() {
		return flag2week;
	}

	public void setFlag2week(String flag2week) {
		this.flag2week = flag2week;
	}

	public String getFlag1week() {
		return flag1week;
	}

	public void setFlag1week(String flag1week) {
		this.flag1week = flag1week;
	}

	public String getFlag2buy() {
		return flag2buy;
	}

	public void setFlag2buy(String flag2buy) {
		this.flag2buy = flag2buy;
	}

	public StockWeekChange() {
	}

	public StockWeekChange(String stockId, String day, BigDecimal close, BigDecimal week4change, BigDecimal week2change,
			BigDecimal week1change) {
		super();
		this.id = stockId + " " + day;
		this.stockId = stockId;
		this.day = day;
		this.close = close;
		this.week4change = week4change;
		this.week2change = week2change;
		this.week1change = week1change;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getWeek4change() {
		return week4change;
	}

	public void setWeek4change(BigDecimal week4change) {
		this.week4change = week4change;
	}

	public BigDecimal getWeek2change() {
		return week2change;
	}

	public void setWeek2change(BigDecimal week2change) {
		this.week2change = week2change;
	}

	public BigDecimal getWeek1change() {
		return week1change;
	}

	public void setWeek1change(BigDecimal week1change) {
		this.week1change = week1change;
	}

	@Override
	public String toString() {
		return "StockWeekChange [id=" + id + ", stockId=" + stockId + ", day=" + day + ", close=" + close
				+ ", week4change=" + week4change + ", week2change=" + week2change + ", week1change=" + week1change
				+ "]";
	}

}
