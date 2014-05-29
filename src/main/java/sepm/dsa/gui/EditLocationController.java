package sepm.dsa.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sepm.dsa.application.SpringFxmlLoader;
import sepm.dsa.exceptions.DSAValidationException;
import sepm.dsa.model.*;
import sepm.dsa.service.LocationService;
import sepm.dsa.service.RegionService;
import sepm.dsa.service.SaveCancelService;

import java.io.File;
import java.util.*;

@Service("EditLocationController")
public class EditLocationController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(EditLocationController.class);
    private SpringFxmlLoader loader;

    private static Location selectedLocation;

    private LocationService locationService;
    private RegionService regionService;
    private SaveCancelService saveCancelService;
    // true if the location is not editing
    private boolean isNewLocation;

    @FXML
    private TextField nameField;
    @FXML
    private ChoiceBox weatherChoiceBox;
    @FXML
    private Button mapCoordSelection;
    @FXML
    private ChoiceBox sizeChoiceBox;
    @FXML
    private ChoiceBox regionChoiceBox;
    @FXML
    private TextField xCoord;
    @FXML
    private TextField yCoord;
    @FXML
    private TextField height;
    @FXML
    private TextArea commentArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button removeBorderButton;
    //map file name
    private String backgroundMapName = "";

    @FXML
    private TableView<LocationConnection> locationConnectionsTable;

    @FXML
    private TableColumn<LocationConnection, String> connectionToColumn;
//    @FXML
//    private TableColumn<LocationConnection, Location> location2Column;
    @FXML
    private TableColumn<LocationConnection, Integer> travelTimeColumn;

    @FXML
    private Button editConnectionsBtn;
//    @FXML
//    private Button suggestConnectionsBtn;
//    @FXML
//    private Button addConnectionBtn;
//    @FXML
//    private Button removeConnectionBtn;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        log.debug("initialise EditRegionController");

        // init ChoiceBoxes
        List<String> sizeList = new ArrayList<>();
        for (TownSize t : TownSize.values()) {
            sizeList.add(t.getName());
        }
        List<String> weatherList = new ArrayList<>();
        for (Weather w : Weather.values()) {
            weatherList.add(w.getName());
        }
        weatherChoiceBox.setItems(FXCollections.observableArrayList(weatherList));
        sizeChoiceBox.setItems(FXCollections.observableArrayList(sizeList));

        // set values if editing
        if (selectedLocation != null) {
            isNewLocation = false;
            nameField.setText(selectedLocation.getName());
            weatherChoiceBox.getSelectionModel().select(selectedLocation.getWeather().getValue());
            sizeChoiceBox.getSelectionModel().select(selectedLocation.getSize().getValue());
            commentArea.setText(selectedLocation.getComment());
            xCoord.setText(selectedLocation.getxCoord().toString());
            yCoord.setText(selectedLocation.getyCoord().toString());
            height.setText(selectedLocation.getHeight().toString());
            regionChoiceBox.getSelectionModel().select(selectedLocation.getRegion());
        } else {
            isNewLocation = true;
            selectedLocation = new Location();
            weatherChoiceBox.getSelectionModel().select(Temperature.MEDIUM.getValue());
            sizeChoiceBox.getSelectionModel().select(RainfallChance.MEDIUM.getValue());
        }

        // init region choice box
        List<Region> otherRegions = regionService.getAll();
//        otherRegions.removeConnection(selectedLocation.getRegion());
        regionChoiceBox.setItems(FXCollections.observableArrayList(otherRegions));

        travelTimeColumn.setCellValueFactory(new PropertyValueFactory<>("travelTime"));
