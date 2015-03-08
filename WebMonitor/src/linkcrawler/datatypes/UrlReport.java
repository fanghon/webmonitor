package linkcrawler.datatypes;
import java.util.ArrayList;
import java.util.HashMap;
public class UrlReport {    
    private ArrayList<LinkStatus> internalLinks; //内部链接
    private ArrayList<LinkStatus> externalLinks; //外部链接   
    private ArrayList<LinkStatus> specialLinks; //特殊链接 email https
    private ArrayList<LinkStatus> imagesSrc; //图片链接
    private int goodLinks = 0;
    private int badLinks = 0;
    private String currentPage;
    HashMap<String, Integer> contentTypeStats;    
    public UrlReport(String currentPage)
    {
        this.currentPage = currentPage;
        this.internalLinks = new ArrayList<LinkStatus>();
        this.externalLinks = new ArrayList<LinkStatus>();
        this.specialLinks = new ArrayList<LinkStatus>();
        this.imagesSrc = new ArrayList<LinkStatus>();
        contentTypeStats = new HashMap<String, Integer>();
    }    
    public void addLink(LinkStatus checkedLink) throws Exception
    {    	
        if(checkedLink.isInternalLink() && !checkedLink.isSelfAnchor() && !checkedLink.isImage())
        {
            this.getInternalLinks().add(checkedLink);
        }
        else if(checkedLink.isEmailAddress() && !checkedLink.isImage())
        {
            this.getSpecialLinks().add(checkedLink);
        }
        else if(checkedLink.isSelfAnchor() && !checkedLink.isImage())
        {
            this.getSpecialLinks().add(checkedLink);
        }
        else if(checkedLink.isImage())
        {
            getImagesSrc().add(checkedLink);
        } 
        else
        {
            this.getExternalLinks().add(checkedLink);
        }
        
        if(checkedLink.isUp())
    	{
    		goodLinks++;
    	}
    	else if(!checkedLink.isSelfAnchor() && !checkedLink.isEmailAddress() && !checkedLink.isUp())
    	{
    		badLinks++;
    	}        
        if(!contentTypeStats.containsKey(checkedLink.getContentType()))
		{
			contentTypeStats.put(checkedLink.getContentType().trim().toLowerCase(), 1);
		}
		else
		{
			contentTypeStats.put(checkedLink.getContentType().trim().toLowerCase(), contentTypeStats.get(checkedLink.getContentType().trim().toLowerCase()) + 1);
		}
    }
    public int[] getShortDetails()
    {
        return new int[]{this.getInternalLinks().size(), this.getExternalLinks().size(), this.getSpecialLinks().size()};
    }    
    public boolean haveImages()
    {
        return !this.imagesSrc.isEmpty();
    }    
    public boolean haveInternalLinks()
    {
        return !this.internalLinks.isEmpty();
    }    
    public boolean haveExternalLinks()
    {
        return !this.externalLinks.isEmpty();
    }    
    public boolean haveSpecialLinks()
    {
        return !this.specialLinks.isEmpty();
    }    
    public String getPageUrl()
    {
        return this.currentPage;
    }    
    @Override
    public String toString() {
        String reportInTxt = "URL = " + this.currentPage + "\n\r"
                + "\n\r -- Internal Links --\n\r ";
        
        if(this.getInternalLinks().isEmpty())
        {
             reportInTxt += "No internal links detected";
        }
        else
        {
            for(LinkStatus link : this.getInternalLinks())
            {
                 reportInTxt += link.toString() + "\n\r";
            }
        }
        
        reportInTxt += "\n\r -- External Links --\n\r ";
        
        if(this.getExternalLinks().isEmpty())
        {
             reportInTxt += "No external links detected";
        }
        else
        {
            for(LinkStatus link : this.getExternalLinks())
            {
                 reportInTxt += link.toString() + "\n\r";
            }
        }
        return reportInTxt;
    }    
    public int getGoodLinks()
    {
    	return goodLinks;
    }    
    public int getBadLinks()
    {
    	return badLinks;
    }    
    public HashMap<String, Integer> getContentTypeStatistics()
    {
    	return contentTypeStats;
    }    
    public String toJSON() {
        String reportInTxt = "{"
        		+ "\"id\": \"" + this.currentPage +"\","
        				+ "\"Links\": [ ";        
        for(LinkStatus link : this.getInternalLinks())
        {
             reportInTxt += link.toJSON("Internal Links")+", ";
        }
        for(LinkStatus link : this.getExternalLinks())
        {
        	  reportInTxt += link.toJSON("External Links")+", ";
        }
        for(LinkStatus link : this.getImagesSrc())
        {
        	  reportInTxt += link.toJSON("Images")+", ";
        }
        for(LinkStatus link : this.getSpecialLinks())
        {
        	  reportInTxt += link.toJSON("Special Links")+", ";
        }        
        reportInTxt = reportInTxt.substring(0, reportInTxt.length() - 2);
        reportInTxt += "]}";        
        return reportInTxt;
    }
    public ArrayList<LinkStatus> getInternalLinks() {
        return internalLinks;
    }
    public ArrayList<LinkStatus> getExternalLinks() {
        return externalLinks;
    }
    public ArrayList<LinkStatus> getSpecialLinks() {
        return specialLinks;
    }
    public ArrayList<LinkStatus> getImagesSrc() {
        return imagesSrc;
    }
    public void setImagesSrc(ArrayList<LinkStatus> imagesSrc) {
        this.imagesSrc = imagesSrc;
    }    
}
