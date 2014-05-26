package sepm.dsa.gui;

import com.sun.javafx.stage.StageHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sepm.dsa.application.SpringFxmlLoader;
import sepm.dsa.service.MapService;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainMenuController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(MainMenuController.class);
    private SpringFxmlLoader loader;
	private MapService mapService;
	private boolean dragging = false;

    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu dateiMenu;
    @FXML
    private MenuItem dateiImport;
    @FXML
    private MenuItem dateiExport;
    @FXML
    private MenuItem dateiExit;
    @FXML
    private Menu dateiVerwaltenMenu;
    @FXML
    private MenuItem verwaltenHaendlerKategorie;
    @FXML
    private MenuItem verwaltenGebieteGrenzen;
    @FXML
    private MenuItem verwaltenWaehrungen;
    @FXML
    private MenuItem verwaltenWaren;
    @FXML
    private Menu verwaltenWeltkarte;
    @FXML
    private MenuItem weltkarteImportieren;
    @FXML
    private MenuItem weltkarteExportieren;
    @FXML
    private MenuItem location;

    private ImageView mapImageView = new ImageView();
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private Label xlabel;
	@FXML
	private Label ylabel;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
	    scrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
		    public void changed(ObservableValue<? extends Number> ov,
		                        Number old_val, Number new_val) {
			    ylabel.setText(""+ new_val.doubleValue()*100);
		    }
	    });
	    scrollPane.hvalueProperty().addListener(new ChangeListener<Number>() {
		    public void changed(ObservableValue<? extends Number> ov,
		                        Number old_val, Number new_val) {
			    xlabel.setText(""+ new_val.doubleValue()*100);
		    }
	    });

        updateWorldMap();
    }

    @FXML
    private void onGrenzenGebieteClicked() {
        log.debug("onGrenzenGebieteClicked - open Grenzen und Gebiete Window");
        Stage stage = new Stage();
        Parent scene = (Parent) loader.load("/gui/regionlist.fxml");

        stage.setTitle("Grenzen und Gebiete");
        stage.setScene(new Scene(scene, 600, 438));
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void onTraderCategoriesClicked() {
        log.debug("onTraderCategoriesClicked - open Trader Categories Window");
        Stage stage = new Stage();
        Parent scene = (Parent) loader.load("/gui/tradercategorylist.fxml");

        stage.setTitle("Händlerkategorien");
        stage.setScene(new Scene(scene, 600, 438));
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void onWarenClicked() {
        log.debug("onWarenClicked - open Waren Window");
        Stage stage = new Stage();

        Parent scene = (Parent) loader.load("/gui/productslist.fxml");

        stage.setTitle("Waren");
        stage.setScene(new Scene(scene, 600, 438));
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void onShowLocationsClicked() {
        log.debug("onShowLocationsClicked - open Location Window");
        Stage stage = new Stage();
        Parent scene = (Parent) loader.load("/gui/locationlist.fxml");

        stage.setTitle("Orte verwalten");
        stage.setScene(new Scene(scene, 600, 438));
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void onExitClicked() {
        log.debug("onExitClicked - exit Programm Request");
        if (exitProgramm()) {
            Stage primaryStage = (Stage) menuBar.getScene().getWindow();
            primaryStage.close();
        }
    }

    @FXML
    private void onTradersPressed() {

        log.debug("called onTradersPressed");
        Stage stage = new Stage();
        Parent scene = (Parent) loader.load("/gui/traderlist.fxml");
        stage.setTitle("Händlerverwaltung");
        stage.setScene(new Scene(scene, 600, 400));
        stage.setResizable(false);
        stage.show();

    }

    @FXML
    private void onWeltkarteImportierenPressed() {
        log.debug("onWeltkarteImportierenPressed called");

        File newmap = mapService.chooseMap();
	    if (newmap == null) { return; }
	    mapService.setWorldMap(newmap);

        updateWorldMap();
    }

    @FXML
    private void onWeltkarteExportierenPressed() {
        //choose File to export
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export-Datei auswählen");
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
        File exportFile = fileChooser.showSaveDialog(new Stage());

        if (exportFile == null) {
            return;
        }

        //look for world map
        File activeDir = new File("maps/active");
        File[] matchingFiles = activeDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("worldMap");
            }
        });

        if (matchingFiles != null && matchingFiles.length >= 1) {
            File worldMap = matchingFiles[0];
            String extSource = FilenameUtils.getExtension(worldMap.getAbsolutePath());
            String extTarget = FilenameUtils.getExtension(exportFile.getAbsolutePath());
            if (exportFile.exists() && !exportFile.isDirectory()) {
            }
            try {
                if (extTarget == "") {
                    FileUtils.copyFile(worldMap, new File(exportFile.getAbsolutePath() + "." + extSource));
                    exportFile.delete();
                } else {
                    FileUtils.copyFile(worldMap, exportFile);
                }
                log.debug("exported worldMap");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWorldMap() {
        log.debug("updateWorldMap called");
        File worldMap = mapService.getWorldMap();

        if (worldMap != null) {
            Image image = new Image("file:" + worldMap.getAbsolutePath(), true);
            mapImageView.setImage(image);
            mapImageView.setSmooth(true);
            mapImageView.setPreserveRatio(true);
	        scrollPane.setContent(mapImageView);
        }
    }

    /**
     * Shows a exit-confirm-dialog if more than the primaryStage are open and close all other stages if confirmed
     *
     * @return false if the user cancle or refuse the dialog, otherwise true
     */
    public boolean exitProgramm() {
        Stage primaryStage = (Stage) menuBar.getScene().getWindow();
        List<Stage> stages = new ArrayList<Stage>(StageHelper.getStages());

        // only primaryStage
        if (stages.size() <= 1) {
            return true;
        }

        log.debug("open Dialog - Confirm-Exit-Dialog");
        Action response = Dialogs.create()
                .owner(primaryStage)
                .title("Programm beenden?")
                .masthead(null)
                .message("Wollen Sie das Händlertool wirklich beenden? Nicht gespeicherte Änderungen gehen dabei verloren.")
                .showConfirm();

        if (response == Dialog.Actions.YES) {
            log.debug("Confirm-Exit-Dialog confirmed");
            for (Stage s : stages) {
                if (!s.equals(primaryStage)) {
                    s.close();
                }
            }
            return true;

        } else {
            log.debug("Confirm-Exit-Dialog refused");
            return false;
        }
    }

    public void setLoader(SpringFxmlLoader loader) {
        this.loader = loader;
    }

	public void setMapService(MapService mapService) { this.mapService = mapService; }
}