//        location1Column.setCellValueFactory(new PropertyValueFactory<>("location1"));
//        location2Column.setCellValueFactory(new PropertyValueFactory<>("location2"));
//        location1Column.setCellFactory(new Callback<TableColumn<LocationConnection, Location>, TableCell<LocationConnection, Location>>() {
//            @Override
//            public TableCell<LocationConnection, Location> call(TableColumn<LocationConnection, Location> locationConnectionLocationTableColumn) {
//                return new TableCell<LocationConnection, Location>() {
//
//                    @Override
//                    protected void updateItem(Location item, boolean empty) {
//                        super.updateItem(item, empty);
//
//                        if (!empty) {
//                            if (item != null) {
//                                setText(item.getName());
//                                if (selectedLocation.equals(item)) {
//                                    //
//                                }
//                            } else {
//                                setText("<null>");
//                            }
//                        } else {
//                            setText(null);
//                        }
//                    }
//
//                };
//            }
//        });
        connectionToColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<LocationConnection, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<LocationConnection, String> r) {
                if (r.getValue() != null) {
                    String connectedToString = r.getValue().connectedTo(selectedLocation).getName();
                    return new SimpleStringProperty(connectedToString);
                } else {
                    return new SimpleStringProperty("");
                }
            }
        });

        Set<LocationConnection> allConnections = selectedLocation.getAllConnections();
        ObservableList<LocationConnection> connections = FXCollections.observableArrayList(allConnections);
        locationConnectionsTable.setItems(connections);

    }

    public void setLocationService(LocationService locationService) {
        log.debug("calling setLocationService(" + locationService + ")");
        this.locationService = locationService;
    }

    public void setRegionService(RegionService regionService) {
        log.debug("calling setRegionService(" + regionService + ")");
        this.regionService = regionService;
    }

    @FXML
    private void onCancelPressed() {
        log.debug("CancelButtonPressed");
        saveCancelService.cancel();
        saveCancelService.refresh(selectedLocation);
        log.info("before: connections.size=" + selectedLocation.getAllConnections().size());
        selectedLocation = locationService.get(selectedLocation.getId());
        log.info("after: connections.size=" + selectedLocation.getAllConnections().size());

        Stage stage = (Stage) nameField.getScene().getWindow();
        Parent scene = (Parent) loader.load("/gui/locationlist.fxml");

        stage.setScene(new Scene(scene, 600, 438));
    }

    private void applyLocationChanges() {
        // save region
        String name = nameField.getText();
        Weather weather = Weather.parse(weatherChoiceBox.getSelectionModel().getSelectedIndex());
        TownSize townSize = TownSize.parse(sizeChoiceBox.getSelectionModel().getSelectedIndex());
        String comment = commentArea.getText();
        Region seletcedRegionForLocation = (Region) regionChoiceBox.getSelectionModel().getSelectedItem();
        if (seletcedRegionForLocation == null) {
            throw new DSAValidationException("Wählen sie ein Gebiet aus");
        }
        selectedLocation.setPlanFileName(backgroundMapName);
        selectedLocation.setName(name);
        selectedLocation.setComment(comment);
        selectedLocation.setWeather(weather);
        selectedLocation.setSize(townSize);
        selectedLocation.setRegion(seletcedRegionForLocation);
        try {
            selectedLocation.setxCoord(Integer.parseInt(xCoord.getText()));
        } catch (NumberFormatException e) {
            throw new DSAValidationException("xCoord muss eine Zahl sein.");
        }
        try {
            selectedLocation.setyCoord(Integer.parseInt(yCoord.getText()));
        } catch (NumberFormatException e) {
            throw new DSAValidationException("yCoord muss eine Zahl sein.");
        }
        try {
            selectedLocation.setHeight(Integer.parseInt(height.getText()));
        } catch (NumberFormatException e) {
            throw new DSAValidationException("Höhe muss eine Zahl sein.");
        }

        log.info("connections now in selected Location");
        for (LocationConnection con : selectedLocation.getAllConnections()) {
            log.info("location: " + con);
        }

        if (isNewLocation) {
            log.info("addConnection location");
            locationService.add(selectedLocation);
        } else {
            log.info("update location");
            locationService.update(selectedLocation);
        }

        log.info("selectedLocation.id = " + selectedLocation.getId());
//        selectedLocation = locationService.get(selectedLocation.getId());

    }

    @FXML
    private void onSavePressed() {
        log.debug("calling SaveButtonPressed");

        applyLocationChanges();

        saveCancelService.save();
//        locationService.update(selectedLocation);


        // return to locationlist
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        Parent scene = (Parent) loader.load("/gui/locationlist.fxml");


        //TODO ist das so gut immer eine NEUE scene zu öffnen?
        stage.setScene(new Scene(scene, 600, 438));
    }

    @FXML
    public void chooseBackground() {
        log.info("Select Backgroundimage Location");
//choose File to export
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ort Karte wählen");
        List<String> extensions = new ArrayList<String>();
        extensions.add("*.jpg");
        extensions.add("*.png");
        extensions.add("*.gif");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", extensions),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );

        //look for location map
        File newlocationMap = fileChooser.showOpenDialog(new Stage());

        if (newlocationMap == null) {
            return;
        }
        this.backgroundMapName = newlocationMap.getAbsolutePath();
    }


    public static void setLocation(Location location) {
        log.debug("calling setLocation(" + location + ")");
        selectedLocation = location;
    }

    public void setLoader(SpringFxmlLoader loader) {
        this.loader = loader;
    }

    @FXML
    public void onEditConnectionsClicked() {
        log.debug("calling onEditConnectionsClicked");

        applyLocationChanges();

        EditLocationConnectionsController.setSelectedLocation(selectedLocation);

        Stage stage = (Stage) locationConnectionsTable.getScene().getWindow();
        Parent root = (Parent) loader.load("/gui/editlocationconnections.fxml");

        stage.setTitle("Reiseverbindungen für Ort '" + selectedLocation.getName() + "' bearbeiten");
        stage.setScene(new Scene(root, 900, 500));
        stage.show();
    }

    public void setSaveCancelService(SaveCancelService saveCancelService) {
        this.saveCancelService = saveCancelService;
    }

