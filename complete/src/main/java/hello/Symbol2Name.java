package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Symbol2Name
{
    private String symbol;

    private String[][] data;

    private String name;

    public String getSymbol ()
    {
        return symbol;
    }

    public void setSymbol (String symbol)
    {
        this.symbol = symbol;
    }

    public String[][] getData ()
    {
        return data;
    }

    public void setData (String[][] data)
    {
        this.data = data;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Symbol2Name [symbol = "+symbol+", name = "+name+"]";
    }
}