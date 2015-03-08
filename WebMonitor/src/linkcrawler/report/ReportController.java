package linkcrawler.report;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import linkcrawler.datatypes.LinkStatus;
import linkcrawler.datatypes.LinkTypes;
import linkcrawler.datatypes.UrlReport;
public class ReportController extends Thread {    
    private String nameOfSite;
    private ArrayList<UrlReport> reports;
    private String reportLocation = "";
    private String status = "Generating Report.....";
    private boolean success = true;
    private String exception = "";
    private ReportType rt = ReportType.HTML;
    public ReportController(String nameOfSite, ArrayList<UrlReport> reports)
    {
        this.nameOfSite = nameOfSite;
        this.reports = reports;
    }
    public ReportController(String nameOfSite, ArrayList<UrlReport> reports, ReportType rt)
    {
        this.nameOfSite = nameOfSite;
        this.reports = reports;
        this.rt = rt;
    }
    @Override
    public void run() {
        if(this.rt.equals(ReportType.EXCEL))
        {
            generateExcelReport();
        }
        else if(this.rt.equals(ReportType.JSON))
        {
            generateJSONReport();
        }
        else
        {
            try {
                generateHTMLReport();
            } catch (Exception ex) {
                Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }    
    public void generateJSONReport()
    {
    	if(this.reports.isEmpty())
        {
            this.status = "There is no data yet";
            this.success = false;
            this.exception = "Please run linkcrawler at least once in order to be able to generate a report";
            return;
        }
        try {
            status = "Generating report for site: " + nameOfSite;
            //Preparing directories
            String reportDir = System.getProperty("user.dir") + File.separatorChar + "Reports";            
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");            
            String reportDirFile = reportDir + File.separatorChar + "Report_" + dateFormat.format(date);
            reportLocation = reportDirFile;
            String reportSybDir = reportDirFile + File.separatorChar + "resources";
            File reportDirPointer = new File(reportDir);
            File reportDirFilePointer = new File(reportDirFile);
            File reportSubDirPointer = new File(reportSybDir);
            //Creating directories if not there
            if(!reportSubDirPointer.exists())
            {
                reportDirPointer.mkdir();
                reportDirFilePointer.mkdir();
                reportSubDirPointer.mkdir();
            }
            int counter = 1;
            for (UrlReport report : reports)
            {
                status = "Generating report for " +report.getPageUrl();
                String nameOfReport = "report_"+ counter +".json";
                File subReportFile =new File(reportSybDir + File.separatorChar + nameOfReport);     
                subReportFile.createNewFile();
                FileWriter fw; fw = new FileWriter(subReportFile);
                fw.write(report.toJSON());
                fw.close();                    
                counter++;
            }            
            reportLocation = reportDirFile;
            status = "Report generated";
        } catch (IOException ex) {
            status = "Unable to create report";
            this.exception = ex.getMessage();
            success = false;
        }        
    }    
    public void generateExcelReport()
    {
        if(this.reports.isEmpty())
            {
                this.status = "There is no data yet";
                this.success = false;
                this.exception = "Please run linkcrawler at least once in order to be able to generate a report";
                return;
            }
        try {            
            String reportDir = System.getProperty("user.dir") + File.separatorChar + "Reports";
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
            String reportDirFile = reportDir + File.separatorChar + "Report_" + dateFormat.format(date);
            reportLocation = reportDirFile;            
            String reportExcelFile = reportDirFile + File.separatorChar + "ReportExcel.xls";            
            File reportDirPointer = new File(reportDir);
            File reportDirFilePointer = new File(reportDirFile);            
            //Creating directories if not there
            if(!reportDirFilePointer.exists())
            {
                reportDirPointer.mkdir();
                reportDirFilePointer.mkdir();                
            }
            File reportExcelFilePointer = new File(reportExcelFile);
            reportExcelFilePointer.createNewFile();            
            WritableWorkbook workbook = Workbook.createWorkbook(reportExcelFilePointer);            
            int sheetNumber = 0;
            for(UrlReport ur : reports)
            {
                WritableSheet sheet = workbook.createSheet("Crawled Url " + (sheetNumber + 1), sheetNumber++);
                WritableFont blackHeadersFont = new WritableFont(WritableFont.ARIAL, 8, WritableFont.BOLD,false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE);
                WritableCellFormat blackHeadersFormatBackground = new WritableCellFormat();
                blackHeadersFormatBackground.setBackground(Colour.BLUE) ;
                blackHeadersFormatBackground.setBorder(Border.ALL, BorderLineStyle.THIN,Colour.BLACK);
                blackHeadersFormatBackground.setFont(blackHeadersFont);
                blackHeadersFormatBackground.setAlignment(Alignment.LEFT);
                WritableFont whiteValueFont = new WritableFont(WritableFont.ARIAL, 8, WritableFont.BOLD,false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
                WritableCellFormat whiteValueFormatBackground = new WritableCellFormat();
                whiteValueFormatBackground.setBackground(Colour.WHITE);
                whiteValueFormatBackground.setBorder(Border.ALL, BorderLineStyle.THIN,Colour.BLACK);
                whiteValueFormatBackground.setFont(whiteValueFont);
                whiteValueFormatBackground.setAlignment(Alignment.LEFT);
                Label introLabel = new Label(0, 0, "Site:", blackHeadersFormatBackground);
                Label introLabelValue = new Label(1, 0, ur.getPageUrl()+"         ", whiteValueFormatBackground);
                sheet.addCell(introLabel);
                sheet.addCell(introLabelValue);  
                int staringRow = 0;                
                staringRow = prepareLinksDataDetail(sheet, ur, staringRow, LinkTypes.INTERNAL, "Internal links   ", blackHeadersFormatBackground, whiteValueFormatBackground);
                staringRow = prepareLinksDataDetail(sheet, ur, staringRow, LinkTypes.EXTERNAL, "External links   ", blackHeadersFormatBackground, whiteValueFormatBackground);
                staringRow = prepareLinksDataDetail(sheet, ur, staringRow, LinkTypes.SPECIAL, "Special links   ", blackHeadersFormatBackground, whiteValueFormatBackground);
                staringRow = prepareLinksDataDetail(sheet, ur, staringRow, LinkTypes.IMAGES, "Images   ", blackHeadersFormatBackground, whiteValueFormatBackground);
                 for(int x=0; x < sheet.getColumns(); x++)
                {
                    CellView cell=sheet.getColumnView(x);
                    cell.setAutosize(true);
                    sheet.setColumnView(x, cell);
                }                
            }       
            workbook.write(); 
            workbook.close();
            this.status = "Excel report generated";
        } catch (Exception ex) {
            this.status = "Error when generating Excel File";
            this.success = false;
            this.exception = ex.getMessage();
        }
    }    
    private int prepareLinksDataDetail(WritableSheet sheet, UrlReport ur, int startingRow, LinkTypes lt, String label, WritableCellFormat blackHeadersFormatBackground, WritableCellFormat whiteValueFormatBackground) throws Exception
    {
                startingRow += 2;
                sheet.addCell(new Label(0, startingRow++, label, whiteValueFormatBackground));              
                sheet.addCell(new Label(0, startingRow, "URL Link", blackHeadersFormatBackground));
                sheet.addCell(new Label(1, startingRow++, "Status", blackHeadersFormatBackground));                
                if(lt.equals(LinkTypes.INTERNAL))
                {
                    if(ur.haveInternalLinks())
                    {
                        for(LinkStatus ls: ur.getInternalLinks())
                        {                        
                            String[] statusOfLink = ls.getStatusInfoInArray();
                            sheet.addCell(new Label(0, startingRow, statusOfLink[0], whiteValueFormatBackground));
                            sheet.addCell(new Label(1, startingRow++, statusOfLink[1], whiteValueFormatBackground));
                        }
                    }
                    else
                    {
                        sheet.addCell(new Label(0, startingRow++, "No data collected", whiteValueFormatBackground));
                    }
                }
                else if(lt.equals(LinkTypes.EXTERNAL))
                {
                    if(ur.haveExternalLinks())
                    {
                        for(LinkStatus ls: ur.getExternalLinks())
                        {                        
                            String[] statusOfLink = ls.getStatusInfoInArray();
                            sheet.addCell(new Label(0, startingRow, statusOfLink[0], whiteValueFormatBackground));
                            sheet.addCell(new Label(1, startingRow++, statusOfLink[1], whiteValueFormatBackground));
                        }
                    }
                    else
                    {
                        sheet.addCell(new Label(0, startingRow++, "No data collected", whiteValueFormatBackground));
                    }
                }
                else if(lt.equals(LinkTypes.SPECIAL))
                {
                    if(ur.haveSpecialLinks())
                    {
                        for(LinkStatus ls: ur.getSpecialLinks())
                        {                        
                            String[] statusOfLink = ls.getStatusInfoInArray();
                            sheet.addCell(new Label(0, startingRow, statusOfLink[0], whiteValueFormatBackground));
                            sheet.addCell(new Label(1, startingRow++, statusOfLink[1], whiteValueFormatBackground));
                        }
                    }
                    else
                    {
                        sheet.addCell(new Label(0, startingRow++, "No data collected", whiteValueFormatBackground));
                    }
                }
                else if(lt.equals(LinkTypes.IMAGES))
                {
                    if(ur.haveImages())
                    {
                        for(LinkStatus ls: ur.getImagesSrc())
                        {                        
                            String[] statusOfLink = ls.getStatusInfoInArray();
                            sheet.addCell(new Label(0, startingRow, statusOfLink[0], whiteValueFormatBackground));
                            sheet.addCell(new Label(1, startingRow++, statusOfLink[1], whiteValueFormatBackground));
                        }
                    }
                    else
                    {
                        sheet.addCell(new Label(0, startingRow++, "No data collected", whiteValueFormatBackground));
                    }
                }       
               return startingRow;
    }
    
    private void generateHTMLReport() throws Exception
    {
        if(this.reports.isEmpty())
            {
                this.status = "There is no data yet";
                this.success = false;
                this.exception = "Please run linkcrawler at least once in order to be able to generate a report";
                return;
            }
            try {

                status = "Generating report for site: " + nameOfSite;
                String reportDir = System.getProperty("user.dir") + File.separatorChar + "Reports";                
                Date date = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");                
                String reportDirFile = reportDir + File.separatorChar + "Report_" + dateFormat.format(date);
                reportLocation = reportDirFile;
                String reportSybDir = reportDirFile + File.separatorChar + "jsonData";
                File reportDirPointer = new File(reportDir);
                File reportDirFilePointer = new File(reportDirFile);
                File reportSubDirPointer = new File(reportSybDir);
                if(!reportSubDirPointer.exists())
                {
                    reportDirPointer.mkdir();
                    reportDirFilePointer.mkdir();
                    reportSubDirPointer.mkdir();
                }   
                int goodlinks = 0;
                int badlinks = 0;
                HashMap<String, Integer> contentStatistics = new HashMap<String, Integer>();
                String jsonReportsSection = "";
                for (UrlReport report : reports)
                {
                	goodlinks = goodlinks + report.getGoodLinks();
                	badlinks = badlinks + report.getBadLinks();
                	for(Entry<String, Integer> entry : report.getContentTypeStatistics().entrySet()) {
                	    String key = entry.getKey();
                	    Integer value = entry.getValue();

                	    if(!contentStatistics.containsKey(key))
                	    {
                	    	contentStatistics.put(key, value);
                	    }
                	    else
                	    {
                	    	contentStatistics.put(key, contentStatistics.get(key) + value);
                	    }
                	}                	
                	jsonReportsSection+=report.toJSON()+", ";
                }
                jsonReportsSection = jsonReportsSection.substring(0, jsonReportsSection.length() - 2);
                
                String jsonFile = "var jsonDATA = { \"statistics\" : ["
                		+ "{\"topic\" : \"Total Links\", \"value\": "+ (goodlinks + badlinks) +" , \"type\" : \"Global Statistics\" },"
                		+ "{\"topic\" : \"Good Links\", \"value\": "+ goodlinks +" , \"type\" : \"Global Statistics\" }," +
                				  "{\"topic\" : \"Bad Links\" , \"value\": "+ badlinks +" , \"type\" : \"Global Statistics\"} ,";
                
                if(contentStatistics.size() == 0)
                {
                	jsonFile += "{ \"topic\": \"No data for ContentType\", \"value\": \"No Data\", \"type\" : \"Content Type Statistics\"},";
                }
                else
                {
                	for(Entry<String, Integer> entry : contentStatistics.entrySet()) {
                	    String key = entry.getKey();
                	    Integer value = entry.getValue();
                	    
                	    jsonFile += "{\"topic\" : \""+ key +"\", \"value\": "+ value +", \"type\" : \"Content Type Statistics\"}, ";                	    
                    }
                	jsonFile = jsonFile.substring(0, jsonFile.length() - 2);
                }                
                jsonFile += "],";                
                jsonFile += "\"reports\" : [";
                jsonFile += jsonReportsSection;
                jsonFile += "]};";                
                status = "Generating JSON DATA ";
                File subReportFile =new File(reportSybDir + File.separatorChar + "jsonData.js");     
                subReportFile.createNewFile();
                FileWriter fw; fw = new FileWriter(subReportFile);
                fw.write(jsonFile);
                fw.close();                    
                ZipInputStream zis = new ZipInputStream(new FileInputStream(getHTMLReportZIPFile()));
                ZipEntry entry = null;
                OutputStream out = null;
                while((entry = zis.getNextEntry()) != null){
					String fileName = entry.getName();
					if(entry.isDirectory())
					{
						new File(reportDirFile, fileName).mkdirs();
					}
					else
					{
						out = new FileOutputStream(new File(reportDirFile,fileName));
				          byte[] buf = new byte[1024];
				          int len;
				          while ((len = zis.read(buf)) > 0) {
				            out.write(buf, 0, len);
				          }
				          out.close();
					}
                }             
                zis.closeEntry();
                zis.close();               
                reportLocation = reportDirFile;
                status = "Report generated";
            } catch (IOException ex) {
                status = "Unable to create report";
                this.exception = ex.getMessage();
                success = false;
            }          
    }    
    private File getHTMLReportZIPFile()
    {    	
        InputStream inZip = ReportController.class.getResourceAsStream("htmlviewer.zip");
        File tempZIP = new File("tempzip.zip");
        try
	    {
        	if(tempZIP.exists())
        	{
        		tempZIP.delete();
        	}
	        OutputStream os = new FileOutputStream(tempZIP);
	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while((bytesRead = inZip.read(buffer)) !=-1){
	            os.write(buffer, 0, bytesRead);
	        }
	        inZip.close();
	        os.flush();
	        os.close();
	    }
        catch(Exception e)
        {
        	e.printStackTrace();
        }        
        return tempZIP;
    }
    public String getReportLocation() {
        return reportLocation;
    }
    public boolean isSuccess() {
        return success;
    }
    public String getException() {
        return exception;
    }
    public String getStatus() {
        return status;
    }
}
