package sepm.dsa.gui;

import javafx.fxml.Initializable;

public interface BaseController extends Initializable {
    /**
     * Reloads the shown ui elements out of the db
     */
    void reload();
}
