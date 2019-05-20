package hello;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

@Route
public class MainView extends VerticalLayout {

	private final StockWeekChangeRepository repo;

	final Grid<StockWeekChange> grid;

	final TextField filter;

	private final Button addNewBtn;

	public MainView(StockWeekChangeRepository repo) {
		this.repo = repo;
		this.grid = new Grid<>(StockWeekChange.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New StockWeekChange", VaadinIcon.PLUS.create());
  
		add(grid);

		grid.setColumnReorderingAllowed(true);
//		grid.setColumns("stockId", "day", "close", "week4change", "week2change");
		
		listData();
	}

	public void listData() {
		List<StockWeekChange> swbyid = repo.findByDay("2019-05-20");
		grid.setItems(repo.findByDay("2019-05-20"));
//		grid.setItems(repo.findAll());

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
