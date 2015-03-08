package linkcrawler;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import linkcrawler.datatypes.LinkModel;
import linkcrawler.datatypes.URLObject;
import linkcrawler.log.LogController;
import linkcrawler.logic.htmlUnitEngine.Configuration;
import linkcrawler.logic.htmlUnitEngine.HtmlUnitCrawler;
import linkcrawler.report.ReportController;
import linkcrawler.report.ReportType;
public class LinkCrawlerFXController implements Initializable {
	//--------------------FX items
	@FXML
	private Parent root;	
	@FXML
	private Label statusMessageLabel;
	@FXML
	private Label engineStatus;
	@FXML
	private Label healthPercentage;
	@FXML
	private Label linksFoundLabel;
	@FXML
	private Label linksUPLabel;
	@FXML
	private Label linksDownLabel;	
	@FXML
	private Label credentialsToUse;
	@FXML
	private Label exclusionListStatusLabel;
	@FXML
	private ProgressIndicator progressAnimation;
	//menu
	@FXML
	private MenuItem exitMenuItem;
	@FXML
	private MenuItem excludeListMenuItem;
	@FXML
	private Menu optionsMenu;
	@FXML
	private Menu reportsMenu;
	@FXML
	private MenuItem aboutMenuItem;	
	//forms
	@FXML
	private TextField depthLevelSpinner;
	@FXML
	private TextField domainToCheckTextField;
	@FXML
	private ComboBox<String> browserSelection;
	@FXML
	private CheckBox enableImageCheckingCheckbox;
	@FXML
	private CheckBox checkSubdomainsCheckBox;
	@FXML
	private Accordion accordionSection;
	@FXML
	private TableView<LinkModel> linkTable;
	@FXML
	private TableColumn<LinkModel, String> urlCol;
	@FXML
	private TableColumn<LinkModel, String> contentCol;
	@FXML
	private TableColumn<LinkModel, String> statusCol;	
	//form buttons
	@FXML
	private Button addLevelButton;	
	@FXML
	private Button reduceLevelButton;
	@FXML
	private Button credentialsButton;
	@FXML
	private Button startButton;	
	@FXML
	private Button stopButton;	
	//charts
	@FXML
	private PieChart pieChartG;		
	//--------------------FX items Dialog Components
	@FXML
	private TextField httpusernameTextField;
	@FXML
	private TextField httppasswordTextField;	
	// Exclusion Window
	@FXML
	private TextField stringToAddField;
	
	@FXML
	private Button addStringButton;
	
	@FXML
	private ListView<String> exclusionListView;
	// About dialog	
	@FXML
	private ImageView logoimg;	
	@FXML
	private ImageView logofb;	
	//Logic Items
	private ArrayList<String> exclusionListArray = new ArrayList<String>();
    private URLObject domainToCheck;
    private String httpUsername = "", httpPassword = ""; //$NON-NLS-1$ //$NON-NLS-2$
    private LogController mainlog = new LogController(true);
    private HtmlUnitCrawler job = new HtmlUnitCrawler("HTMLCrawlerDummy", new LogController(false)); //$NON-NLS-1$
    private ReportController rc;
    private Stage dialogStage, aboutStage, exclusionStage;	
    private byte runSignal = 1;
    Thread crawlUpdateUIThread = null;	
	Thread reportUpdateUIThread = null;    
    @Override
	public void initialize(URL location, ResourceBundle resources) {
		 if(location.toString().contains("index")) //$NON-NLS-1$
		 {
			 pieChartG.setTitle(Messages.getString("LinkCrawlerFXController.4")); //$NON-NLS-1$
			 clearPieChart();			 
			 //Nice technique from http://stackoverflow.com/questions/10403838/prevent-an-accordion-in-javafx-from-collapsing
			 accordionSection.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
			      @Override public void changed(ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) {
			          if (oldPane != null) oldPane.setCollapsible(true);
			          if (newPane != null) Platform.runLater(new Runnable() { @Override public void run() { 
			            newPane.setCollapsible(false); 
			          }});
			        }
			      });
			 
