/*
 *
 */
package linkcrawler.logic.htmlUnitEngine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import linkcrawler.connectors.HTTPConnector;
import linkcrawler.datatypes.URLObject;
import linkcrawler.log.LogController;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * 
 */
public class HTMLUnitSitemapVerificator extends Thread{
    
    private String status = "Checking Sitemap", accesible = "", valid = "OK", urlUP = "OK", notRedirects = "OK", size = "", lastModified = "";
    private int totalLinks;
    private long lastmodifiedPage = 0;
    private URLObject sitemapLocation;
    private LogController log;
    private XPathFactory xPathfactory = XPathFactory.newInstance();
    private XPath xpath = xPathfactory.newXPath();
    private ArrayList<String> recommendations;
    
    public HTMLUnitSitemapVerificator(String threadName, LogController log)
    {
        super(threadName);
        this.log = log;
        recommendations = new ArrayList<String>();
    }
    
    public HTMLUnitSitemapVerificator(URLObject sitemapLocation, LogController log)
    {
        this("HTMLUnitSitemapVerificator", log);
        this.sitemapLocation = sitemapLocation;
    }
    
    @Override
    public void run() {
        try {
            getLog().addOutputLine("Sitemap to check: " + sitemapLocation.getSitemapLocation(), "config");
            
            WebClient webClient = new WebClient();
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);
            
            this.status = "Getting XML..... " + sitemapLocation.getSitemapLocation();
            getLog().addOutputLine("Getting XML..... " + sitemapLocation.getSitemapLocation(), "info");
            XmlPage xmlSiteMap = webClient.getPage(sitemapLocation.getSitemapLocation());
            
            //if XMLSitemap doesn't crash we can assume that the xml is both accesible and valid
            this.accesible = "OK";
            this.status = "Checking is sitemap is valid for google..... ";
            getLog().addOutputLine("Checking is sitemap is valid for google..... ", "info");
            if(!isValidForGoogle(xmlSiteMap.getXmlDocument()))
                this.valid = "ERROR";
            
            this.status = "Getting amount of URL in sitemap..... ";
            getLog().addOutputLine("Getting amount of URL in sitemap..... ", "info");
            //Getting amount of links            
            XPathExpression expr = xpath.compile("//*[local-name()=\"loc\"]");
            
            NodeList nl = (NodeList) expr.evaluate(xmlSiteMap.getXmlDocument(), XPathConstants.NODESET);
            
            for (int i = 0; i < nl.getLength(); i++) {
                 Node aNode = nl.item(i);                 
                 verifyUrl(aNode.getTextContent());
            } 
            
            this.totalLinks = nl.getLength();
            this.size =  (xmlSiteMap.asXml().getBytes().length / 1024) +" Kb";
            
            HttpURLConnection conn = new HTTPConnector(sitemapLocation.getSitemapLocation()).getConnection();
            //Get last modified date            
            this.lastModified = new Date(conn.getLastModified()).toString();
            conn.disconnect();
            
            
        } catch (Exception ex) {
            accesible = "ERROR";
            this.valid = "ERROR";
            this.notRedirects = "N/A";
            this.urlUP = "N/A";
            recommendations.add("- URL provided is not a valid XML please adjust it correctly");
            getLog().addOutputLine("Error on XML..... " + ex.getMessage(), "ERROR");
            ex.printStackTrace();
        } 
        
    }
    
    public int amountOfRecommendations()
    {
        return this.recommendations.size();
    }
    
    public String getRecommendations()
    {
        String recommend = "";
        
        for (String recommendation : this.recommendations)
        {
            recommend += recommendation + "\r\n";
        }
        
        return recommend;
    }

    private boolean isValidForGoogle(Document siteMapXML)
    {
        try {
            XPathExpression expr = xpath.compile("//*[local-name()=\"urlset\"]/*[local-name()=\"url\"]");
            boolean result = (Boolean) expr.evaluate(siteMapXML, XPathConstants.BOOLEAN);
            if(result)    
                return true;
            
            recommendations.add("- Looks like your sitemap doesn't have the right structure.\r\n   please follow the instructions on this page http://support.google.com/webmasters/bin/answer.py?hl=en&answer=183668#2");
            
            return false;
        } catch (XPathExpressionException ex) {
            getLog().addOutputLine(ex.getMessage(), "ERROR");
            return false;
        }
    }
    
    private void verifyUrl(String Url)
    {
        try {
            this.status = "Checking URL..... " + Url;
            getLog().addOutputLine("Checking URL..... " + Url, "INFO");
            HttpURLConnection conn = new HTTPConnector(Url).getConnection();
            
            String header = conn.getHeaderField(0);
            if(!header.trim().equals("HTTP/1.1 200 OK"))
            {
                 getLog().addOutputLine("Page " + Url + " Status: " + header + ": SiteMap should use 200 HTTP/Code URLs", "ERROR");
                 recommendations.add("- Sitemap contains url " + Url + ", which returns a Status of " + header + ". SiteMap should use URL that gives 200 HTTP Status Codes");
                 this.notRedirects = "ERROR";
            }
            else
            {
                getLog().addOutputLine("Page " + Url + " Status: " + header + ": It's OK", "INFO");  
            }
            
            conn.disconnect();
        } catch (Exception ex) {
            getLog().addOutputLine(ex.getMessage(), "ERROR");
            this.urlUP = "ERROR";
        }
    }
    
    
    public String getAccesible() {
        return this.accesible;
    }

    public String getValid() {
        return valid;
    }

    public String getUrlUP() {
        return urlUP;
    }

    public String getNotRedirects() {
        return notRedirects;
    }

    public String getSize() {
        return size;
    }

    public String getTotalLinks() {
        return String.valueOf(totalLinks);
    }

    public LogController getLog() {
        return log;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getLastmodifiedPage() {
        return new Date(this.lastmodifiedPage).toString();
    }

    public String getStatus() {
        return status;
    }
}
