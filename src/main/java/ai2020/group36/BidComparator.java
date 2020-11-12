package ai2020.group36;

import java.util.Comparator;

import geniusweb.issuevalue.Bid;
import geniusweb.profile.utilityspace.UtilitySpace;

public class BidComparator implements Comparator<Bid>{

	private UtilitySpace utilitySpace;
	
	public BidComparator(UtilitySpace utilitySpace)
	{
		this.utilitySpace = utilitySpace;
	}
	
	public int compare(Bid a, Bid b)
	{
		 return Double.compare(utilitySpace.getUtility(a).doubleValue(), utilitySpace.getUtility(b).doubleValue());
	}
}
