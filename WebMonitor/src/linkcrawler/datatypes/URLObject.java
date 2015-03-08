package linkcrawler.datatypes;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import linkcrawler.connectors.HTTPConnector;
public class URLObject {    
    private String protocol = "";  
    private String prefixes = "";    //主机名前缀，如www
    private String domainName = "";  // 域名
    private String domainSuffix = ""; //主机名域后缀如  gov.cn
    private String[] domainSuffixesArray = {"AC", "AD", "AE", "AERO", "AF", "AG", "AI", "AL", "AM", "AN", "AO", "AQ", "AR", "ARPA", "AS", "ASIA", "AT", "AU", "AW", "AX", "AZ", "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BIZ", "BJ", "BL", "BM", "BN", "BO", "BQ", "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CAT", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO", "COM", "COOP", "CR", "CU", "CV", "CW", "CX", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO", "DZ", "EC", "EDU", "EE", "EG", "EH", "ER", "ES", "ET", "EU", "FI", "FJ", "FK", "FM", "FO", "FR", "GA", "GB", "GD", "GE", "GF", "GG", "GH", "GI", "GL", "GM", "GN", "GOV", "GP", "GQ", "GR", "GS", "GT", "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IM", "IN", "INFO", "INT", "IO", "IQ", "IR", "IS", "IT", "JE", "JM", "JO", "JOBS", "JP", "KE", "KG", "KH", "KI", "KM", "KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME", "MF", "MG", "MH", "MIL", "MK", "ML", "MM", "MN", "MO", "MOBI", "MP", "MQ", "MR", "MS", "MT", "MU", "MUSEUM", "MV", "MW", "MX", "MY", "MZ", "NA", "NAME", "NC", "NE", "NET", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "ORG", "PA", "PE", "PF", "PG", "PH", "PK", "PL", "PM", "PN", "PR", "PRO", "PS", "PT", "PW", "PY", "QA", "RE", "RO", "RS", "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "SS", "ST", "SU", "SV", "SX", "SY", "SZ", "TC", "TD", "TEL", "TF", "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO", "TP", "TR", "TRAVEL", "TT", "TV", "TW", "TZ", "UA", "UG", "UK", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VN", "VU", "WF", "WS", "XXX", "YE", "YT", "ZA", "ZM", "ZW"};
    private ArrayList<String> resource;  //该url的子路径列表，如/jnjs/xx/  分成jnjs xx 根据此列表的大小，判断深度。
    boolean mainSiteOnly = false;  //该url对象是否是主站点
    private boolean offlinesite = false;  //是否是离线站点    
    public URLObject(String url) throws Exception
    {
        this(url, false);
    }    
    public URLObject(String url, boolean mainSiteOnly) throws Exception
    {
    	String old = url;
        url = url.trim();
        resource = new ArrayList<String>();
        if(url.indexOf("://") != -1)
        {
            url = this.extractProtocol(url);          //返回的是不包含协议的部分  
            if(url.indexOf("/") != -1) //有子路径如../jsjn
            {
                extractResourceSection(url.substring(url.indexOf("/") + 1, url.length()));
                url = url.substring(0, url.indexOf("/"));//主机名部分
            }
            url = extractSuffixesAndDomainName(url);            
            if(!url.equals(""))
                this.prefixes = url;            
        }
        else
        {
            throw new Exception("URL invalid: No protocol detected. ->" + old);
        }
        this.mainSiteOnly = mainSiteOnly;
    }
    // 返回url不包含协议的部分
    private String extractProtocol(String url)
    {
        this.protocol = url.split("://")[0];            
        url = url.replaceAll(this.protocol+"://", "");
        return url;
    }
    //根据主机名，返回主域名的前面部分，如：www.njqxq.gov.cn ,返回www，根据域名，还判断是否是离线站点
    private String extractSuffixesAndDomainName(String urlFragment)
    {
        String[] sections = urlFragment.split("\\.");
        for(int x = sections.length - 1; x >= 0; x--)
        {
            if(this.isDomainSuffix(sections[x]))
            {
                //removing this from urlFragment
                urlFragment = urlFragment.replaceAll("\\."+sections[x]+"$", "");
                if(!this.domainSuffix.equals(""))
                    this.domainSuffix = sections[x] + "." + this.getDomainSuffix();
                else
                    this.domainSuffix = sections[x];
            }
            else if(this.getDomainName().equals(""))
            {
                this.domainName = sections[x];
                urlFragment = urlFragment.replaceAll(sections[x]+"$", "");
                if(urlFragment.endsWith("."))
                {
                    urlFragment = urlFragment.replaceAll("\\.$", "");
                }
            }
        }
        if(this.getDomainSuffix().equals(""))
            offlinesite = true;
        return urlFragment;
    }    
    public int getDepthLevel()
    {
        return this.resource.size();
    }    
    public boolean isValidSitemapLocation()
    {
        URL siteURL = null;
        try {
            siteURL = new URL(this.getSitemapLocation());
                try {
                    URLConnection conn = siteURL.openConnection();
                    conn.connect();
                } catch (IOException ex) {
                    return false;
                }
        } catch (MalformedURLException ex) {
            return false;
        }
        return true;
    }    
    public String getSitemapLocation()
    {   
        String URLFragment = this.toString();
        if(this.resource.isEmpty())
        {
            if(URLFragment.endsWith("/"))
            {
                URLFragment += "sitemap.xml";
            }
            else
            {
                URLFragment += "/sitemap.xml";
            }
        }            
        return URLFragment;       
    }    
    private boolean isDomainSuffix(String possibleSuffix)
    {
        return Arrays.asList(domainSuffixesArray).contains(possibleSuffix.toUpperCase());
    }
    //将url子路径../xx/yy/..  分成xx,yy后，加入resource中
    private void extractResourceSection(String URLResourceSection)
    {
        if(URLResourceSection.indexOf("/") != -1)
        {
            this.resource.addAll(Arrays.asList(URLResourceSection.split("/")));
        }
        else
        {
            this.resource.add(URLResourceSection);
        }
    }
    @Override
    public String toString() {
        String urlFormatted = this.protocol + "://";
        if(!prefixes.equals(""))
            urlFormatted += prefixes + ".";       
        if(isOfflinesite() == true)
            urlFormatted += getDomainName();
        else
            urlFormatted += getDomainName() + "." + this.getDomainSuffix();        
        if(!mainSiteOnly)
        {
            if(!resource.isEmpty())
            {
                for(String resourceSection : resource)
                {
                    urlFormatted += "/" + resourceSection;
                }
            }        
        }
        if (urlFormatted.endsWith("/"))
        {
            return urlFormatted.substring(0, urlFormatted.length() - 1);            
        }
        return urlFormatted;
    }    
    // 检查主站点是否有效，url格式错、http连接异常均返回false
    public URLValidationCodes isValidMainSiteUrl()
    {
        String siteURL = this.toString();        
                try {
                	HttpURLConnection conn = new HTTPConnector(siteURL).getConnection();
                	conn.connect();
                } 
                catch (MalformedURLException ex) {
                    return URLValidationCodes.MALFORMED;
                }
                catch (IOException ex) {
                    System.out.println("Main site looks Unreachable");
                    if(!resource.isEmpty())  //主站有子路径
                    {                       
                        mainSiteOnly = false;
                        siteURL = this.toString();  //提取出不含子路径的url
                        mainSiteOnly = true;                        
                        try
                        {                
                            HttpURLConnection conn = new HTTPConnector(siteURL).getConnection();
                        	conn.connect();
                        }
                        catch(Exception e)
                        {
                            System.out.println("Got:");
                            System.out.println(e.toString());
                            return URLValidationCodes.UNREACHABLE;
                        }
                    }
                    else
                    {
                        return URLValidationCodes.UNREACHABLE;
                    }
                }        
        return URLValidationCodes.VALID;
    }    
    public String getMainSiteOnly()
    {
        String urlFormatted = this.protocol + "://";
        if(!prefixes.equals(""))
            urlFormatted += prefixes + ".";             
        if(isOfflinesite() == true)
            urlFormatted += getDomainName();
        else
            urlFormatted += getDomainName() + "." + this.getDomainSuffix();
        return urlFormatted;
    }
    public boolean isOfflinesite() {
        return offlinesite;
    }    
    public boolean isSitePartOfSameDomain(URLObject uo)
    {
        if(uo.isOfflinesite() && this.isOfflinesite())
        {
            return this.getDomainName().equals(uo.getDomainName());
        }
        else
        {
            return this.getDomainName().equals(uo.getDomainName()) && this.getDomainSuffix().equals(uo.getDomainSuffix());
        }
    }
    public String getDomainName() {
        return domainName;
    }
    public String getDomainSuffix() {
        return domainSuffix;
    }    
}
