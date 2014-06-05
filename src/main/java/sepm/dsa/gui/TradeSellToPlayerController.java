package sepm.dsa.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sepm.dsa.exceptions.DSAValidationException;
import sepm.dsa.model.*;
import sepm.dsa.service.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class TradeSellToPlayerController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(TradeSellToPlayerController.class);

    @FXML
    private Label selectedOffer;
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

    private static Trader trader;
    private static Offer offer;
    private TraderService traderService;
    private SaveCancelService saveCancelService;
    private UnitService unitService;
    private CurrencyService currencyService;
    private TimeService timeService;
    private PlayerService playerService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //TODO set nice names
        selectedCurrency.setItems(FXCollections.observableArrayList(currencyService.getAll()));
        selectedUnit.setItems(FXCollections.observableArrayList(unitService.getAll()));
        selectedPrice.setText(offer.getPricePerUnit().toString());
        selectedPlayer.setItems(FXCollections.observableArrayList(playerService.getAll()));

        //select default unit & currency

//        selectedCurrency.getSelectionModel().select(); TODO set preferred currency
//        selectedUnit.getSelectionModel().select(offer.getUnit); TODO set preferred unit
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
        int amount = 0;
        try {
            amount = new Integer(selectedAmount.getText());

        } catch (NumberFormatException ex) {
            throw new DSAValidationException("Menge muss eine ganze Zahl sein!");
        }

        //Calculate Price
        BigDecimal price = new BigDecimal(0); //TODO

        //Unit convert TODO

        //Decerement stock at trader TODO

        Deal newDeal = new Deal();
        newDeal.setAmount(amount);
        newDeal.setDate(timeService.getCurrentDate());
        newDeal.setLocationName(trader.getLocation().getName()); //TODO location vs. location NAME
        newDeal.setPlayer(selectedPlayer.getSelectionModel().getSelectedItem());
        newDeal.setPrice(price);
//        newDeal.setProduct(); //TODO check if @deprecated
        newDeal.setProductName(offer.getProduct().getName());//TODO user PRODUCT instead of name?? DEL??
        newDeal.setPurchase(true);
        newDeal.setquality(offer.getQuality());
        newDeal.setTrader(trader);
        newDeal.setUnit(selectedUnit.getSelectionModel().getSelectedItem());
//        newDeal.setCurreny(selectedCurrency.getSelectionModel().getSelectedItem()); TODO implement
//        saveCancelService.save();

        Stage stage = (Stage) selectedUnit.getScene().getWindow();
        stage.close();
    }

    public static void setTrader(Trader trader) {
        TradeSellToPlayerController.trader = trader;
    }

    public static void setOffer(Offer offer) {
        TradeSellToPlayerController.offer = offer;
    }

    public void setTraderService(TraderService traderService) {
        this.traderService = traderService;
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

    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }
}
