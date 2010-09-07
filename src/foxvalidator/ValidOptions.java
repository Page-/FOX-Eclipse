package foxvalidator;

import java.util.Map;

import org.w3c.dom.Node;

public class ValidOptions {
	public final Map<String,Node> mValidValues;
	public final CachedFile mCachedFile;
	public ValidOptions(Map<String,Node> pValidValues, CachedFile pCachedFile) {
	  this.mValidValues = pValidValues;
	  this.mCachedFile = pCachedFile;
  }
}
