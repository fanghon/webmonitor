package linkcrawler.datatypes;
import com.sun.javafx.fxml.builder.JavaFXImageBuilder;
import javafx.beans.property.SimpleStringProperty;
public class LinkModel {
	private final SimpleStringProperty url = new SimpleStringProperty("");
	private final SimpleStringProperty contentType = new SimpleStringProperty("");
	private final SimpleStringProperty status = new SimpleStringProperty("");		
	public LinkModel()
	{
		this("", "", "");
	}	
	public LinkModel(String url, String contentType, String status) {
		setUrl(url);
		setContentType(contentType);
		setStatus(status);
    }
	public String getUrl() {
		return url.get();
	}
	public String getContentType() {
		return contentType.get();
	}
	public String getStatus() {
		return status.get();
	}	
	public void setUrl(String url) {
		this.url.set(url);
	}
	public void setContentType(String contentType) {
		this.contentType.set(contentType);
	}
	public void setStatus(String status) {
		this.status.set(status);;
	}
}
