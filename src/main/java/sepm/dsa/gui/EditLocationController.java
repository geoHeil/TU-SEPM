package sepm.dsa.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sepm.dsa.application.SpringFxmlLoader;
import sepm.dsa.exceptions.DSAValidationException;
import sepm.dsa.model.*;
import sepm.dsa.service.*;

import java.io.File;
import java.util.*;

@Service("EditLocationController")
public class EditLocationController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(EditLocationController.class);
    private SpringFxmlLoader loader;

    private static Location selectedLocation;
    private static Set<LocationConnection> connections = new HashSet<>();

    private LocationService locationService;
    private LocationConnectionService locationConnectionService;
    private RegionService regionService;
	private MapService mapService;
    private SaveCancelService saveCancelService;
    // true if the location is not editing
    private boolean isNewLocation;

	private int xCoord = 0;
	private int yCoord = 0;
	private File newMap;

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

    public static void setConnections(Set<LocationConnection> connections) {
        EditLocationController.connections = connections;
    }
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
	        xCoord = selectedLocation.getxCoord();
	        yCoord = selectedLocation.getyCoord();
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

        Set<LocationConnection> allConnections = this.connections;//selectedLocation.getAllConnections();
        ObservableList<LocationConnection> connections = FXCollections.observableArrayList(allConnections);
        locationConnectionsTable.setItems(connections);
//        this.connections = new HashSet<>(connections);

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
        //TODO @Michael: wozu war das hier gedacht? wirft einen fehler wenn region null? ++ Model Location Region set to NOT NULL
//        saveCancelService.refresh(selectedLocation);
//        log.info("before: connections.size=" + selectedLocation.getAllConnections().size());
//        selectedLocation = locationService.get(selectedLocation.getId());
//        log.info("after: connections.size=" + selectedLocation.getAllConnections().size());

        Stage stage = (Stage) nameField.getScene().getWindow();
	    stage.close();
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
        selectedLocation.setxCoord(xCoord);
        selectedLocation.setyCoord(yCoord);
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

        Set<LocationConnection> localConnectionList = connections;
        for (LocationConnection connection : locationConnectionService.getAllByLocation(selectedLocation.getId())) {
            boolean contain = false;
            for (LocationConnection localConnection : localConnectionList) {
                if (localConnection.equalsById(connection)) {
                    locationConnectionService.update(connection);
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                locationConnectionService.remove(connection);
            }
            localConnectionList.remove(connection);
        }
        for (LocationConnection connection : localConnectionList) {
            locationConnectionService.add(connection);
        }


        log.info("selectedLocation.id = " + selectedLocation.getId());
//        selectedLocation = locationService.get(selectedLocation.getId());

    }

    @FXML
    private void onSavePressed() {
        log.debug("calling SaveButtonPressed");

	    if (newMap != null) {
		    mapService.setLocationMap(selectedLocation, newMap);
	    }
        applyLocationChanges();

        saveCancelService.save();
//        locationService.update(selectedLocation);
        saveCancelService.refresh(selectedLocation);

        // return to locationlist
        Stage stage = (Stage) cancelButton.getScene().getWindow();
	    stage.close();
    }

    @FXML
    public void chooseBackground() {
        log.info("Select Backgroundimage Location");
	    newMap = mapService.chooseMap();
    }


    public static void setLocation(Location location) {
        log.debug("calling setLocation(" + location + ")");
        selectedLocation = location;
        if (selectedLocation != null) {
            connections = new HashSet<>(selectedLocation.getAllConnections());
        }
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

	public void setPosition(Point2D pos) {
		this.yCoord = (int) pos.getY();
		this.xCoord = (int) pos.getX();
	}

	public void setMapService(MapService mapService) {
		this.mapService = mapService;
	}

    public void setSaveCancelService(SaveCancelService saveCancelService) {
        this.saveCancelService = saveCancelService;
    }

    public void setLocationConnectionService(LocationConnectionService locationConnectionService) {
        this.locationConnectionService = locationConnectionService;
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