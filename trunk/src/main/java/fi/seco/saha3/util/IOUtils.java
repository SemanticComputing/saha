package fi.seco.saha3.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;

/**
 * Contains static helper/utility methods, mostly for file interaction.
 * 
 * @author jlaitio
 *
 */
public class IOUtils {
			
	private final static Log log = LogFactory.getLog(IOUtils.class);
		
	// RDF language types in the order input files are checked for
    public static final String[] RDF_LANGUAGES = {
            "RDF/XML", "TTL", "N-TRIPLE", "N3"
    };  
	
	
	/**
	 * Reads the given rdf file from the file system
	 * 
	 * @param file The file
	 * @return A model of the file's contents
	 */
	public static Model readRDFFile(String file)
	{
	    if (file == null)
	        return null;
	    
		Model model = ModelFactory.createDefaultModel();
		
        byte[] fileBytes = null;  
        
        try
        {
            FileInputStream in = new FileInputStream(new File(file));
            fileBytes = new byte[in.available()];            
            in.read(fileBytes);
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException("File " + file + " not found.");
        }
        catch (IOException x)
        {
            log.error("IO Exception while reading a file: " + x.getMessage());      
            return model;
        }
        
        // Suppress unnecessary errors from RDF parsing as they are expected
        RDFDefaultErrorHandler.silent = true;
        
        for (int i = 0 ; i < RDF_LANGUAGES.length ; i++)
        {
            try
            {               
                ByteArrayInputStream byteIn = new ByteArrayInputStream(fileBytes);
                model.read(byteIn, null, RDF_LANGUAGES[i]);                
                break;
            }     
            catch (OutOfMemoryError e)
            {
                log.error("Ran out of memory while reading input file " + file);
                log.error("Maximum heap size might have to be increased to process this file.");
                return null;
            }
            catch (Exception c)
            {
                if (i + 1 < RDF_LANGUAGES.length)
                {
                    // Not valid for this RDF language, try next
                    log.debug(
                            "Input file was not language " + RDF_LANGUAGES[i]
                            + ", trying " + RDF_LANGUAGES[i+1] 
                            + " next. File name: " + file); 
                    
                }
                else
                    log.error("Input file was not language " + RDF_LANGUAGES[i] + " or any other known language.");
            }
        }
        
        RDFDefaultErrorHandler.silent = false;
		
		return model;
	}
	
	/**
     * Reads a byte array to a Jena model
     * 
     * @param bytes The array
     * @return A model of the array's contents
     */
    public static Model readRDFByteArray(byte[] bytes)
    {
        if (bytes == null)
            return null;
        
        Model model = ModelFactory.createDefaultModel();
                
        // Suppress unnecessary errors from RDF parsing as they are (sadly) expected
        RDFDefaultErrorHandler.silent = true;
        
        for (int i = 0 ; i < RDF_LANGUAGES.length ; i++)
        {
            try
            {               
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                model.read(byteIn, null, RDF_LANGUAGES[i]);                
                break;
            }            
            catch (Exception c)
            {
                if (i + 1 < RDF_LANGUAGES.length)
                {
                    // Not valid for this RDF language, try next
                    log.debug(
                            "Input byte array was not language " + RDF_LANGUAGES[i]
                            + ", trying " + RDF_LANGUAGES[i+1] 
                            + " next."); 
                    
                }
                else
                    log.error("Input byte array was not language " + RDF_LANGUAGES[i] + " or any other known language.");
            }
        }
        
        RDFDefaultErrorHandler.silent = false;
        
        return model;
    }
    
    public static Model readRDFByteArray(Byte[] array)
    {
        byte[] bytes = new byte[array.length];
        for (int i = 0 ; i < array.length ; i++)
            bytes[i] = array[i].byteValue();
        
        return readRDFByteArray(bytes);
    }

	/**
	 * Writes the given model out to the file system
	 * 
	 * @param model The model
	 * @param file The output file
	 */
	public static void writeRDFFile(Model model, String file)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(file));

			model.write(out, "RDF/XML");
			out.close();
			log.info("writing completed successfully");
		}
		catch (Exception e)
		{
			e.printStackTrace();			
		}		
	}
		
	/**
	 * Writes the given model out to the file system
	 * 
	 * @param model The model 
	 * @param file The output file
	 * @param type The RDF output syntax type
	 */
	public static void writeRDFFile(Model model, String file, String type)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(file));

			model.write(out, type);
			out.close();
			log.info("writing completed successfully");
		}
		catch (Exception e)
		{
			e.printStackTrace();			
		}		
	}
	
   
}
