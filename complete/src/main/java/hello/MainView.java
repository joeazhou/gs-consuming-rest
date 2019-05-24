package hello;

import com.vaadin.flow.component.UI;
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
	private final Week2ChangeRepository repo2week;

	final Grid<StockWeekChange> grid;
	final Grid<Week2Change> week2grid;

	final TextField filter;

	private final Button addNewBtn;

	private HorizontalLayout layout ;
	
	public MainView(StockWeekChangeRepository repo, Week2ChangeRepository repo2week) {
		this.repo = repo;
		this.repo2week = repo2week;
		
		this.grid = new Grid<>();
		this.week2grid = new Grid<>();
		this.filter = new TextField();
		this.addNewBtn = new Button("New StockWeekChange", VaadinIcon.PLUS.create());
//		 UI.getCurrent().getPage().reload();
		 UI.getCurrent().setPollInterval( ( int )  30000 );
		
		layout = new HorizontalLayout();
		layout.getStyle().set("border", "1px solid #9E9E9E");
		layout.setWidth("100%");

		layout.add(grid, week2grid);
		
		add(layout);

		grid.setColumnReorderingAllowed(true);
		grid.setWidth("650px");
		grid.setHeight("500px");
				
		// NumberRenderer to render numbers in general
		grid.addColumn(TemplateRenderer.of("[[index]]")).setHeader("#").setWidth("5px");
		grid.addColumn(StockWeekChange::getDay).setHeader("Date").setWidth("80px");
		grid.addColumn(StockWeekChange::getStockId).setHeader("Stock").setWidth("50px");
		grid.addColumn(StockWeekChange::getClose).setHeader("Close").setWidth("50px");
		grid.addColumn(StockWeekChange::getWeek4change).setHeader("4Weeks").setWidth("50px"); 
		grid.addColumn(TemplateRenderer.<StockWeekChange> of(
		        "[[item.week4change]]%")
//		        .withProperty("week4change", StockWeekChange -> StockWeekChange.getWeek4change()),
		        .withProperty("week4change", StockWeekChange -> StockWeekChange.getWeek4change().movePointRight(2)),
		        "week4change").setHeader("4w-rate");
		
		week2grid.setColumnReorderingAllowed(true);
		week2grid.setWidth("650px");
		week2grid.setHeight("500px");

		week2grid.addColumn(TemplateRenderer.of("[[index]]")).setHeader("#").setWidth("5px");
		week2grid.addColumn(Week2Change::getDay).setHeader("Date").setWidth("80px");
		week2grid.addColumn(Week2Change::getStockId).setHeader("Stock").setWidth("50px");
		week2grid.addColumn(Week2Change::getClose).setHeader("Close").setWidth("50px");
		week2grid.addColumn(Week2Change::getWeek2change).setHeader("2Weeks").setWidth("50px"); 
		week2grid.addColumn(TemplateRenderer.<Week2Change> of(
		        "[[item.week2change]]%")
//		        .withProperty("week4change", StockWeekChange -> StockWeekChange.getWeek4change()),
		        .withProperty("week2change", Week2Change -> Week2Change.getWeek2change().movePointRight(2)),
		        "week2change").setHeader("2w-rate");
		listData();
	}

	public void listData() {
		grid.setItems(repo.findAll());
		week2grid.setItems(repo2week.findAll());
	}
}
