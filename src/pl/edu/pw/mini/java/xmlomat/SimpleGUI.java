package pl.edu.pw.mini.java.xmlomat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.xml.transform.TransformerConfigurationException;
import java.io.*;
import java.util.List;
import java.util.Optional;


public class SimpleGUI extends Application implements FileParsingUI {

    public Button parseSingleFileButton;
    public Button parseMultipleFilesButton;
    public ImageView mainImage;
    private Stage stage;
    private static XmlParser xmlparser;
    private Image minilogo = new Image("file:images/minilogo.png");
    private Image processingimg = new Image("file:images/processing.png");
    private static boolean processingManyFiles = false;
    private static boolean processingSingleFile = false;


    @Override
    public void start(Stage primaryStage) throws Exception {
        xmlparser = new XmlParser(this);
        stage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("XML-O-MAT");
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        mainImage = (ImageView) scene.lookup("#mainImage");
        mainImage.setImage(minilogo);
    }


    public static void main(String[] args) {
        launch(args);
    }

    public void parseSingleFileOnClick(ActionEvent actionEvent) {
        mainImage.setImage(processingimg);
        System.out.println("parse Single FileOnClick");

        FileChooser fil_chooser = new FileChooser();
        fil_chooser.setTitle("Choose single file");
        File file = fil_chooser.showOpenDialog(stage);
        if (file != null) {
            processingSingleFile = true;
            System.out.println("file chosen: "+file.getAbsolutePath());
            xmlparser.parseFiles(file);
        }
    }

    public void parseMultipleFileOnClick(ActionEvent actionEvent) {
        mainImage.setImage(processingimg);
        System.out.println("parse Multiple FileOnClick");

        FileChooser fil_chooser = new FileChooser();
        fil_chooser.setTitle("Choose single file");
        List<File> files = fil_chooser.showOpenMultipleDialog(stage);
        if (files!=null) {
            processingManyFiles = true;
            System.out.println("files chosen: "+files);
            xmlparser.parseFiles(files);
        }
    }

    @Override
    public void onFileLoadFail(String path) {
        if(processingSingleFile) {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Error encountered.");
                a.setContentText("Could not access file: " + path);
                a.showAndWait();
                resetGUIState();
            });
        }
        else{
            showErrorMultithread(path);
        }
    }

    private void showErrorMultithread(String path){
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Error");
            a.setHeaderText("Error encountered while reading or processing: "+path);
            a.setContentText("Skip this file or cancel all?");

            ButtonType buttonTypeSkip = new ButtonType("Skip");
            ButtonType buttonTypeCancelAll = new ButtonType("Cancel all", ButtonBar.ButtonData.CANCEL_CLOSE);
            a.getButtonTypes().setAll(buttonTypeSkip, buttonTypeCancelAll);

            Optional<ButtonType> result = a.showAndWait();

            if (result.get() == buttonTypeSkip) {
                System.out.println("skipped file");
            } else {
                System.out.println("cancel all files");
                xmlparser.cancelAll();
            }
        });

    }

    @Override
    public void onFileInvalidStructure(String path) {
         if(processingSingleFile){
             Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Error encountered.");
                a.setContentText("Could not parse file: " + path);
                a.showAndWait();
                resetGUIState();
             });
        }
        else{
            showErrorMultithread(path);
        }
    }

    @Override
    public void onFileParsed(UnsavedFile unsavedXmlFile) {
        if(processingSingleFile){
            Platform.runLater(() -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("lel.xml");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    System.out.println("saving to directory: " + file.getAbsolutePath());
                    unsavedXmlFile.save(file.getAbsolutePath());
                }
                else {
                    System.out.println("save location was not selected - decide what to do next");
                    onFileSaveFail(unsavedXmlFile);
                }
            });
        }
        else{
            File file = new File(unsavedXmlFile.getInputPath());
            String path = file.getAbsoluteFile().getParent() + "\\processed_" + file.getName();
            unsavedXmlFile.save(path);
        }
    }

    @Override
    public void onFileSaveFail(UnsavedFile unsavedXmlFile) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Error");
            a.setHeaderText("Error saving file. Retry?");
            ButtonType buttonRetry = new ButtonType("Yes, retry");

            if(processingSingleFile){
                a.setContentText("Would you like to choose another directory and try again?");
                ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                a.getButtonTypes().setAll(buttonRetry, buttonTypeCancel);

                Optional<ButtonType> result = a.showAndWait();

                 if (result.get() == buttonRetry) {
                     System.out.println("retry saving file");
                     onFileParsed(unsavedXmlFile);
                 } else {
                     System.out.println("discard file");
                     resetGUIState();
                }
            }
            else{
                a.setContentText("Would you like to skip this file or try again?");
                ButtonType buttonTypeSkip = new ButtonType("Skip", ButtonBar.ButtonData.CANCEL_CLOSE);
                a.getButtonTypes().setAll(buttonRetry, buttonTypeSkip);

                Optional<ButtonType> result = a.showAndWait();

                if (result.get() == buttonRetry) {
                    System.out.println("retry saving file");
                    onFileParsed(unsavedXmlFile);
                } else {
                    System.out.println("skip file");
                }
            }
        });
    }

    @Override
    public void endFileProcessing() {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.NONE);
            a.setAlertType(Alert.AlertType.INFORMATION);
            a.setTitle("Finished");
            a.setHeaderText("Finished");
            a.setContentText("Finished processing files.");
            a.showAndWait();
            resetGUIState();
        });
    }

    private void resetGUIState(){

        mainImage.setImage(minilogo);
        processingSingleFile = false;
        processingManyFiles = false;
    }
}

