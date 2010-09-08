package foxvalidator;

import java.util.Map;


public class ValidOptions {
	public final Map<String,ValidValues> mValidValues;
	public final CachedFile mCachedFile;
	public ValidOptions(Map<String,ValidValues> pValidValues, CachedFile pCachedFile) {
	  this.mValidValues = pValidValues;
	  this.mCachedFile = pCachedFile;
  }
	
}