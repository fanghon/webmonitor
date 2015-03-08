package linkcrawler.datatypes;
public class LinkStatus {    
    private String href;
    private String httpCode;  //http响应状态码
    private String contentType;
    private URLObject urlLocation;
    private boolean image = false;    
    public LinkStatus(String href, URLObject urlLocation, String httpCode)
    {
        this(href, urlLocation);        
        this.httpCode = httpCode;        
    }    
    public LinkStatus(String href, URLObject urlLocation, boolean image)
    {
        this(href, urlLocation); 
        this.image = image;
    }   
    public LinkStatus(String href, URLObject urlLocation)
    {
        this.href = href;
        this.urlLocation = urlLocation;        
    }    
    public boolean isImage()
    {
        return image;
    } 
    public boolean isUp()
    {
        if(this.getHttpCode().matches(".*200.*|.*206.*|.*301.*|.*302.*|.*303.*|.*304.*"))
        {
            return true;
        }
        return false;
    }    
    public boolean isEmailAddress()
    {
        return this.getHttpCode().equals("Email Address");
    }    
    public boolean isSelfAnchor()
    {
        return this.getHttpCode().equals("Self Anchor");
    }
    public String getHrefUnformatted() {
        if (this.href.endsWith("/"))
        {
            return this.href.substring(0, this.href.length() - 1);
            
        }
        return this.href;
    }    
    public URLObject getHref() throws Exception {
        String fixedHref = this.href;        
        if (fixedHref.endsWith("/"))
        {
            fixedHref = fixedHref.substring(0, fixedHref.length() - 1);
        }        
        if (fixedHref.startsWith("/"))
        {
            if(urlLocation.toString().endsWith("/"))
            {                
               return new URLObject(this.urlLocation.toString().substring(0, this.urlLocation.toString().length() - 1) + fixedHref);             
            }
            return new URLObject(this.urlLocation + fixedHref);
        }        
        if(!fixedHref.startsWith("mailto:") && !fixedHref.startsWith("http") && !fixedHref.startsWith("javascript:"))
        {
            if(urlLocation.toString().endsWith("/"))
            {                 
                return new URLObject(this.urlLocation.toString().substring(0, this.urlLocation.toString().length() - 1) + "/" + fixedHref);
            }
            return new URLObject(this.urlLocation + "/" + fixedHref);
        }        
        return new URLObject(fixedHref);
    }    
    public boolean isInternalLink() throws Exception
    {
        boolean isInternal = this.getHref().toString().startsWith(this.urlLocation.toString());
        if(!isInternal && this.getHref().isSitePartOfSameDomain(urlLocation)) //子站连接，二级域名
            isInternal = true;
        return isInternal;
    }    
    public boolean isValidForCrawling() throws Exception
    {
        return this.getContentType().contains("text/html");
    }
    @Override
    public String toString() {
        return "Link Detail {" + "href=" + href + ", httpCode=" + getHttpCode() + '}';
    }    
    public String toJSON() {
        return "{\"url\": \"" + href + "\", \"status\": \""+ getHttpCode() +"\", \"contentType\": \""+ getContentType() +"\"}";
    }    
    public String toJSON(String linkType) {
        return "{\"url\": \"" + href + "\", \"status\": \""+ getHttpCode() +"\", \"contentType\": \""+ getContentType() +"\", \"type\": \""+ linkType +"\"}";
    }
    public String getHttpCode() {
        return httpCode;
    }
    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }    
    public void setContentType(String contentType)
    {
    	if(contentType.indexOf(";") > -1)
    	{
    		this.contentType = contentType.substring(0, contentType.indexOf(";"));
    	}
    	else
    	{
    		this.contentType = contentType;
    	}
    	if(this.contentType.trim().equals(""))
    	{
    		this.contentType = "[Not Specified]";
    	}
    }    
    public String getContentType()
    {
    	return this.contentType;
    }    
    public String[] getStatusInfoInArray()
    {
        return new String[]{href, getHttpCode()};
    }
    public boolean isIsSubdomain() {    	
        try {
			return getHref().getDomainName().equals(urlLocation.getDomainName());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    }    
}
