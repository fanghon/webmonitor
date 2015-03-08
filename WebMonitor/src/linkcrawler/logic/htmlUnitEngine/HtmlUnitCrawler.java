/*
 *   使用htmlunit爬行网站，深度优先算法，深度是url的子路径数
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
    private ArrayList<String> alreadyCrawled;  //已抓取列表
    private HashMap<String, String[]> previousCheckUrl; //前面检查的urls
    private ArrayList<UrlReport> totalReport;
    private byte runByte = 1;  // 控制抓取线程是否退出
    private Cache cacheHandler = new Cache();
    private String currentStatus = "Crawler Starting......";
    private int goodLinks = 0;
    private int badLinks = 0;
    private final CredentialsProvider credentials = new BasicCredentialsProvider();  // 网站认证
    private LogController log;
    private Configuration cfg; //配置参数对象
    private TableView<LinkModel> linkTable = null;
    private String exclusionRegex = "";   // 过滤正则，符合该正则的不被抓取
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
    //将坏连接加入图形界面的表中
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
            webClient = new WebClient(this.cfg.getBrowserEnum());  // 设置模拟的浏览器类型
        } else {
            webClient = new WebClient();
        }
        if (!this.cfg.getHttpUserName().equals("") && !this.cfg.getHttpPassword().equals("")) {
            webClient.setCredentialsProvider(credentials); //设置网站登录
        }
        webClient.setCache(cacheHandler);  // 缓存
        webClient.getOptions().setCssEnabled(false);  //禁止css
        webClient.getOptions().setJavaScriptEnabled(false);   //禁止js脚本
        //Opening site
        log.addOutputLine("Downloading site...", "info");
        setCurrentStatus("Downloading " + url);
        HtmlPage page;
        //5 secs should be enough;
        webClient.getOptions().setTimeout(5000);     //socket连接和获取数据的超时时间各为5s        
        try {
            page = webClient.getPage(url.toString());
        } catch (Exception ex) {  
            log.addOutputLine("Unable to crawl url: " + url + " " + ex.getMessage(), "ERROR");
            return;
        }
        log.addOutputLine("Crawling...", "Info");
        //将访问过的url加入已访问列表
        this.alreadyCrawled.add(url.toString());  
        
        // 获取网页所有链接
        List<HtmlAnchor> links = page.getAnchors();
        int linksTotal = links.size();
        
        
        log.addOutputLine("Total links detected: " + linksTotal + " link(s)", "info");
        int linksCount = 1;
        // 循环连接一页的url
        for (HtmlAnchor link : links) {
            if (this.runByte == 0) {  //退出抓取
                return;
            }
            LinkStatus linkToCrawl = null;
            try {            	
                linkToCrawl = new LinkStatus(page.getFullyQualifiedUrl(link.getHrefAttribute()).toString(), this.domain);
                setCurrentStatus("Checking link " + (linksCount) + " of " + linksTotal + " : " + linkToCrawl.getHref());
                log.addOutputLine("Checking link " + (linksCount++) + " of " + linksTotal + " : " + linkToCrawl.getHref(), "info");
                String[] evaluationResult = this.evaluateLink(linkToCrawl);//通过实际连接该url获取响应状态码和类型
                String statusCode = evaluationResult[0];
                String contentType = evaluationResult[1];
                log.addOutputLine(linkToCrawl.getHref() + " Got Status: "+ statusCode + " Content-Type: " + contentType, "info");
                linkToCrawl.setHttpCode(statusCode);
                linkToCrawl.setContentType(contentType);
                //Storing site
                reportThisPage.addLink(linkToCrawl);                
                //将连接过的url放入已连接hash表
                previousCheckUrl.put(linkToCrawl.getHref().toString(), new String[]{ linkToCrawl.getHttpCode(), linkToCrawl.getContentType()});
                //Updating counts
                if (!linkToCrawl.isSelfAnchor() && !linkToCrawl.isEmailAddress())
                {                	
                	if(linkToCrawl.isUp()) //根据状态码判断是可显示的url ，2xx,3xx
                	{
                		addGoodLinks(1);    //好连接数
                	}
                	else   //将坏连接加入界面的坏连接表
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
        //处理图片连接
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
                    String[] evaluationResult = this.evaluateLink(linkToCrawl);//通过实际连接该url获取响应状态码和类型
                    String statusCode = evaluationResult[0];
                    String contentType = evaluationResult[1];
                    linkToCrawl.setHttpCode(statusCode);
                    linkToCrawl.setContentType(contentType);
                    //Storing site
                    reportThisPage.addLink(linkToCrawl);
                    if (!linkToCrawl.isSelfAnchor() && !linkToCrawl.isEmailAddress())  //非自连接、email链接
                    {                	
                    	if(linkToCrawl.isUp())  //根据状态码判断是可显示的url ，2xx,3xx
                    	{
                    		addGoodLinks(1);   //好连接数
                    	}
                    	else
                    	{
                    		this.putLinks(linkToCrawl, url.toString());
                    		addBadLinks(1);
                    	}
                    }                 
                    //将已连接过的图片url放入已连接表
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
            //循环实际下载内部url指向的网页
            for (LinkStatus ls : lsArray) {
                try {
                    log.addOutputLine("URL: " + ls.getHref().toString() + ", Depth Level:" + ls.getHref().getDepthLevel() + " - Allowed is " + this.cfg.getAllowedDepthLevel(), "INFO");
                    if (this.cfg.isCheckSubdomains()) {  //若设置子站检查参数，则子站url也会被处理
                        log.addOutputLine("Check subdomains flag is on, checking : " + ls.getHref().toString() + " is inside " + domain.getMainSiteOnly() + " ? ", "INFO");
                        if (!ls.isIsSubdomain()) {  //不是子站，则略过该url
                            log.addOutputLine(ls.getHref().toString() + " is NOT inside " + domain.getMainSiteOnly() + "! ", "INFO");
                            continue;
                        }
                        log.addOutputLine(ls.getHref().toString() + " is clearly inside " + domain.getMainSiteOnly() + "! ", "INFO");
                    }                    
                    if(!ls.isUp())  //根据连接响应状态码，认为可以爬行 2xx 3xx
                    {
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable. Reason: URL was reported as DOWN in a previous run", "INFO");
                    }
                    else if(alreadyCrawled.contains(ls.getHref().toString())) //已经抓取过
                    {
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable. Reason: Already crawled in a previous run", "INFO");
                    }
                    else if(!ls.isValidForCrawling()) //根据抓取的文档类型（默认text/html），认为可以下载，
                    {
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable Reason: ContentType! "+ls.getContentType() + " " + ls.getHttpCode(), "INFO");
                    }
                    else if(shouldBeExcluded(ls.getHref().toString())) //在url排除列表里
                    {
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable. Reason: URL is present in the Exclusion List", "INFO");
                    }
                    else if (ls.getHref().getDepthLevel() > this.cfg.getAllowedDepthLevel()) {  //超过抓取深度
                    	log.addOutputLine("Declaring " + ls.getHref().toString() + " as not crawleable. Reason: URL is located deeper than the expected", "INFO");
                    }
                    else
                    {
                    	crawlURL(ls.getHref());     //递归调用抓取               	
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
    // 对符合要求的连接实际去HTTPConnector连接，返回响应状态码和类型，邮件及# url不连接
    private String[] evaluateLink(LinkStatus ls) throws Exception {
        credentials.clear();
        if (ls.isInternalLink()) {  //站内连接
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
        //处理邮件、#url地址
        if (ls.getHrefUnformatted().startsWith("mailto") == false && ls.getHrefUnformatted().indexOf("#") == -1) {
            try {
                if (previousCheckUrl.containsKey(ls.getHref().toString())) {  //该url前面连接过，
                    return previousCheckUrl.get(ls.getHref().toString());
                }
                HttpURLConnection conn = new HTTPConnector(ls.getHref().toString()).getConnection();
                String status = conn.getHeaderField(null);  //如：200 OK ，why  getResponseCode status code              
                String contentType = conn.getContentType();
                conn.disconnect();
                conn = null;
                return new String[] { status, contentType};
            } catch (Exception ex) {
                return new String[] { "", ""};
            }
        } else if (ls.getHref().toString().indexOf("#") >= 0) {  // 页内自身的url
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
