package linkcrawler.logic.htmlUnitEngine;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import java.util.ArrayList;
import linkcrawler.datatypes.URLObject;
/**
 * 链接检查参数配置
 */
public class Configuration {     
    private URLObject domain;
    private ArrayList<String> exclusionListArray; //被排除的urls
    private String browserName = "";
    private int allowedDepthLevel = 1;
    private boolean imageCheck = false;
    private boolean checkSubdomains = false;
    private String httpUserName = "";
    private String httpPassword = "";      
    public Configuration()
    {        
    }
    public URLObject getDomain() {
        return domain;
    }
    public void setDomain(URLObject domain) {
        this.domain = domain;
    }
    public ArrayList<String> getExclusionListArray() {
        return exclusionListArray;
    }
    public void setExclusionListArray(ArrayList<String> exclusionListArray) {
        this.exclusionListArray = exclusionListArray;
    }
    @SuppressWarnings("deprecation")
	public BrowserVersion getBrowserEnum() {
        if (browserName.equals("Mozilla Firefox 3.6")) {
            return BrowserVersion.FIREFOX_3_6;
        }
        else if (browserName.equals("Mozilla Firefox 10")) {
            return BrowserVersion.FIREFOX_10;
        }
        else if (browserName.equals("Mozilla Firefox 17")) {
            return BrowserVersion.FIREFOX_17;
        }
        else if (browserName.equals("IE 6")) {
            return BrowserVersion.INTERNET_EXPLORER_6;
        } else if (browserName.equals("IE 7")) {
            return BrowserVersion.INTERNET_EXPLORER_7;
        } else if (browserName.equals("IE 8")) {
            return BrowserVersion.INTERNET_EXPLORER_8;
        }
        else if (browserName.equals("IE 9")) {
            return BrowserVersion.INTERNET_EXPLORER_9;
        }
        else if (browserName.equals("IE 10")) {
            return BrowserVersion.INTERNET_EXPLORER_10;
        }        
        return BrowserVersion.getDefault();
    }
    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }
    public int getAllowedDepthLevel() {
        if (allowedDepthLevel == 0)
            return 1000000000;
        return allowedDepthLevel;
    }
    public void setAllowedDepthLevel(int allowedDepthLevel) {
        this.allowedDepthLevel = allowedDepthLevel;
    }
    public boolean isImageCheck() {
        return imageCheck;
    }
    public void setImageCheck(boolean imageCheck) {
        this.imageCheck = imageCheck;
    }
    public boolean isCheckSubdomains() {
        return checkSubdomains;
    }
    public void setCheckSubdomains(boolean checkSubdomains) {
        this.checkSubdomains = checkSubdomains;
    }
    public String getHttpUserName() {
        return httpUserName;
    }
    public void setHttpUserName(String httpUserName) {
        this.httpUserName = httpUserName;
    }
    public String getHttpPassword() {
        return httpPassword;
    }
    public void setHttpPassword(String httpPassword) {
        this.httpPassword = httpPassword;
    }       
    @Override
    public String toString()
    {
        String configToDisplay = "******************************\r\n";
        configToDisplay += "Domain to check: " + this.getDomain().toString() +"\r\n";
        configToDisplay += "Amount of excluded URL: " + this.getExclusionListArray().size() +"\r\n";
        configToDisplay += "BrowserName to Use: " + this.getBrowserEnum().getUserAgent() +"\r\n";
        configToDisplay += "Depth Level: " + this.getAllowedDepthLevel() +"\r\n";
        configToDisplay += "Check for Broken Images: " + this.isImageCheck() +"\r\n";
        configToDisplay += "Check for Subdomains: " + this.isCheckSubdomains() +"\r\n";
        configToDisplay += "******************************\r\n";
        configToDisplay += "HTTP AUTH\r\n";
        configToDisplay += "******************************\r\n";
        configToDisplay += "HTTP Username: " + this.getHttpUserName() +"\r\n";
        configToDisplay += "HTTP Password: " + this.getHttpPassword() +"\r\n";
        configToDisplay += "******************************";
        return configToDisplay;
    }   
}
