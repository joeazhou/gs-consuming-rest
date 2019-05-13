package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OneWeekRecord
{
    private String volume;

    private String high;

    private String low;

    private String day;

    private String close;

    private String open;

    public String getVolume ()
    {
        return volume;
    }

    public void setVolume (String volume)
    {
        this.volume = volume;
    }

    public String getHigh ()
    {
        return high;
    }

    public void setHigh (String high)
    {
        this.high = high;
    }

    public String getLow ()
    {
        return low;
    }

    public void setLow (String low)
    {
        this.low = low;
    }

    public String getDay ()
    {
        return day;
    }

    public void setDay (String day)
    {
        this.day = day;
    }

    public String getClose ()
    {
        return close;
    }

    public void setClose (String close)
    {
        this.close = close;
    }

    public String getOpen ()
    {
        return open;
    }

    public void setOpen (String open)
    {
        this.open = open;
    }

    @Override
    public String toString()
    {
        return "OneWeekRecord [day = "+day+", close = "+close+", low = "+low+", volume = "+volume+", high = "+high+", open = "+open+"]";
    }
}
			