//    @FXML
//    public void onSuggestConnectionsBtnClicked() {
//        try {
//            selectedLocation.setxCoord(Integer.parseInt(xCoord.getText()));
//        } catch (NumberFormatException e) {
//            throw new DSAValidationException("xCoord muss eine Zahl sein.");
//        }
//        try {
//            selectedLocation.setyCoord(Integer.parseInt(yCoord.getText()));
//        } catch (NumberFormatException e) {
//            throw new DSAValidationException("yCoord muss eine Zahl sein.");
//        }
//        List<LocationConnection> suggestedConnections = locationService.suggestLocationConnectionsAround(selectedLocation, 100.0);
//
////        if (selectedLocation.getId() != null) {
////            Location reloaded = locationService.get(selectedLocation.getId());
////            if (reloaded != null) {
////                suggestedConnections.addAll(reloaded.getAllConnections());
////            }
////        }
////        ObservableList<LocationConnection> connections = FXCollections.observableArrayList(suggestedConnections);
////        locationConnectionsTable.getItems().clear();
////        locationConnectionsTable.setItems(connections);
//        ArrayList<String> errorMsgs = new ArrayList<>();
//        for (LocationConnection c : suggestedConnections) {
//            try {
//                locationConnectionService.addConnection(c);
//                locationConnectionsTable.getItems().addConnection(c);
//            } catch (DSARuntimeException ex) {
//                errorMsgs.addConnection(ex.getMessage());
//            }
//        }
//        if (errorMsgs.size() > 0) {
//            Dialogs.create()
//                    .title(errorMsgs.size() + " Verbindungen konnten nicht hinzugefügt werden.")
//                    .masthead(null)
//                    .message(errorMsgs.toString())
//                    .showWarning();
//        }
//
//    }

//    @FXML
//    public void onAddConnectionBtnClicked() {
//
//    }
//
//    @FXML
//    public void onRemoveConnectionBtnClicked() {
//        LocationConnection selected = locationConnectionsTable.getSelectionModel().getSelectedItem();
//        locationConnectionService.removeConnection(selected);
//        locationConnectionsTable.getItems().removeConnection(selected);
//    }

//    public void setLocationConnectionService(LocationConnectionService locationConnectionService) {
//        this.locationConnectionService = locationConnectionService;
//    }


//    private void removeConnection(LocationConnection connection) {
//        selectedLocation.removeConnection(connection);
//    }

}