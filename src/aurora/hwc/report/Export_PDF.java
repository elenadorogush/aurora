/**
 * @(#)Export_PDF.java
 */

package aurora.hwc.report;

import java.awt.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.*;
import org.jfree.chart.JFreeChart;
import com.lowagie.text.ChapterAutoNumber;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;


/**
 * Exporter for PDF format.
 * @author Gabriel Gomes
 */
public class Export_PDF extends AbstractExporter {

	private Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
	private PdfWriter docWriter;
	private Plotter plotter = new Plotter();
	
	@Override
	protected void closeFile() {
		doc.close();
	}

	@Override
	protected void exportSlide(Slide S) {

		float p_height,p_width,width,height;
		
		JFreeChart chart = plotter.makeSlideChart(S);
		p_height = Math.min( 0.3f*S.plots.size() , 0.8f );
		if(S.plots.get(0).showlegend)
			p_height = Math.min( p_height+0.2f , 0.8f );
		p_width = 0.8f;
		Rectangle pagesize = PageSize.A4;
		width = pagesize.getWidth()*p_width;
		height = pagesize.getHeight()*p_height;
		
		try {
			doc.newPage();
			doc.add(new Paragraph(S.title,FontFactory.getFont(FontFactory.HELVETICA,16, Font.BOLDITALIC, new Color(0,0,0))));
			PdfContentByte contentByte = docWriter.getDirectContent();
			PdfTemplate temp = contentByte.createTemplate(width,height);
			Graphics2D g2d = temp.createGraphics(width,height, new DefaultFontMapper());
			Rectangle2D r2d = new Rectangle2D.Double(0,0,width,height);
			chart.draw(g2d,r2d);
			g2d.dispose();
			contentByte.addTemplate(temp,0.5f*(1f-p_width)*pagesize.getWidth(),0.5f*(1f-p_height)*pagesize.getHeight());
		} catch (DocumentException e) {
			e.printStackTrace();
		}
       
	}

	@Override
	protected void openFile(File file) {
		
    	try {
    		FileOutputStream pdfstream = new FileOutputStream(file);
			docWriter = PdfWriter.getInstance(doc, pdfstream);
	    	doc.addProducer();
	    	doc.addCreator(this.getClass().getName());
	      	doc.addTitle("jfreechart pdf");
	      	doc.setPageSize(PageSize.LETTER);
			//doc.setPageSize(new Rectangle(width,height));
			//doc.setMargins(50, 50, 50, 50);
			//doc.addAuthor("JFreeChart");
			//doc.addSubject("Demonstration");
	        doc.open();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exportSectionHeader(ReportSection S) {
		try {
			Paragraph p = new Paragraph(S.title,FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLDITALIC, new Color(0, 0, 255))); 
			doc.add(new ChapterAutoNumber(p));
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

}
