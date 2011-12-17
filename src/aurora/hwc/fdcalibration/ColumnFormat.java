package aurora.hwc.fdcalibration;

public class ColumnFormat {
	public int laneblocksize;
	public int flwoffset;
	public int occoffset;
	public int spdoffset;
	public int maxlanes;
	public boolean hasheader;
	public String delimiter;

	public ColumnFormat(String delimiter,int laneblocksize, int flwoffset,int occoffset,int spdoffset, int maxlanes,boolean hasheader) {
		super();
		this.delimiter = delimiter;	
		this.laneblocksize = laneblocksize;
		this.flwoffset = flwoffset;
		this.occoffset = occoffset;
		this.spdoffset = spdoffset;
		this.maxlanes = maxlanes;
		this.hasheader = hasheader;
	}
}
