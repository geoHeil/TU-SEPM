package sepm.dsa.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sepm.dsa.exceptions.DSAValidationException;
import sepm.dsa.model.*;
import sepm.dsa.service.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class TradeBuyFromPlayerController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(TradeBuyFromPlayerController.class);

    private PlayerService playerService;
    private TraderService traderService;
    private UnitService unitService;
    private CurrencyService currencyService;
    private TimeService timeService;
    private SaveCancelService saveCancelService;

    private static Trader trader;

    @FXML
    private ChoiceBox<Unit> selectedUnit;
    @FXML
    private ChoiceBox<Player> selectedPlayer;
    @FXML
    private TextField selectedAmount;
    @FXML
    private ChoiceBox<Currency> selectedCurrency;
    @FXML
    private TextField selectedPrice;
    @FXML
    private TableView<Deal> dealsTable;
    @FXML
    private TableColumn<Deal, String> pricecolumn;
    @FXML
    private TableColumn<Deal, String> productColumn;
    @FXML
    private TableColumn<Deal, String> amountColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("initialize TradeBuyFromPlayerController");
        //initialize table
        initialzeTableWithColums();

        //TODO set nice names
        selectedCurrency.setItems(FXCollections.observableArrayList(currencyService.getAll()));
        selectedUnit.setItems(FXCollections.observableArrayList(unitService.getAll()));
        selectedPlayer.setItems(FXCollections.observableArrayList(playerService.getAll()));

        selectedPlayer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> dealsTable.setItems(FXCollections.observableArrayList(newValue.getDeals())));
    }

    //TODO need some on selection changed

    @FXML
    private void checkFocus() {
        Deal selectedDeal = dealsTable.getSelectionModel().getSelectedItem();
        if (selectedDeal != null) {
            selectedAmount.setText(selectedDeal.getAmount().toString());
//        selectedCurrency.getSelectionModel().select(selectedDeal.getCurrency());
            selectedPrice.setText(selectedDeal.getPrice().toPlainString());
            selectedUnit.getSelectionModel().select(selectedDeal.getUnit());
        }
    }

    @FXML
    private void onCancelPressed() {
        log.debug("CancelButtonPressed");

        Stage stage = (Stage) selectedUnit.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onSavePressed() {
        log.debug("calling SaveButtonPressed");

        if (selectedAmount.getText().isEmpty()) {
            throw new DSAValidationException("Bitte Menge eingeben");
        }
        int amount;
        try {
            amount = new Integer(selectedAmount.getText());

        } catch (NumberFormatException ex) {
            throw new DSAValidationException("Menge muss eine ganze Zahl sein!");
        }

        //Calculate Price
        BigDecimal price = new BigDecimal(0); //TODO


        //Unit convert TODO

        //Decerement stock at player + increment at trader TODO

        Deal newDeal = new Deal();
        newDeal.setAmount(amount);
        newDeal.setDate(timeService.getCurrentDate());
        newDeal.setLocationName(trader.getLocation().getName()); //TODO location vs. location NAME
        newDeal.setPlayer(selectedPlayer.getSelectionModel().getSelectedItem());
        newDeal.setPrice(price);
//        newDeal.setProduct(); //TODO check if @deprecated
        newDeal.setProductName(dealsTable.getSelectionModel().getSelectedItem().getProductName());//TODO user PRODUCT instead of name?? DEL??
        newDeal.setPurchase(false);
        newDeal.setquality(dealsTable.getSelectionModel().getSelectedItem().getQuality());
        newDeal.setTrader(trader);
        newDeal.setUnit(selectedUnit.getSelectionModel().getSelectedItem());
//        newDeal.setCurreny(selectedCurrency.getSelectionModel().getSelectedItem()); TODO implement
//        saveCancelService.save();

        Stage stage = (Stage) selectedUnit.getScene().getWindow();
        stage.close();
    }

    private void initialzeTableWithColums() {

        pricecolumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Deal, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Deal, String> d) {
                if (d.getValue() != null) {
                    Deal deal = d.getValue();
                    StringBuilder sb = new StringBuilder();
                    sb.append(deal.getProductName());
                    if (deal.getQuality()!= null) {
                        sb.append(" (" + d.getValue().getQuality().getName() + ")");
                    }
                    return new SimpleStringProperty(sb.toString());
                } else {
                    return new SimpleStringProperty("");
                }
            }
        });

        amountColumn.setCellValueFactory(d -> {
            Unit unit = d.getValue().getUnit();
            Integer amount = d.getValue().getAmount();

            return new SimpleStringProperty(String.valueOf(amount) + " " + unit.getShortName());
        });
    }

    public static void setTrader(Trader trader) {
        TradeBuyFromPlayerController.trader = trader;
    }

    public void setTraderService(TraderService traderService) {
        this.traderService = traderService;
    }

    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    public void setSaveCancelService(SaveCancelServiceImpl saveCancelService) {
        this.saveCancelService = saveCancelService;
    }

    public void setUnitService(UnitService unitService) {
        this.unitService = unitService;
    }

    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }
}
