/**
 * @(#)Export_PPT.java
 */

package aurora.hwc.report;

import java.io.*;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.SlideMaster;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.jfree.chart.JFreeChart;


/**
 * Exporter for MS PowerPoint.
 * @author Gabriel Gomes
 */
public class Export_PPT extends AbstractExporter {

	private SlideShow ppt;
	private SlideMaster [] masters;
	private FileOutputStream out;
	private Plotter plotter = new Plotter();

	@Override
	protected void closeFile() {
		try {
	  		ppt.write(out);
	  		out.close();	
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} 
	}

	@Override
	protected void exportSlide(Slide S) {

		JFreeChart chart = plotter.makeSlideChart(S);
		chart.setTitle("");
		File file = new File(S.title + ".png");
		plotter.savecharttoPNG(chart,file);
		
		int idx;
		try {
			idx = ppt.addPicture(file,Picture.PNG);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		file.delete();
		Picture pict = new Picture(idx);
		pict.setAnchor(new java.awt.Rectangle(100, 100, 500, 350));
		
		org.apache.poi.hslf.model.Slide slide = ppt.createSlide();
  		slide.setMasterSheet(masters[2]);
  		slide.addShape(pict);

		TextBox txt = new TextBox();
		txt.setText(S.title);
		txt.setAnchor(new java.awt.Rectangle(5, 20, 700, 50));
		RichTextRun rt = txt.getTextRun().getRichTextRuns()[0];
		rt.setFontSize(24);
		rt.setAlignment(TextBox.AlignRight);
		slide.addShape(txt);
	}

	@Override
	protected void exportSectionHeader(ReportSection S) {

		org.apache.poi.hslf.model.Slide slide = ppt.createSlide();
  		slide.setMasterSheet(masters[1]);

		TextBox txt = new TextBox();
		txt.setText(S.title);
		txt.setAnchor(new java.awt.Rectangle(20, 190, 650, 50));
		RichTextRun rt = txt.getTextRun().getRichTextRuns()[0];
		rt.setFontSize(24);
		rt.setAlignment(TextBox.AlignRight);
		slide.addShape(txt);	
	}

	@Override
	protected void openFile(File file){
		try {
			out = new FileOutputStream(file.getAbsolutePath());
			ppt = new SlideShow(new HSLFSlideShow(System.getenv("AURORA_CLASS_PREFIX") + "/ppttemplate.pot"));
			masters = ppt.getSlidesMasters();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