			 for (TitledPane pane: accordionSection.getPanes()) pane.setAnimated(false);
			 accordionSection.setExpandedPane(accordionSection.getPanes().get(0));
		 }
		 else if(location.toString().contains(Messages.getString("LinkCrawlerFXController.5"))) //$NON-NLS-1$
		 {
			 httpusernameTextField.setText(httpUsername);
			 httppasswordTextField.setText(httpPassword);
		 }
		 else if(location.toString().contains(Messages.getString("LinkCrawlerFXController.6"))) //$NON-NLS-1$
		 {
			 Image imageFB = new Image(this.getClass().getResource("fb.png").toExternalForm()); //$NON-NLS-1$
			 Image imageLogo = new Image(this.getClass().getResource("aboutTransparent.png").toExternalForm()); //$NON-NLS-1$
			 logofb.setImage(imageFB);
			 logoimg.setImage(imageLogo);
		 }		 
	}
	@FXML
	protected void handleCloseActionFromMenu(ActionEvent event) {
		closeApplication();
	}	
	@FXML
	protected void increaseDepth(ActionEvent event) {
		String textInField = depthLevelSpinner.getText();
		depthLevelSpinner.setAlignment(Pos.CENTER_RIGHT);
		if(textInField.equals("Unlimited")) //$NON-NLS-1$
		{
			depthLevelSpinner.setText("1"); //$NON-NLS-1$
		}
		else
		{
			depthLevelSpinner.setText(String.valueOf((Integer.parseInt(textInField) + 1)));
		}		
	}	
	@FXML
	protected void reduceDepth(ActionEvent event) {
		String textInField = depthLevelSpinner.getText();
		if(!textInField.equals("Unlimited")) //$NON-NLS-1$
		{
			int value = (Integer.parseInt(textInField) - 1);
			if (value <= 0)
			{
				depthLevelSpinner.setAlignment(Pos.CENTER);
				depthLevelSpinner.setText("Unlimited"); //$NON-NLS-1$
			}
			else
			{
				depthLevelSpinner.setText(String.valueOf(value));
			}
		}
	}	
	@FXML
	protected void startCrawling(ActionEvent event) {
		disableCriticalElements();
		engineStatus.setText(Messages.getString("LinkCrawlerFXController.13")); //$NON-NLS-1$
		progressAnimation.visibleProperty().set(true);		
		try
	    {
	        domainToCheck = new URLObject(domainToCheckTextField.getText(), true);
	    }
	    catch(Exception e)
	    {
	    	LinkCrawlerFXDialogs.showErrorDialog(Messages.getString("LinkCrawlerFXController.14"), Messages.getString("LinkCrawlerFXController.15"));	         //$NON-NLS-1$ //$NON-NLS-2$
	        enableCriticalElements();
	        engineStatus.setText(Messages.getString("LinkCrawlerFXController.16")); //$NON-NLS-1$
	        progressAnimation.visibleProperty().set(false);
	        return;
	    }		
		String message = ""; //$NON-NLS-1$
	    switch(domainToCheck.isValidMainSiteUrl())
	    {
	        case NOTVALID:
	            message = Messages.getString("LinkCrawlerFXController.18"); //$NON-NLS-1$
	            break;
	        case MALFORMED:
	            message = Messages.getString("LinkCrawlerFXController.19"); //$NON-NLS-1$
	            break;
	        case UNREACHABLE:
	            message = Messages.getString("LinkCrawlerFXController.20"); //$NON-NLS-1$
	            break;
	        case VALID:
	            message = ""; //$NON-NLS-1$
	            break;
	    }
	    if(!message.equals("")) //$NON-NLS-1$
	    {
	    	LinkCrawlerFXDialogs.showErrorDialog(Messages.getString("LinkCrawlerFXController.23"), message);	         //$NON-NLS-1$
	        enableCriticalElements();
	        engineStatus.setText(Messages.getString("LinkCrawlerFXController.24")); //$NON-NLS-1$
	        progressAnimation.visibleProperty().set(false);
	        return;
	    }		    
	    if((!httpUsername.equals("") && httpPassword.equals("")) || (httpUsername.equals("") && !httpPassword.equals(""))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    {       
	        LinkCrawlerFXDialogs.showErrorDialog(Messages.getString("LinkCrawlerFXController.29"), Messages.getString("LinkCrawlerFXController.30")); //$NON-NLS-1$ //$NON-NLS-2$
	        engineStatus.setText(Messages.getString("LinkCrawlerFXController.31")); //$NON-NLS-1$
	        progressAnimation.visibleProperty().set(false);
	        return;
	    }
	    statusMessageLabel.setText(Messages.getString("LinkCrawlerFXController.32")); //$NON-NLS-1$
	    statusMessageLabel.setText(Messages.getString("LinkCrawlerFXController.33")); //$NON-NLS-1$
	    domainToCheckTextField.setText(domainToCheck.getMainSiteOnly());
	    mainlog.addOutputLine(Messages.getString("LinkCrawlerFXController.34"), Messages.getString("LinkCrawlerFXController.35")); //$NON-NLS-1$ //$NON-NLS-2$
	    Configuration config = new Configuration();
		String depthLevel = depthLevelSpinner.getText().trim();
		if (depthLevel.equals(Messages.getString("LinkCrawlerFXController.36").trim())) //$NON-NLS-1$
		{
			config.setAllowedDepthLevel(0);
		}
		else
		{
			config.setAllowedDepthLevel(Integer.parseInt(depthLevel));
		}		
		config.setExclusionListArray(exclusionListArray);
		config.setDomain(domainToCheck);
		config.setCheckSubdomains(checkSubdomainsCheckBox.isSelected());
		config.setImageCheck(enableImageCheckingCheckbox.isSelected());
		config.setBrowserName(browserSelection.getValue());		
        if(!httpUsername.equals("") && !httpPassword.equals("")) //$NON-NLS-1$ //$NON-NLS-2$
        {
        	config.setHttpUserName(httpUsername);
        	config.setHttpPassword(httpPassword);
        }        
        job = new HtmlUnitCrawler(config, mainlog, linkTable);
        job.start();
        linkTable.getItems().clear();             
        runSignal = 1;
        crawlUpdateUIThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (runSignal == 1) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {                        	
                        	if(job.isAlive())
                        	{
                        		statusMessageLabel.setText(job.getCurrentStatus());
                            	pieChartG.getData().get(0).setPieValue(job.getGoodLinks());
                            	pieChartG.getData().get(1).setPieValue(job.getBadLinks());                        	
                            	
                            	int glinks = job.getGoodLinks();
            	                int blinks = job.getBadLinks();
            	                int total = glinks + blinks;            	                
            	                linksUPLabel.setText(String.valueOf(glinks));
            	                linksDownLabel.setText(String.valueOf(blinks));
            	                linksFoundLabel.setText(String.valueOf(total));            	                
            	                if(total > 0)
            	                {
            	                	DecimalFormat df = new DecimalFormat("#"); //$NON-NLS-1$
            		                df.setMaximumFractionDigits(0);		                
            		                double healthValue = ((double) glinks / (double) total) * (double) 100;
            		                healthPercentage.setText(df.format(healthValue) + "%"); //$NON-NLS-1$
            	                }
                        	}
                        	else
                        	{                        		 
                        		 statusMessageLabel.setText(Messages.getString("LinkCrawlerFXController.41")); //$NON-NLS-1$
                                 engineStatus.setText(Messages.getString("LinkCrawlerFXController.42")); //$NON-NLS-1$
                                 progressAnimation.visibleProperty().set(false);
                                 enableCriticalElements();
                                 runSignal = 0;
                        	}
                        	
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        break;
                    }   
                  }               
            }
        });
        crawlUpdateUIThread.setName(Messages.getString("LinkCrawlerFXController.43")); //$NON-NLS-1$
	    crawlUpdateUIThread.setDaemon(true);
        crawlUpdateUIThread.start();		    
	}	
	@FXML
	protected void stopCrawling(ActionEvent event) {
		if(job.isAlive())
		{
			engineStatus.setText(Messages.getString("LinkCrawlerFXController.44")); //$NON-NLS-1$
			job.stopCrawling();
			stopButton.setDisable(true);
		}		
	}	
	private void closeApplication() {
		mainlog.endLog();
		System.exit(0);
	}
	@FXML
	protected void setCredentialsWindow(ActionEvent event) {
		FXMLLoader fxmlLoader  = null;
		fxmlLoader  = new FXMLLoader(getClass().getResource("authDialog.fxml")); //$NON-NLS-1$
		fxmlLoader.setController(this);
		Parent root = null;
		try {
			root = (Parent) fxmlLoader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dialogStage = new Stage();
		dialogStage.setScene(new Scene(root));
		dialogStage.setTitle(Messages.getString("LinkCrawlerFXController.46")); //$NON-NLS-1$
		dialogStage.setResizable(false);
		dialogStage.getScene().getStylesheets().add(getClass().getResource("main.css").toExternalForm()); //$NON-NLS-1$
		dialogStage.getIcons().add(new Image(getClass().getResource("aboutTransparent.png").toExternalForm()));  //$NON-NLS-1$
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(
	        ((Node)event.getSource()).getScene().getWindow() );
		dialogStage.show();
	}	
	@FXML
	protected void openAboutWindows(ActionEvent event) {
		FXMLLoader fxmlLoader  = null;
		fxmlLoader  = new FXMLLoader(getClass().getResource("about.fxml")); //$NON-NLS-1$
		fxmlLoader.setController(this);
		Parent root = null;
		try {
			root = (Parent) fxmlLoader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		aboutStage = new Stage();
		aboutStage.setScene(new Scene(root));

		aboutStage.setTitle(Messages.getString("LinkCrawlerFXController.50")); //$NON-NLS-1$
		aboutStage.initStyle(StageStyle.UNDECORATED);
		aboutStage.setResizable(false);
		aboutStage.getScene().getStylesheets().add(getClass().getResource("main.css").toExternalForm()); //$NON-NLS-1$
		aboutStage.getIcons().add(new Image(getClass().getResource("aboutTransparent.png").toExternalForm()));  //$NON-NLS-1$
		aboutStage.initModality(Modality.WINDOW_MODAL);
		aboutStage.show();
	}	
	@FXML
	protected void openExclusionWindows(ActionEvent event) {
		FXMLLoader fxmlLoader  = null;
		fxmlLoader  = new FXMLLoader(getClass().getResource("exclusionWindow.fxml")); //$NON-NLS-1$
		fxmlLoader.setController(this);
		Parent root = null;
		try {
			root = (Parent) fxmlLoader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exclusionStage = new Stage();
		exclusionStage.setScene(new Scene(root));
		exclusionStage.setTitle(Messages.getString("LinkCrawlerFXController.54")); //$NON-NLS-1$
		exclusionStage.setResizable(false);
		exclusionStage.getScene().getStylesheets().add(getClass().getResource("main.css").toExternalForm()); //$NON-NLS-1$
		exclusionStage.getIcons().add(new Image(getClass().getResource("aboutTransparent.png").toExternalForm()));  //$NON-NLS-1$
		exclusionStage.initModality(Modality.WINDOW_MODAL);
		exclusionStage.show();
	}
	@FXML
	protected void setCredentialsAction(ActionEvent event) {
		httpUsername = httpusernameTextField.getText().trim();
		httpPassword = httppasswordTextField.getText().trim();		
		dialogStage.close();
		if(httpUsername.trim().equals("") && httpPassword.trim().equals("")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			credentialsToUse.setText(Messages.getString("LinkCrawlerFXController.59")); //$NON-NLS-1$
		}
		else
		{
			credentialsToUse.setText(httpUsername);
		}		
	}	
	@FXML
	protected void clearCredentialsAction(ActionEvent event) {
		httpUsername = ""; //$NON-NLS-1$
		httpPassword = "";		 //$NON-NLS-1$
		dialogStage.close();
		if(httpUsername.trim().equals("") && httpPassword.trim().equals("")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			credentialsToUse.setText(Messages.getString("LinkCrawlerFXController.64")); //$NON-NLS-1$
		}
		else
		{
			credentialsToUse.setText(httpUsername);
		}		
	}	
	@FXML
	protected void addStringForExclusion(ActionEvent event) {
		String stringToAdd = stringToAddField.getText().trim();
		if(stringToAdd.isEmpty())
		{
			LinkCrawlerFXDialogs.showErrorDialog(Messages.getString("LinkCrawlerFXController.65"), Messages.getString("LinkCrawlerFXController.66")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if(exclusionListArray.contains(stringToAdd))
		{
			LinkCrawlerFXDialogs.showErrorDialog(Messages.getString("LinkCrawlerFXController.67"), Messages.getString("LinkCrawlerFXController.68")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		exclusionListArray.add(stringToAdd);
		exclusionListView.getItems().add(stringToAdd);
		exclusionListStatusLabel.setText(Messages.getString("LinkCrawlerFXController.69")); //$NON-NLS-1$
		stringToAddField.clear();
	}	
	@FXML
	protected void clearStringForExclusion(ActionEvent event) {
		exclusionListArray.clear();
		exclusionListView.getItems().clear();
		exclusionListStatusLabel.setText(Messages.getString("LinkCrawlerFXController.70")); //$NON-NLS-1$
		exclusionStage.close();
	}	
	@FXML
	protected void goToFacebook(ActionEvent event) {
		if (Desktop.isDesktopSupported()) {
		      try {
		        Desktop.getDesktop().browse(new URL("https://www.facebook.com/carlosumanzor.blog").toURI()); //$NON-NLS-1$
		      } catch (Exception ex) { /* TODO: error handling */ }
		    } else { /* TODO: error handling */ }		
	}
	@FXML
	protected void goToHomePage(ActionEvent event) {
		if (Desktop.isDesktopSupported()) {
		      try {
		        Desktop.getDesktop().browse(new URL("http://www.carlosumanzor.com/linkcrawler").toURI()); //$NON-NLS-1$
		      } catch (Exception ex) { /* TODO: error handling */ }
		    } else { /* TODO: error handling */ }		
	}	
	@FXML
	protected void closeAboutWindows(ActionEvent event) {
		aboutStage.close();		
	}	
	@FXML
	protected void htmlReportMenuItemActionPerformed(ActionEvent event) {
        rc = new ReportController(this.domainToCheckTextField.getText(), job.getReport());
        disableCriticalElements();
        statusMessageLabel.setText(Messages.getString("LinkCrawlerFXController.73")); //$NON-NLS-1$
        rc.start();
        runSignal = 1;
        createReportUpdateThread();
        reportUpdateUIThread.start();
    }	
	@FXML
	protected void jsonReportMenuItemActionPerformed(ActionEvent event) {
        rc = new ReportController(this.domainToCheckTextField.getText(), job.getReport(), ReportType.JSON);
        disableCriticalElements();
        statusMessageLabel.setText(Messages.getString("LinkCrawlerFXController.74")); //$NON-NLS-1$
        rc.start();
        runSignal = 1;        
        createReportUpdateThread();        
        reportUpdateUIThread.start();
    }	
	@FXML
	protected void excelReportMenuItemActionPerformed(ActionEvent event) {
        rc = new ReportController(this.domainToCheckTextField.getText(), job.getReport(), ReportType.EXCEL);
        disableCriticalElements();
        statusMessageLabel.setText(Messages.getString("LinkCrawlerFXController.75")); //$NON-NLS-1$
        rc.start();
        runSignal = 1;        
        createReportUpdateThread();        
        reportUpdateUIThread.start();
    }
	private void createReportUpdateThread()
	{
		reportUpdateUIThread = new Thread(new Runnable() {
            @Override
            public void run() {
            	
                while (runSignal == 1) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                        	if(rc.isAlive())
                        	{
                        		statusMessageLabel.setText(rc.getStatus());
                        	}
                        	else
                        	{
                        		runSignal = 0;
                        		enableCriticalElements();                    		
                        		if(!rc.isSuccess())
                                {
                        			statusMessageLabel.setText(Messages.getString("LinkCrawlerFXController.76")); //$NON-NLS-1$
                        			LinkCrawlerFXDialogs.showErrorDialog(Messages.getString("LinkCrawlerFXController.77"), Messages.getString("LinkCrawlerFXController.78") + rc.getException());                                 //$NON-NLS-1$ //$NON-NLS-2$
                                }
                                else
                                {
                                	statusMessageLabel.setText(Messages.getString("LinkCrawlerFXController.79")); //$NON-NLS-1$
                                	LinkCrawlerFXDialogs.showInfoDialog(Messages.getString("LinkCrawlerFXController.80"), Messages.getString("LinkCrawlerFXController.81") + rc.getReportLocation()); //$NON-NLS-1$ //$NON-NLS-2$
                                }                    		
                        	}
                        	
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }                
            }
        });
	}
	private void clearPieChart()
	{
		ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                new PieChart.Data(Messages.getString("LinkCrawlerFXController.82"), 0), //$NON-NLS-1$
                new PieChart.Data(Messages.getString("LinkCrawlerFXController.83"), 0)); //$NON-NLS-1$
				 pieChartG.setData(pieChartData);
				 pieChartG.setAnimated(true);
				 pieChartG.setLabelsVisible(false);
	}	
	private void disableCriticalElements()
	{
		//Disable everything
		startButton.setDisable(true);
		addLevelButton.setDisable(true);
		reduceLevelButton.setDisable(true);
		depthLevelSpinner.setDisable(true);
		browserSelection.setDisable(true);
		credentialsButton.setDisable(true);
		domainToCheckTextField.setDisable(true);
		checkSubdomainsCheckBox.setDisable(true);
		enableImageCheckingCheckbox.setDisable(true);
		optionsMenu.setDisable(true);
		reportsMenu.setDisable(true);
		//But enable STOP
		stopButton.setDisable(false);
	}	
	private void enableCriticalElements()
	{
		startButton.setDisable(false);
		addLevelButton.setDisable(false);
		reduceLevelButton.setDisable(false);
		depthLevelSpinner.setDisable(false);
		browserSelection.setDisable(false);
		credentialsButton.setDisable(false);
		domainToCheckTextField.setDisable(false);
		checkSubdomainsCheckBox.setDisable(false);
		enableImageCheckingCheckbox.setDisable(false);
		optionsMenu.setDisable(false);
		reportsMenu.setDisable(false);
		stopButton.setDisable(true);
	}	
}
