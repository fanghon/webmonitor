/*
 *   ʹ��htmlunit������վ����������㷨�������url����·����
 * 
 */
package linkcrawler.logic.htmlUnitEngine;
import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import linkcrawler.connectors.HTTPConnector;
import linkcrawler.datatypes.LinkModel;
import linkcrawler.datatypes.LinkStatus;
import linkcrawler.datatypes.URLObject;
import linkcrawler.datatypes.UrlReport;
import linkcrawler.log.LogController;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
public class HtmlUnitCrawler extends Thread {
    private URLObject domain;
    private ArrayList<String> alreadyCrawled;  //��ץȡ�б�
    private HashMap<String, String[]> previousCheckUrl; //ǰ�����urls
    private ArrayList<UrlReport> totalReport;
    private byte runByte = 1;  // ����ץȡ�߳��Ƿ��˳�
    private Cache cacheHandler = new Cache();
    private String currentStatus = "Crawler Starting......";
    private int goodLinks = 0;
    private int badLinks = 0;
    private final CredentialsProvider credentials = new BasicCredentialsProvider();  // ��վ��֤
    private LogController log;
    private Configuration cfg; //���ò�������
    private TableView<LinkModel> linkTable = null;
    private String exclusionRegex = "";   // �������򣬷��ϸ�����Ĳ���ץȡ
    public HtmlUnitCrawler(String threadName, LogController log) {
        super(threadName); // Initialize thread.
        this.log = log;
        alreadyCrawled = new ArrayList<String>();
        totalReport = new ArrayList<UrlReport>();
        previousCheckUrl = new HashMap<String, String[]>();
    }
    public HtmlUnitCrawler(Configuration cfg, LogController log, TableView<LinkModel> linkTable) {
        this("HtmlUnitCrawler", log);
        this.cfg = cfg;
        this.domain = cfg.getDomain();
        this.linkTable = linkTable;
    }
    @Override
    public void run() {
        crawlURL(this.domain);
        setCurrentStatus("Crawler Stopped");
    }     
    //�������Ӽ���ͼ�ν���ı���
    private void putLinks(LinkStatus ls, String locatedAt){
    	final LinkStatus linkdata = ls;
    	Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	if(linkdata != null)
                {
                	try {
        				linkTable.getItems().add(new LinkModel(linkdata.getHref().toString(), linkdata.getContentType(), linkdata.getHttpCode()));
        			} catch (Exception e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                }
            }
            });    	
    }    
    @SuppressWarnings("unchecked")
	private void crawlURL(URLObject url) {
        if (this.runByte == 0) {
            return;
        }        
        //Starting Report
        UrlReport reportThisPage = new UrlReport(url.toString());
        log.addOutputLine("Working on url: " + url, "info");
        //Starting client
        WebClient webClient;
        if (this.cfg.getBrowserEnum() != null) {
            webClient = new WebClient(this.cfg.getBrowserEnum());  // ����ģ������������
        } else {
            webClient = new WebClient();
        }
        if (!this.cfg.getHttpUserName().equals("") && !this.cfg.getHttpPassword().equals("")) {
            webClient.setCredentialsProvider(credentials); //������վ��¼
        }
        webClient.setCache(cacheHandler);  // ����
        webClient.getOptions().setCssEnabled(false);  //��ֹcss
        webClient.getOptions().setJavaScriptEnabled(false);   //��ֹjs�ű�
        //Opening site
        log.addOutputLine("Downloading site...", "info");
        setCurrentStatus("Downloading " + url);
        HtmlPage page;
        //5 secs should be enough;
        webClient.getOptions().setTimeout(5000);     //socket���Ӻͻ�ȡ���ݵĳ�ʱʱ���Ϊ5s        
        try {
            page = webClient.getPage(url.toString());
        } catch (Exception ex) {  
            log.addOutputLine("Unable to crawl url: " + url + " " + ex.getMessage(), "ERROR");
            return;
        }
        log.addOutputLine("Crawling...", "Info");
        //�����ʹ���url�����ѷ����б�
        this.alreadyCrawled.add(url.toString());  
        
        // ��ȡ��ҳ��������
        List<HtmlAnchor> links = page.getAnchors();
        int linksTotal = links.size();
        
        
        log.addOutputLine("Total links detected: " + linksTotal + " link(s)", "info");
        int linksCount = 1;
        // ѭ������һҳ��url
        for (HtmlAnchor link : links) {
            if (this.runByte == 0) {  //�˳�ץȡ
                return;
            }
            LinkStatus linkToCrawl = null;
            try {            	
                linkToCrawl = new LinkStatus(page.getFullyQualifiedUrl(link.getHrefAttribute()).toString(), this.domain);
                setCurrentStatus("Checking link " + (linksCount) + " of " + linksTotal + " : " + linkToCrawl.getHref());
                log.addOutputLine("Checking link " + (linksCount++) + " of " + linksTotal + " : " + linkToCrawl.getHref(), "info");
                String[] evaluationResult = this.evaluateLink(linkToCrawl);//ͨ��ʵ�����Ӹ�url��ȡ��Ӧ״̬�������
                String statusCode = evaluationResult[0];
                String contentType = evaluationResult[1];
                log.addOutputLine(linkToCrawl.getHref() + " Got Status: "+ statusCode + " Content-Type: " + contentType, "info");
                linkToCrawl.setHttpCode(statusCode);
                linkToCrawl.setContentType(contentType);
                //Storing site
                reportThisPage.addLink(linkToCrawl);                
                //�����ӹ���url����������hash��
                previousCheckUrl.put(linkToCrawl.getHref().toString(), new String[]{ linkToCrawl.getHttpCode(), linkToCrawl.getContentType()});
                //Updating counts
                if (!linkToCrawl.isSelfAnchor() && !linkToCrawl.isEmailAddress())
                {                	
                	if(linkToCrawl.isUp()) //����״̬���ж��ǿ���ʾ��url ��2xx,3xx
                	{
                		addGoodLinks(1);    //��������
                	}
                	else   //�������Ӽ������Ļ����ӱ�
                	{
                		this.putLinks(linkToCrawl, url.toString());
                		addBadLinks(1);
                	}
                }
            } catch (Exception e) {
                log.addOutputLine(e.getMessage(), "Error");
                continue;
            }
        }        
        //����ͼƬ����
        List<HtmlImage> images = null;
        if (this.cfg.isImageCheck()) {
            log.addOutputLine("Collecting img tags...", "Info");
            images = (List<HtmlImage>) page.getByXPath("//img");
        }
        if (this.cfg.isImageCheck()) {
            log.addOutputLine("Checking src attribute on IMG tags...", "info");
            int imagesTotal = links.size();
            log.addOutputLine("Total images detected: " + linksTotal + " image(s)", "info");
            int imageCount = 1;
            for (HtmlImage image : images) {
                if (this.runByte == 0) {
                    return;
                }
                LinkStatus linkToCrawl;
                try {
                    linkToCrawl = new LinkStatus(page.getFullyQualifiedUrl(image.getSrcAttribute()).toString(), this.domain, true);
                    setCurrentStatus("Checking image " + (imageCount++) + " of " + imagesTotal + " : " + linkToCrawl.getHref());
                    String[] evaluationResult = this.evaluateLink(linkToCrawl);//ͨ��ʵ�����Ӹ�url��ȡ��Ӧ״̬�������
                    String statusCode = evaluationResult[0];
                    String contentType = evaluationResult[1];
                    linkToCrawl.setHttpCode(statusCode);
                    linkToCrawl.setContentType(contentType);
                    //Storing site
                    reportThisPage.addLink(linkToCrawl);
                    if (!linkToCrawl.isSelfAnchor() && !linkToCrawl.isEmailAddress())  //�������ӡ�email����
                    {                	
                    	if(linkToCrawl.isUp())  //����״̬���ж��ǿ���ʾ��url ��2xx,3xx
                    	{
                    		addGoodLinks(1);   //��������
                    	}
                    	else
                    	{
                    		this.putLinks(linkToCrawl, url.toString());
                    		addBadLinks(1);
                    	}
                    }                 
                    //�������ӹ���ͼƬurl���������ӱ�
                    previousCheckUrl.put(linkToCrawl.getHref().toString(), new String[]{ linkToCrawl.getHttpCode(), linkToCrawl.getContentType()});
                } catch (Exception e) {
                    log.addOutputLine(e.getMessage(), "Error");
                    continue;
                }
            }
        }        
        //Recovering resourcers
        page.cleanUp();
        webClient.closeAllWindows();
        page = null;
        webClient = null;
        log.addOutputLine("Saving report in memory\r\n", "info");
        totalReport.ensureCapacity(totalReport.size());
        totalReport.add(reportThisPage);
        if (reportThisPage.haveInternalLinks()) {
            ArrayList<LinkStatus> lsArray = reportThisPage.getInternalLinks();
            //ѭ��ʵ�������ڲ�urlָ�����ҳ
            for (LinkStatus ls : lsArray) {
                try {
                    log.addOutputLine("URL: " + ls.getHref().toString() + ", Depth Level:" + ls.getHref().getDepthLevel() + " - Allowed is " + this.cfg.getAllowedDepthLevel(), "INFO");
                    if (this.cfg.isCheckSubdomains()) {  //��������վ������������վurlҲ�ᱻ����
                        log.addOutputLine("Check subdomains flag is on, checking : " + ls.getHref().toString() + " is inside " + domain.getMainSiteOnly() + " ? ", "INFO");
                        if (!ls.isIsSubdomain()) {  //������վ�����Թ���url
                            log.addOutputLine(ls.getHref().toString() + " is NOT inside " + domain.getMainSiteOnly() + "! ", "INFO");
                            continue;
                        }
                        log.addOutputLine(ls.getHref().toString() + " is clearly inside " + domain.getMainSiteOnly() + "! ", "INFO");
                    }                    
                    if(!ls.isUp())  //����������Ӧ״̬�룬��Ϊ�������� 2xx 3xx
                    {
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable. Reason: URL was reported as DOWN in a previous run", "INFO");
                    }
                    else if(alreadyCrawled.contains(ls.getHref().toString())) //�Ѿ�ץȡ��
                    {
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable. Reason: Already crawled in a previous run", "INFO");
                    }
                    else if(!ls.isValidForCrawling()) //����ץȡ���ĵ����ͣ�Ĭ��text/html������Ϊ�������أ�
                    {
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable Reason: ContentType! "+ls.getContentType() + " " + ls.getHttpCode(), "INFO");
                    }
                    else if(shouldBeExcluded(ls.getHref().toString())) //��url�ų��б���
                    {
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable. Reason: URL is present in the Exclusion List", "INFO");
                    }
                    else if (ls.getHref().getDepthLevel() > this.cfg.getAllowedDepthLevel()) {  //����ץȡ���
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable. Reason: URL is located deeper than the expected", "INFO");
                    }
                    else
                    {
                    	crawlURL(ls.getHref());     //�ݹ����ץȡ               	
                    }
                    if (this.runByte == 0) {
                        break;
                    }
                } catch (Exception ex) {
                    log.addOutputLine(ex.getMessage(), "ERROR");
                    ex.printStackTrace();
                    continue;
                }
            }
        }
    }
    private void generateExclusionRegex()
    {
    	int startFlag = 0;
    	for (String exclusionString : this.cfg.getExclusionListArray())
    	{
    		if(startFlag == 0)
    			this.exclusionRegex = ".*"+exclusionString.toLowerCase()+".*";
    		else
    			this.exclusionRegex = "|.*"+exclusionString.toLowerCase()+".*";
    		startFlag = 1;
    	}    	
    }
    private boolean shouldBeExcluded(String href) {
    	if(cfg.getExclusionListArray().isEmpty())
    		return false;
    	
    	if (this.exclusionRegex.equals(""))
    	{
    		generateExclusionRegex();
    	}
    	if (href.trim().toLowerCase().matches(exclusionRegex)) {
            return true;
        }
        return false;
    }
    public boolean isReportReady() {
        return !this.totalReport.isEmpty();
    }
    public ArrayList<UrlReport> getReport() {
        return this.totalReport;
    }
    public void stopCrawling() {
        this.runByte = 0;
    }
    // �Է���Ҫ�������ʵ��ȥHTTPConnector���ӣ�������Ӧ״̬������ͣ��ʼ���# url������
    private String[] evaluateLink(LinkStatus ls) throws Exception {
        credentials.clear();
        if (ls.isInternalLink()) {  //վ������
            if (!this.cfg.getHttpUserName().equals("") && !this.cfg.getHttpPassword().equals("")) {
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(cfg.getHttpUserName(), cfg.getHttpPassword().toCharArray());
                    }
                });

                credentials.setCredentials(
                        new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
                        new UsernamePasswordCredentials(this.cfg.getHttpUserName(), this.cfg.getHttpPassword()));
            }
        }
        //�����ʼ���#url��ַ
        if (ls.getHrefUnformatted().startsWith("mailto") == false && ls.getHrefUnformatted().indexOf("#") == -1) {
            try {
                if (previousCheckUrl.containsKey(ls.getHref().toString())) {  //��urlǰ�����ӹ���
                    return previousCheckUrl.get(ls.getHref().toString());
                }
                HttpURLConnection conn = new HTTPConnector(ls.getHref().toString()).getConnection();
                String status = conn.getHeaderField(null);  //�磺200 OK ��why  getResponseCode status code              
                String contentType = conn.getContentType();
                conn.disconnect();
                conn = null;
                return new String[] { status, contentType};
            } catch (Exception ex) {
                return new String[] { "", ""};
            }
        } else if (ls.getHref().toString().indexOf("#") >= 0) {  // ҳ�������url
        	 return new String[] { "Self Anchor", ""};            
        } else {
        	return new String[] { "Email Address", ""};  
        }
    }
    public String getCurrentStatus() {
        return currentStatus;
    }
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
    public int getGoodLinks() {
        return goodLinks;
    }
    private void addGoodLinks(int goodLink) {
        this.goodLinks += goodLink;
    }
    public int getBadLinks() {
        return badLinks;
    }
    private void addBadLinks(int badLink) {
        this.badLinks += badLink;
    }
}
