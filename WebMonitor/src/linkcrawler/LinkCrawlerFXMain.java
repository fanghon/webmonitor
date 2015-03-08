package linkcrawler;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
/*
 *  程序主入口类
 */
public class LinkCrawlerFXMain extends Application {
	@Override
	public void start(Stage primaryStage) throws IOException {
		FXMLLoader fxmlLoader  = null;
		fxmlLoader  = new FXMLLoader(getClass().getResource("index.fxml")); //$NON-NLS-1$
		fxmlLoader.setController(new LinkCrawlerFXController());
		Parent root = (Parent) fxmlLoader.load();
		primaryStage.setTitle(Messages.getString("LinkCrawlerFXMain.1")); //$NON-NLS-1$
		primaryStage.getIcons().add(new Image(getClass().getResource("aboutTransparent.png").toExternalForm()));  //$NON-NLS-1$
		Scene scene= new Scene(root,768, 596);		
		scene.getStylesheets().add(getClass().getResource("main.css").toExternalForm()); //$NON-NLS-1$		
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}
