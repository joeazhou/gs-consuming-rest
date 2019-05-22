package hello;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.util.StringUtils;

@Route
public class MainView extends VerticalLayout {

	private final StockWeekChangeRepository repo;

	final Grid<StockWeekChange> grid;

	final TextField filter;

	private final Button addNewBtn;

	public MainView(StockWeekChangeRepository repo) {
		this.repo = repo;
		this.grid = new Grid<>();
		this.filter = new TextField();
		this.addNewBtn = new Button("New StockWeekChange", VaadinIcon.PLUS.create());
  
		add(grid);

		grid.setColumnReorderingAllowed(true);
		grid.setWidth("650px");
		grid.setHeight("700px");
//		grid.removeColumnByKey("id");
//		grid.removeColumnByKey("Flag1week");
//		grid.removeColumnByKey("Flag2week");
//		grid.removeColumnByKey("Flag4week");
//		grid.removeColumnByKey("Flag2buy");
//		grid.setColumns("day", "stockId", "close");
//		grid.getColumnByKey("day").setHeader("day").setWidth("90px");
//		grid.getColumnByKey("stockId").setHeader("Name").setWidth("60px");
//		grid.getColumnByKey("close").setHeader("close").setWidth("50px");
		
		DecimalFormat df = new DecimalFormat("0.00");
		NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.US);
		
		// NumberRenderer to render numbers in general
		grid.addColumn(TemplateRenderer.of("[[index]]")).setHeader("#").setWidth("5px");
		grid.addColumn(StockWeekChange::getDay).setHeader("Date").setWidth("80px");
		grid.addColumn(StockWeekChange::getStockId).setHeader("Stock").setWidth("50px");
		grid.addColumn(StockWeekChange::getClose).setHeader("Close").setWidth("50px");
		grid.addColumn(StockWeekChange::getWeek4change).setHeader("4Weeks").setWidth("50px");
		grid.addColumn(new NumberRenderer<>(StockWeekChange::getWeek2change, df)).setHeader("2Weeks").setWidth("50px");
		grid.addColumn(TemplateRenderer.<StockWeekChange> of(
		        "[[item.week4change]]%")
//		        .withProperty("week4change", StockWeekChange -> StockWeekChange.getWeek4change()),
		        .withProperty("week4change", StockWeekChange -> StockWeekChange.getWeek4change().movePointRight(2)),
		        "week4change").setHeader("4w-rate");
		
		for (Column column : grid.getColumns()) {
		    column.setSortable(true);
		    System.out.println(column.getKey() + " is sortable? " + column.isSortable());
		}
//		grid.getColumnByKey("week4change").setHeader("4Weeks").setWidth("60px");
//		grid.getColumnByKey("week2change").setHeader("2Weeks").setWidth("60px");
		
//		grid.addColumn(StockWeekChange::getDay).setHeader("Day").setWidth("50px");
//		grid.addColumn(StockWeekChange::getStockId).setHeader("Stock").setWidth("50px");
//		grid.addColumn(StockWeekChange::getClose).setHeader("Close").setWidth("150px");
//		grid.addColumn(StockWeekChange::getWeek4change).setHeader("Week4change").setWidth("150px");
//		grid.addColumn(StockWeekChange::getWeek2change).setHeader("getWeek2change").setWidth("150px");
		listData();
	}

	public void listData() {
		List<StockWeekChange> swbyid = repo.findByDay("2019-05-20");
//		grid.setItems(repo.findByDay("2019-05-20"));
		grid.setItems(repo.findAll());

		List<StockWeekChange> newlist = new ArrayList<StockWeekChange>();
		System.out.println("Printing from mainView " + swbyid.size());
		for (StockWeekChange oneinst : swbyid) {
			newlist.add(oneinst);
//			grid.(oneinst);
			 System.out.println(oneinst.getStockId() + " " + oneinst.getDay() + 
					 " close:" + oneinst.getClose() +
					 " 4 week: " + oneinst.getWeek4change()+
					 " 2 week: " + oneinst.getWeek2change());
		} 
//		grid.setItems(newlist);
//		for (StockWeekRecord oneinst : swbyid) {
			
//			 System.out.println(oneinst.getStockId() + " " + oneinst.getDay() + 
//					 " close:" + oneinst.getClose() +
//					 " 4 week: " + oneinst.getWeek4change()+
//					 " 2 week: " + oneinst.getWeek2change());
//		}
	}
}
