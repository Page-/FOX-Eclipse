package foxvalidator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xpath.NodeSet;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionContainer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@SuppressWarnings("restriction")
public class X {

	protected final static DOMAddLines dal = new DOMAddLines();
	protected final static DocumentBuilder db;
	protected final static DOMSerializer ds = new DOMSerializer();
	protected static Map<String,CachedFile> gCachedFiles = Collections.synchronizedMap(new LinkedHashMap<String,CachedFile>(50,.75f,true) {
		protected boolean removeEldestEntry(Map.Entry<String, CachedFile> eldest) {
			return size() > 50;
		}
	});
	
	static
	{
		DocumentBuilderFactory dbf =  DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
	    dbf.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
    }
		catch (ParserConfigurationException e) {
	    e.printStackTrace();
    }
    dbf.setIgnoringElementContentWhitespace(true);
    DocumentBuilder tempdb = null;
	  try {
	  	tempdb = dbf.newDocumentBuilder();
    }
	  catch (ParserConfigurationException e) {
	    e.printStackTrace();
    }
    db = tempdb;
	}
	public static void p(Object a)
	{
		System.out.println(a);
	}
	public static void p(InputStream is)
	{
			p(inputStreamToString(is));
	}
	public static String implode(List<String> pList) {
		if (pList.isEmpty()) {
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(pList.get(0));
		for (String s : pList) {
			sb.append(",./;'#");
			sb.append(s);
		}
		return sb.toString();
	}
	public static String[] explode(String pString) {
		if(pString == null) {
			return null;
		}
		return pString.split(",./;'#");
	}
	
	public static String hackNamespaces(String pXML)
	{
		pXML = pXML.replaceAll("xmlns:(.*?)=\"http://www.og.dti.gov/fox[0-9]?\"", "xmlns:$1=\"TO_BE_REPLACED");

		pXML = pXML.replaceFirst("xmlns:(.*?)=\"TO_BE_REPLACED", "xmlns:$1=\"http://www.og.dti.gov/fox\"");
		for(int i=2;i<100;i++)
			pXML = pXML.replaceFirst("xmlns:(.*?)=\"TO_BE_REPLACED", "xmlns:$1=\"http://www.og.dti.gov/fox"+i+"\"");

		
		pXML = pXML.replaceAll("xmlns:(.*?)=\"http://www.og.dti.gov/fox_global[0-9]?\"", "xmlns:$1=\"TO_BE_REPLACED");

		pXML = pXML.replaceFirst("xmlns:(.*?)=\"TO_BE_REPLACED", "xmlns:$1=\"http://www.og.dti.gov/fox_global\"");
		for(int i=2;i<100;i++)
			pXML = pXML.replaceFirst("xmlns:(.*?)=\"TO_BE_REPLACED", "xmlns:$1=\"http://www.og.dti.gov/fox_global"+i+"\"");
		return pXML;
	}
	
	public static String mergeNamespaces(String pXML)
	{
		pXML = pXML.replaceAll("xmlns:(.*?)=\"http://www.og.dti.gov/fox[0-9]?\"", "xmlns:$1=\"http://www.og.dti.gov/fox\"");

		pXML = pXML.replaceAll("xmlns:(.*?)=\"http://www.og.dti.gov/fox_global[0-9]?\"", "xmlns:$1=\"http://www.og.dti.gov/fox_global\"");
		return pXML;
	}
	
	public static int getLength(String[] pStrArr, int pToIndex)
	{
		int lLen = pToIndex; //line breaks
		for(int i=0;i<pToIndex;i++)
		{
			lLen+=pStrArr[i].length();
		}
		return lLen;
	}

	public static String inputStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		if (is != null)
		{
			StringBuilder sb = new StringBuilder();

			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				char[] lBuffer = new char[1024];
				int lRead;
				while ((lRead = reader.read(lBuffer)) != -1) {
					sb.append(lBuffer, 0, lRead);
				}
			}
			catch (IOException e) {
	      e.printStackTrace();
      }
			finally {
				try {
	        is.close();
        } catch (IOException e) {
	        e.printStackTrace();
        }
			}
			return sb.toString();
		}
		else {
			return "";
		}
	}
	
	public static List<CachedFile> getFilesToCheck(IContainer pRootSearch, CachedFile pCachedFile)
	{
		List<CachedFile> lFilesToCheck = new ArrayList<CachedFile>();
		try
		{
			List<IFile> lAllFiles = new ArrayList<IFile>();
			List<IResource> lTempSearch = new ArrayList<IResource>();
	//				Collections.addAll(tempSearch, getProject().members());
	    Collections.addAll(lTempSearch, pRootSearch.members());
			
	  	while(!lTempSearch.isEmpty())
	  	{
	    	IResource[] lMembers = lTempSearch.toArray(new IResource[lTempSearch.size()]);
	    	lTempSearch.clear();
	    	for(IResource lMember : lMembers)
	    	{
	    		if(lMember instanceof IFile)
	    		{
	    			lAllFiles.add((IFile)lMember);
	    		}
	    		else if(lMember instanceof IContainer)
	    		{
	    			Collections.addAll(lTempSearch, ((IContainer) lMember).members());
	    		}
	    	}
	  	}
	    List<String> lLibraryList = new ArrayList<String>(getValidValuesForDoc(pCachedFile,9).keySet());
	    for(int i=0;i<lLibraryList.size();i++)
	    {
	    	String lIncludeName = lLibraryList.get(i)+".xml";
	    	p("Trying to include: "+lIncludeName);
	    	for(IFile lFile : lAllFiles)
	    	{
		    	if(lFile!=null&&lFile.exists()&&lFile.getName().equals(lIncludeName))
		    	{
	    			CachedFile lFileToCheck = getCachedFile(lFile);
    				lFilesToCheck.add(lFileToCheck);
		    		Map<String,String> lLibrariesToAdd = getValidValuesForDoc(lFileToCheck,9);
		    		for(String lLibrary : lLibrariesToAdd.keySet())
		    		{
		    			if(!lLibraryList.contains(lLibrary))
		    			{
		    				lLibraryList.add(lLibrary);
		    			}
		    		}
		    	}
	    	}
	    }
    }
		catch (CoreException e) {
	    e.printStackTrace();
    }
    return lFilesToCheck;
	}

	static long mCacheHits = 0;
	static long mCacheMisses = 0;
	public static CachedFile getCachedFile(IFile pFile)
	{
		CachedFile lFileToCheck;
		p("There are "+gCachedFiles.size()+" cached files");
		p("Searching for cache of: "+pFile.getFullPath());
		if(gCachedFiles.containsKey(pFile.getFullPath().toString()) && gCachedFiles.get(pFile.getFullPath().toString()).mLastModified == pFile.getModificationStamp() )
		{
			p("Found cached version: "+mCacheHits++);
			lFileToCheck = gCachedFiles.get(pFile.getFullPath().toString());
		}
		else
		{
			p("Could not find a cached version: "+mCacheMisses++);
			Element lDocElem = null;
  		try {
				dal.parse(stringToInputSource(hackNamespaces(inputStreamToString(pFile.getContents(true)))));
  			lDocElem = dal.getDocument().getDocumentElement();
	  	}
  		catch (SAXException e) {
		    e.printStackTrace();
	    }
  		catch (IOException e) {
		    e.printStackTrace();
	    }
  		catch (CoreException e) {
	      e.printStackTrace();
	      throw new RuntimeException(e);
      }
	    if(gCachedFiles.containsKey(pFile.getFullPath().toString()) && lDocElem != null && lDocElem.equals(gCachedFiles.get(pFile.getFullPath().toString()).mDocElem))
	    {
	    	lFileToCheck = gCachedFiles.get(pFile.getFullPath().toString());
	    	lFileToCheck.mLastModified = pFile.getModificationStamp();
	    	p("Updated previous cached.");
	    }
	    else
	    {
				lFileToCheck = new CachedFile(pFile.getFullPath().toString(), pFile.getModificationStamp(), lDocElem);
				gCachedFiles.put(pFile.getFullPath().toString(), lFileToCheck);
				p("Created cached file");
	    }
		}
		return lFileToCheck;
	}


	public static InputSource stringToInputSource(String pString)
	{
	  return new InputSource(new StringReader(pString));
  }


	public static InputStream stringToInputStream(String pString)
	{
	  return new ByteArrayInputStream(pString.getBytes());
  }


	public final static XPathExpression[][] mValidateChecks = new XPathExpression[11][4];
	public final static String[] mValidateCheckNames = {"Action","State","Database Interface","Query","Api","Storage Location","Mapset","Template","Entry Theme","Module","Buffer"};
	
	//temp
	protected final static XPath mXPath;

	protected final static int VALIDATE_VALID_DEFINITION_LIST = 0;
	protected final static int VALIDATE_CHECK_LIST = 1;
	protected final static int VALIDATE_EXCLUDE_LIST = 2;
	protected final static int VALIDATE_VALID_VALUES_LIST = 3;
	
	static
	{
		XPathFactory xpf = XPathFactory.newInstance();
		mXPath = xpf.newXPath();
		mXPath.setNamespaceContext(new FoxNamespaceContext());
		try
		{
			mValidateChecks[0][VALIDATE_VALID_VALUES_LIST] = mXPath.compile("@name");
			for(int i=1;i<mValidateChecks.length;i++)
			{
				mValidateChecks[i][VALIDATE_VALID_VALUES_LIST] = mValidateChecks[0][VALIDATE_VALID_VALUES_LIST]; //Reuse the same compiled XPath object.
			}
			
			mValidateChecks[0][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:action");
			mValidateChecks[0][VALIDATE_CHECK_LIST] = mXPath.compile("//@action | //@callback-action | "+foxify("//@fox:change-action | //@fox:action | //@fox:upload-success-action | //@fox:upload-fail-action | //@fox:navAction"));
			mValidateChecks[0][VALIDATE_EXCLUDE_LIST] = mXPath.compile("//fm:state/@action");
			
			mValidateChecks[1][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:state[not(@action) and boolean(@name)]");
			mValidateChecks[1][VALIDATE_CHECK_LIST] = mXPath.compile("//fm:entry-theme/fm:state/text() | //@state | //fm:state/@name | //fm:state-replace/@name | //fm:state-push/@name");


			mValidateChecks[2][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:db-interface[@name]");
			mValidateChecks[2][VALIDATE_CHECK_LIST] = mXPath.compile("//@interface");
			
			mValidateChecks[3][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:query[@name]");
			mValidateChecks[3][VALIDATE_CHECK_LIST] = mXPath.compile("//@query");
			
			mValidateChecks[4][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:api[@name]");
			mValidateChecks[4][VALIDATE_CHECK_LIST] = mXPath.compile("//@api");
			
			mValidateChecks[5][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:storage-location[@name]");
			mValidateChecks[5][VALIDATE_CHECK_LIST] = mXPath.compile("//fm:map-set/fm:storage-location/text()");
			
			mValidateChecks[6][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:map-set[@name]");
			mValidateChecks[6][VALIDATE_CHECK_LIST] = mXPath.compile(foxify("//@fox:map-set"));
			
			mValidateChecks[7][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:template[@name]");
			mValidateChecks[7][VALIDATE_CHECK_LIST] = mXPath.compile("//@template");
			
			mValidateChecks[8][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:entry-theme[@name]");
			mValidateChecks[8][VALIDATE_CHECK_LIST] = mXPath.compile("//@theme");
			
			mValidateChecks[9][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:name[text()] | //fm:library[text()]");
			mValidateChecks[9][VALIDATE_CHECK_LIST] = mXPath.compile("//@module");
			mValidateChecks[9][VALIDATE_VALID_VALUES_LIST] = mXPath.compile("text()");
			
			mValidateChecks[10][VALIDATE_VALID_DEFINITION_LIST] = mXPath.compile("//fm:set-buffer[@name]");
			mValidateChecks[10][VALIDATE_CHECK_LIST] = mXPath.compile("//fm:include/@name");
    }
		catch (XPathExpressionException e1) {
	    e1.printStackTrace();
    }
	}
	
	public static Map<String,String> getValidValuesForDocs(List<CachedFile> pCachedFiles, int pValidateCheckIndex)
	{
		Map<String,String> lValidValues = new TreeMap<String,String>();
		for(CachedFile e : pCachedFiles) {
			lValidValues.putAll(getValidValuesForDoc(e, pValidateCheckIndex));
		}
		return lValidValues;
	}
	
	public static Map<String,String> getValidValuesForDoc(CachedFile pCachedFile, int pValidateCheckIndex)
	{
		if(pValidateCheckIndex>=mValidateChecks.length) {
			throw new RuntimeException("Invalid validateCheck index: "+pValidateCheckIndex);
		}
  	if(pCachedFile.mValidValues[pValidateCheckIndex] == null) {
	    pCachedFile.mValidValues[pValidateCheckIndex] = getValidValuesForDoc(pCachedFile.mDocElem, pValidateCheckIndex);
  	}
  	return pCachedFile.mValidValues[pValidateCheckIndex];
	}
	
	public static Map<String,String> getValidValuesForDoc(Element pDocElem, int pValidateCheckIndex)
	{
		if(pValidateCheckIndex>=mValidateChecks.length) {
			throw new RuntimeException("Invalid validateCheck index: "+pValidateCheckIndex);
		}
    try
    {
	    Map<String,String> lValidValues = new TreeMap<String,String>();
    	NodeList lNodeList = (NodeList) mValidateChecks[pValidateCheckIndex][VALIDATE_VALID_DEFINITION_LIST].evaluate(pDocElem,XPathConstants.NODESET);
	    for(int i=0;i<lNodeList.getLength();i++)
	    {
	    	Node lNode = lNodeList.item(i);
	    	DOMSerializer ds = new DOMSerializer();
//	    	p(ds.serializeNode(n));
	    	lNode = ((Node)mValidateChecks[pValidateCheckIndex][VALIDATE_VALID_VALUES_LIST].evaluate(lNode,XPathConstants.NODE));
	    	String lKey = lNode.getNodeValue();
	    	String lValue = ds.serializeNode(lNodeList.item(i));
	    	lValidValues.put(lKey,lValue);
//	    	p(nodeList.item(i).getNodeValue());
	    }
	    return lValidValues;
    }
    catch (XPathExpressionException e) {
			throw new RuntimeException("Invalid xpath expression for validate check: "+pValidateCheckIndex,e);
    }
	}
	
	public static NodeList getCheckNodeListForDoc(CachedFile pCachedFile, int pValidateCheckIndex)
	{
		if(pValidateCheckIndex>=mValidateChecks.length) {
			throw new RuntimeException("Invalid validateCheck index: "+pValidateCheckIndex);
		}
    if(pCachedFile.mCheckNodeList[pValidateCheckIndex] == null) {
    	pCachedFile.mCheckNodeList[pValidateCheckIndex] = getCheckNodeListForDoc(pCachedFile.mDocElem,pValidateCheckIndex);
    }
    return pCachedFile.mCheckNodeList[pValidateCheckIndex];
	}
	
	public static NodeList getCheckNodeListForDoc(Element pDocElem, int pValidateCheckIndex)
	{
		if(pValidateCheckIndex >= mValidateChecks.length) {
			throw new RuntimeException("Invalid validateCheck index: "+pValidateCheckIndex);
		}
    try
    {
    	NodeSet lCheckList = new NodeSet((NodeList) mValidateChecks[pValidateCheckIndex][VALIDATE_CHECK_LIST].evaluate(pDocElem,XPathConstants.NODESET));

      if(mValidateChecks[pValidateCheckIndex][VALIDATE_EXCLUDE_LIST]!=null)
      {
      	NodeList pExcludeList = (NodeList) mValidateChecks[pValidateCheckIndex][2].evaluate(pDocElem,XPathConstants.NODESET);
      	for(int i=0;i<pExcludeList.getLength();i++) {
      		lCheckList.removeNode(pExcludeList.item(i));
      	}
      }
	    return lCheckList;
    }
    catch (XPathExpressionException e) {
			throw new RuntimeException("Invalid xpath expression for validate check: "+pValidateCheckIndex,e);
    }
	}
	
	private static String foxify(String pString)
	{
		StringBuffer lFoxifiedString = new StringBuffer(pString);
		lFoxifiedString.append("|");
		lFoxifiedString.append(pString.replaceAll("fox:", "foxg:"));
		for(int i=2;i<100;i++)
		{
			lFoxifiedString.append("|");
			lFoxifiedString.append(pString.replaceAll("fox:", "fox"+i+":"));
		}
		return lFoxifiedString.toString();
	}
	
	public static boolean nodeListContains(NodeList pHaystack, Node pNeedle)
	{
		if(pHaystack!=null)
	    for(int i=0;i<pHaystack.getLength();i++)
			{
				if(pHaystack.item(i).isSameNode(pNeedle)) {
					return true;
				}
			}
		return false;
	}
	
	public static String[][] getValidOptions(IDOMNode pNode, int rOffset, int rLength, String pAttributeName, String pMatchString, ITextRegion pRegion)
	{
		List<String[]> lValidOptions = new ArrayList<String[]>();
		Document lDoc = pNode.getOwnerDocument();
		Element lDocElem = lDoc.getDocumentElement();
			
		Node lAttrNode = null;
		if(pNode.getNodeType()==Node.TEXT_NODE || pAttributeName == null) {
			lAttrNode = pNode;
		}
		else {
			lAttrNode = pNode.getAttributes().getNamedItem(pAttributeName);
		}

    for(int i=0;i<mValidateChecks.length;i++)
    {
			NodeList lCheckNodeList = getCheckNodeListForDoc(lDocElem, i);
			if(nodeListContains(lCheckNodeList, lAttrNode))
			{
				//Getting the currently cached file and list of libraries will be redone each time the auto-complete node is in the check node list, however that is likely to only be only 0 or 1 times so it's probably best to keep in rather than move outside the loop
				IWorkbench lWorkbench = PlatformUI.getWorkbench();
				IWorkbenchWindow lWorkbenchWindow = lWorkbench.getActiveWorkbenchWindow();
				IWorkbenchPage lWorkbenchPage = lWorkbenchWindow.getActivePage();
				IEditorPart lEditorPart = lWorkbenchPage.getActiveEditor();
				IFile lCurrentFile = (IFile) lEditorPart.getAdapter(IResource.class);

				CachedFile lCachedFile = getCachedFile(lCurrentFile);
				List<CachedFile> lFilesToCheck = getFilesToCheck(lCurrentFile.getParent().getParent(), lCachedFile);
				
				Map<String,String> lPossibleValues = getValidValuesForDocs(lFilesToCheck, i);
  			
				if (lPossibleValues.size() > 0)
				{
					boolean lExistingComplicatedValue = (pRegion != null) && (pRegion instanceof ITextRegionContainer);
					if (!lExistingComplicatedValue)
					{
						for (Map.Entry<String,String> lPossibleValueEntry : lPossibleValues.entrySet())
						{
							String lPossibleValue = lPossibleValueEntry.getKey();
							if ((pMatchString.length() == 0) || lPossibleValue.startsWith(pMatchString))
							{
								String lReplaceString = "\"" + lPossibleValue + "\"";
								String lInfo = "<pre>"+(lPossibleValueEntry.getValue().replaceAll("&", "&amp;").replaceAll("<", "&lt;"))+"</pre>";
								lValidOptions.add(new String[]{lReplaceString,lInfo});
							}
						}
					}
				}
			}
    }
		return (String[][])lValidOptions.toArray(new String[][]{});
	}
}


class FoxNamespaceContext implements NamespaceContext
{
  public String getNamespaceURI(String pPrefix)
  {
    if (pPrefix.equals("fm")) {
      return "http://www.og.dti.gov/fox_module";
    }
    if (pPrefix.startsWith("fox"))
    {
  		if(pPrefix.equals("foxg")) {
  			return "http://www.og.dti.gov/fox_global";
  		}
  		if(pPrefix.equals("fox")) {
  			return "http://www.og.dti.gov/fox";
  		}
    	for(int i=2;i<100;i++)
    	{
    		if(pPrefix.equals("fox"+i)) {
    			return "http://www.og.dti.gov/fox"+i;
    		}
    		if(pPrefix.equals("fox_global"+i)) {
    			return "http://www.og.dti.gov/fox"+i;
    		}
    	}
    }
    return XMLConstants.NULL_NS_URI;
  }
    
  //Not needed for xpath
  public String getPrefix(String pNamespace)
  {
    return null;
  }

  //Not needed for xpath
  public Iterator getPrefixes(String pNamespace)
  {
    return null;
  }
}