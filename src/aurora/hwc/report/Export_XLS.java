/**
 * @(#)Export_XLS.java
 */

package aurora.hwc.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;


/**
 * Exporter for MS Excel.
 * @author Gabriel Gomes
 */
public class Export_XLS extends AbstractExporter {

    private Workbook wb = new HSSFWorkbook();
	private FileOutputStream fileOut;
	private Plotter plotter = new Plotter();

	@Override
	protected void openFile(File file) {
		try {
			fileOut = new FileOutputStream(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void closeFile() {
	    try {
			wb.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exportSlide(Slide S) {

		int i,j,k,m;
		int rowcount = 0;
	    int colcount;
		Row row,xrow,yrow,zrow;
	    String legend;

	    JFreeChart chart = plotter.makeSlideChart(S);
	    Sheet sheet = wb.createSheet(String.format("%d",S.id));
		
	    // slide title
	    row = sheet.createRow(rowcount++);
	    row.createCell(0).setCellValue("Slide title:");
	    row.createCell(1).setCellValue(S.title);

	    // loop through plots
	    for(i=0;i<S.plots.size();i++){
	    	
		    rowcount++;
		    row = sheet.createRow(rowcount++);
		    row.createCell(0).setCellValue(String.format("Plot %d",i+1));
		    row.createCell(1).setCellValue(S.plots.get(i).type.toString());

		    row = sheet.createRow(rowcount++);
		    row.createCell(0).setCellValue("X label:");
		    row.createCell(1).setCellValue(S.plots.get(i).xlabel);
		    row = sheet.createRow(rowcount++);
		    row.createCell(0).setCellValue("Y label:");
		    row.createCell(1).setCellValue(S.plots.get(i).ylabel);

		    if(S.plots.get(i).zlabel!=null){
			    row = sheet.createRow(rowcount++);
			    row.createCell(0).setCellValue("Z label:");
			    row.createCell(1).setCellValue(S.plots.get(i).zlabel);
		    }
		    
		    XYPlot plot = null;
		    if(S.plots.size()>1){
				CombinedDomainXYPlot cplot = (CombinedDomainXYPlot) chart.getXYPlot();
				plot = (XYPlot) cplot.getSubplots().get(i);
		    }
		    else{
		    	plot =  chart.getXYPlot();
		    }
		    
		    // loop through elements
	    	for(j=0;j<S.plots.get(i).elements.size();j++){
				PlotElement e = S.plots.get(i).elements.get(j);

				rowcount++;
			    row = sheet.createRow(rowcount++);
			    row.createCell(0).setCellValue(String.format("Element %d",j+1));
			    row.createCell(1).setCellValue(e.type.toString());
			    
				switch(e.type){
				
				case contour:
					
					DefaultXYZDataset xyzdataset = (DefaultXYZDataset) plot.getDataset();					
					colcount = 0;
					xrow = sheet.createRow(rowcount++);
					yrow = sheet.createRow(rowcount++);
					zrow = sheet.createRow(rowcount++);
					for(m=0;m<xyzdataset.getItemCount(0);m++){
						xrow.createCell(colcount).setCellValue(xyzdataset.getX(0,m).toString());
						yrow.createCell(colcount).setCellValue(xyzdataset.getY(0,m).toString());
						zrow.createCell(colcount).setCellValue(xyzdataset.getZ(0,m).toString());
						colcount++;
						if(colcount>=254)
							break;
					}
					
					break;

				case multiline:
				case scatter:

					DefaultXYDataset xydataset = (DefaultXYDataset) plot.getDataset();
					
					// loop through XY series
					for(k=0;k<xydataset.getSeriesCount();k++){
						colcount = 0;
						xrow = sheet.createRow(rowcount++);
						yrow = sheet.createRow(rowcount++);
						legend = xydataset.getSeriesKey(k).toString();
	
						xrow.createCell(colcount).setCellValue(legend);
						yrow.createCell(colcount).setCellValue(legend);
						colcount++;
	
						xrow.createCell(colcount).setCellValue("X:");
						yrow.createCell(colcount).setCellValue("Y:");
						colcount++;

						for(m=0;m<xydataset.getItemCount(k);m++){
							xrow.createCell(colcount).setCellValue(xydataset.getX(k,m).toString());
							yrow.createCell(colcount).setCellValue(xydataset.getY(k,m).toString());
							colcount++;
						}	
					}
					break;

				case minmax:
					System.out.println("ERROR: NOT IMPLEMENTED");
					break;
				}

	    	}
	    }
	}

	@Override
	protected void exportSectionHeader(ReportSection S) {
		wb.createSheet(S.title);
	}

}
