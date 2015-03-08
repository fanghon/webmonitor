package linkcrawler.log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
public class LogController {    
    private Date startDate;
    private Date endDate = null;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private DateFormat dateFormatFile = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
    private FileWriter fw; 
    private Boolean isEmpty = false; 
    public LogController(boolean createLogFile)
    {
        if(createLogFile)
        {
            Date date = new Date();
            String logDir = System.getProperty("user.dir") + File.separatorChar + "Logs";
            String logFile = logDir + File.separatorChar + "Log-" + dateFormatFile.format(date) + ".txt";
            try {                
                File logDirPointer = new File(logDir);
                if(!logDirPointer.exists())
                {
                    logDirPointer.mkdir();
                }
                File logFilePointer = new File(logFile);
                logFilePointer.createNewFile();                
                fw = new FileWriter(logFilePointer);
                startDate = new Date();
                String logOutput = "LinkCrawler - Log \r\n"
                + "******************************\r\n\r\n"
                + "Activity starts at " + dateFormat.format(startDate) + ""
                + "\r\n";
                fw.write(logOutput);
                fw.flush();
            } catch (IOException ex) {
                System.out.println(ex.getMessage() + " " + logDir + " " + logFile);
            }
        }
        else
        {
        	isEmpty = true;
        }        
    }    
    public boolean isItEmpty()
    {
    	return isEmpty;
    }    
    public void addOutputLine(String line, String type)
    {
        Date date = new Date();
        try {
            fw.append(dateFormat.format(date) + " - " + type.toUpperCase() + " - " + line + "\r\n");
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(LogController.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }    
    public void endLog()
    {
        try {
            endDate = new Date();
            String logOutput = "\r\n\r\nActivity ends at " + dateFormat.format(endDate);
            fw.append(logOutput);
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            Logger.getLogger(LogController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}