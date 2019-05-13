package hello;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MyKey implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "stockid")
	private String stockId;
	
	@Column(name = "day")
	private String day;

	public MyKey(String stockId, String day) {
		this.stockId = stockId;
		this.day = day;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((stockId == null) ? 0 : stockId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyKey other = (MyKey) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equalsIgnoreCase(other.day))
			return false;
		if (stockId == null) {
			if (other.stockId != null)
				return false;
		} else if (!stockId.equalsIgnoreCase(other.stockId))
			return false;
		return true;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public MyKey() {
	}
	
	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

	@Override
	public String toString() {
		return "MyKey [stockId=" + stockId + ", day=" + day + "]";
	}

}