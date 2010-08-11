package foxvalidator;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CachedFile
{
	public final String mFilePath;
	public long mLastModified; //This is not final to facilitate the updating of timestamp if no actual changes to the file were made.
	public final Element mDocElem;
	public final Map<String,String>[] mValidValues = new Map[X.mValidateChecks.length];
	public final NodeList[] mCheckNodeList = new NodeList[X.mValidateChecks.length];
	
	public CachedFile (String pFilePath, long pLastModified, Element pDocElem)
	{
		this.mFilePath = pFilePath;
		this.mLastModified = pLastModified;
		this.mDocElem = pDocElem;
	}
}