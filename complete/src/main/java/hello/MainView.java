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

import java.util.List;

import org.springframework.util.StringUtils;

@Route
public class MainView extends VerticalLayout {

	private final StockWeekRecordRepository repo;

	final Grid<StockWeekRecord> grid;

	final TextField filter;

	private final Button addNewBtn;

	public MainView(StockWeekRecordRepository repo) {
		this.repo = repo;
		this.grid = new Grid<>(StockWeekRecord.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New StockWeekRecord", VaadinIcon.PLUS.create());
  
		add(grid);
		listData();
	}

	private void listData() {
		List<StockWeekRecord> swbyid = repo.findByMyKeyDay("2019-05-17");
		grid.setItems(swbyid);
		System.out.println(swbyid.size());
		for (StockWeekRecord oneinst : swbyid) {
			 System.out.println(oneinst.getStockId() + " " + oneinst.getDay() + 
					 " close:" + oneinst.getClose() +
					 " 4 week: " + oneinst.getWeek4change()+
					 " 2 week: " + oneinst.getWeek2change());
		}
//		for (StockWeekRecord oneinst : swbyid) {
			
//			 System.out.println(oneinst.getStockId() + " " + oneinst.getDay() + 
//					 " close:" + oneinst.getClose() +
//					 " 4 week: " + oneinst.getWeek4change()+
//					 " 2 week: " + oneinst.getWeek2change());
//		}
	}
}
