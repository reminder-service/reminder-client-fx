package de.bahmut.reminder.client;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static com.google.common.base.Preconditions.checkState;

@Log4j2
@Getter
@SpringBootApplication
public class ReminderClient extends Application {

    @Getter
    private static TrayIcon trayIcon;

    private Stage stage;
    private Parent rootNode;

    @Override
    public void init() throws Exception {
        final SpringApplicationBuilder builder = new SpringApplicationBuilder(ReminderClient.class);
        final ConfigurableApplicationContext context = builder.run(getParameters().getRaw().toArray(new String[0]));
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("settings.fxml"));
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
    }

    @Override
    public void start(final Stage stage) {
        this.stage = stage;
        createTrayIcon();
        checkState(trayIcon != null, "Tray icon could ne be created");
        Platform.setImplicitExit(false);
        stage.setTitle("Reminder Client FX");
        stage.setScene(new Scene(rootNode, 520, 300));
        stage.setResizable(false);
        stage.setOnCloseRequest(this::hide);
        stage.centerOnScreen();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        stage.hide();
    }

    private void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            log.warn("System tray not supported");
            return;
        }

        final SystemTray systemTray = SystemTray.getSystemTray();
        try {
            trayIcon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream("icon.png")), "Reminder Center", createTrayMenu());
        } catch (final IOException e) {
            log.error("Could not load tray icon", e);
            return;
        }

        trayIcon.addActionListener(e -> Platform.runLater(stage::show));

        try {
            systemTray.add(trayIcon);
        } catch (final AWTException e) {
            log.error("Could not add tray icon", e);
        }
    }

    private PopupMenu createTrayMenu() {
        final PopupMenu popup = new PopupMenu();
        final MenuItem showItem = new MenuItem("Show");
        showItem.addActionListener(e -> Platform.runLater(stage::show));
        popup.add(showItem);
        final MenuItem closeItem = new MenuItem("Close");
        closeItem.addActionListener(e -> System.exit(0));
        popup.add(closeItem);
        return popup;
    }

    @SuppressWarnings("unused")
    private void hide(final WindowEvent event) {
        Platform.runLater(() -> {
            if (!SystemTray.isSupported()) {
                System.exit(0);
            }
            stage.hide();
        });
    }

    public static void main(final String[] args) {
        // Always run with head since
        // its a fx application
        System.setProperty("java.awt.headless", "false");
        launch();
    }

}
