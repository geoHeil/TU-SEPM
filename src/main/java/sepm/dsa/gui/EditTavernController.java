package sepm.dsa.gui;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sepm.dsa.application.SpringFxmlLoader;
import sepm.dsa.model.Location;
import sepm.dsa.model.Tavern;
import sepm.dsa.service.LocationService;
import sepm.dsa.service.TavernService;

import java.util.List;

public class EditTavernController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(EditTavernController.class);
    private SpringFxmlLoader loader;

    private Tavern selectedTavern;
    private TavernService tavernService;
    private LocationService locationService;

    private boolean isNewTavern;

    @FXML
    private TextField nameField;
    @FXML
    private ChoiceBox<Location> locationBox;
    @FXML
    private TextArea commentArea;
    @FXML
    private TextField usageField;
    @FXML
    private TextField bedsField;
    @FXML
    private TextField xCoordField;
    @FXML
    private TextField yCoordField;


    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        log.debug("initialise EditTavernController");


        List<Location> locations = locationService.getAll();
        locationBox.setItems(FXCollections.observableArrayList(locations));

    }

    @FXML
    private void onSavePressed() {
        log.debug("called onSavePressed");

        log.debug("calling SaveButtonPressed");

        // save region
        String name = nameField.getText();
        selectedTavern.setName(name);
        selectedTavern.setLocation(locationBox.getSelectionModel().getSelectedItem());
        selectedTavern.setUsage(Integer.parseInt(usageField.getText()));
        //selectedTaver.setBeds(Integer.parseInt(bedField.getText()));
        selectedTavern.setxPos(Integer.parseInt(xCoordField.getText()));
        selectedTavern.setyPos(Integer.parseInt(yCoordField.getText()));
        //selectedTavern.setComment(commentArea.getText());

        /*
        if (isNewTavern) {
            tavernService.add(selectedTavern);
        } else {
            tavernService.update(selectedTavern);
        }
        */

        Stage stage = (Stage) nameField.getScene().getWindow();
        Parent scene = (Parent) loader.load("/gui/tavernlist.fxml");
        stage.setScene(new Scene(scene, 600, 400));


    }

    @FXML
    private void onCancelPressed() {
        log.debug("called onCancelPressed");

        Stage stage = (Stage) nameField.getScene().getWindow();
        Parent scene = (Parent) loader.load("/gui/tavernlist.fxml");
        stage.setScene(new Scene(scene, 600, 400));
    }

    public void setTavernService(TavernService tavernService) {
        log.debug("calling setTavernService(" + tavernService + ")");
        this.tavernService = tavernService;
    }

    public void setLocationService(LocationService locationService) {
        log.debug("calling setLocationService(" + locationService + ")");
        this.locationService = locationService;
    }

    public void setTavern(Tavern tavern) {
        this.selectedTavern = tavern;
    }

    public void setLoader(SpringFxmlLoader loader) {
        this.loader = loader;
    }
}
