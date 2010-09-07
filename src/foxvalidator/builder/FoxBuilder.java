package foxvalidator.builder;

import static foxvalidator.X.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import foxvalidator.CachedFile;
import foxvalidator.X;

public class FoxBuilder extends IncrementalProjectBuilder
{
	static
	{
		p("Validator");
	}
  
  public static char matchingBracket(char bracket) {
    if (bracket == '(')
      return ')';
    if (bracket == '[')
      return ']';
    if (bracket == '{')
      return '}';
    if (bracket == ')')
      return '(';
    if (bracket == ']')
      return '[';
    if (bracket == '}')
      return '{';
    throw new RuntimeException(bracket+" is not an expected bracket type.");
  }

	class FoxVisitor implements IResourceDeltaVisitor, IResourceVisitor
	{
		IProgressMonitor mMonitor;
		public FoxVisitor(IResourceDelta pResourceDelta, IProgressMonitor pMonitor)
		{
			mMonitor = pMonitor;
			mMonitor.beginTask("FOX Building", pResourceDelta.getAffectedChildren().length);
		}
		public FoxVisitor(IProject pResource, IProgressMonitor pMonitor)
		{
			mMonitor = pMonitor;
			try
			{
				int lTotalWork = 0;
				List<IResource> lMembers = new ArrayList<IResource>(Arrays.asList(pResource.members()));
				while(!lMembers.isEmpty())
				{
					IResource lMember = lMembers.remove(0);
					if(lMember instanceof IContainer) {
						lMembers.addAll(Arrays.asList(((IContainer)lMember).members()));
					}
					else if(lMember instanceof IFile) {
						lTotalWork++;
					}
				}
	      mMonitor.beginTask("FOX Building", lTotalWork);
      } catch (CoreException e) {
	      e.printStackTrace();
      }
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta pDelta) throws CoreException
		{
			if(mMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			IResource lResource = pDelta.getResource();
			mMonitor.subTask(lResource.getFullPath().toString());
			switch (pDelta.getKind())
			{
				case IResourceDelta.ADDED:
					// handle added resource
					checkXML(lResource);
				break;
				case IResourceDelta.REMOVED:
					// handle removed resource
				break;
				case IResourceDelta.CHANGED:
					// handle changed resource
					checkXML(lResource);
				break;
			}
			mMonitor.worked(1);
			//return true to continue visiting children.
			return true;
		}
		
		public boolean visit(IResource pResource)
		{
			if(mMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			mMonitor.subTask(pResource.getFullPath().toString());
			checkXML(pResource);
			mMonitor.worked(1);
			//return true to continue visiting children.
			return true;
		}

		void checkXML(IResource pResource)
		{
	    p("Starting validation of "+pResource.getName()+".");
			
			if (pResource instanceof IFile && pResource.getName().endsWith(".xml"))
			{
				IFile lFile = (IFile) pResource;
				deleteMarkers(lFile);
				XMLErrorHandler reporter = new XMLErrorHandler(lFile);
				try
				{
					CachedFile lCachedFile = getCachedFile(lFile);
					
					List<CachedFile> lFilesToCheck = getFilesToCheck(lFile.getParent().getParent(), lCachedFile);
					
		      for(int x=0;x<mValidateChecks.length;x++)
		      {
		  			if(mMonitor.isCanceled()) {
		  				throw new OperationCanceledException();
		  			}
			      List<String> lValidValues = new ArrayList<String>(getValidValuesForDocs(lFilesToCheck,x).keySet());

			      NodeList lNodeList = getCheckNodeListForDoc(lCachedFile,x);
			      for(int i=0;i<lNodeList.getLength();i++)
			      {
			      	Node lNode = lNodeList.item(i);
			      	if(!lValidValues.contains(lNode.getTextContent()))
			      	{
			      		p("Invalid "+mValidateCheckNames[x]+": "+lNode.getTextContent());
//			      		StringWriter sw = new StringWriter();
//			      		d.serializeNode(nodeList.item(i), sw, "");
//			      		p(sw.toString());
			      		
			          /*START COPYPASTA1*/ //TODO: This is also done in HyperLink stuff, can we consolidate?
			      		String lStartLine = (String)lNode.getUserData("startLine");
			      		if(lStartLine==null) {
			      			lStartLine = (String)lNode.getParentNode().getUserData("startLine");
			      		}
	            	int lLineNumber = (lStartLine==null?-1:Integer.parseInt(lStartLine));
			      		int lCharStart = (lLineNumber==-1?0:getLength(lCachedFile.mLines,lLineNumber-1)+lCachedFile.mLines[lLineNumber-1].indexOf(lNode.getTextContent()));
			      		int lCharEnd = (lLineNumber==-1?0:lCharStart + lNode.getTextContent().length());
			      		/*END COPYPASTA1*/
			      		p(lLineNumber);
			      		addMarker(lFile, mValidateCheckNames[x]+" '"+lNode.getTextContent()+"' not found.", lLineNumber, IMarker.SEVERITY_WARNING, lCharStart, lCharEnd, lValidValues);
			      	}
//			      	p(nodeList.item(i).getTextContent());
			      }
		      }

		      NodeList lNodeList = (NodeList)X.mAllAttrsXPath.evaluate(lCachedFile.mDocElem,XPathConstants.NODESET);
		      for(int i=0;i<lNodeList.getLength();i++)
		      {
		      	Node lNode = lNodeList.item(i);
		      	
		      	String lText = lNode.getTextContent();

	          /*START COPYPASTA1*/
	      		String lStartLine = (String)lNode.getUserData("startLine");
	      		if(lStartLine==null) {
	      			lStartLine = (String)lNode.getParentNode().getUserData("startLine");
	      		}
          	int lLineNumber = (lStartLine==null?-1:Integer.parseInt(lStartLine));
	      		int lCharStart = (lLineNumber==-1?0:getLength(lCachedFile.mLines,lLineNumber-1)+lCachedFile.mLines[lLineNumber-1].indexOf(lText));
	      		int lCharEnd = (lLineNumber==-1?0:lCharStart + lText.length());
	      		/*END COPYPASTA1*/
	      		
		        Stack<String> lStack = new Stack<String>();
		        for (int j = 0; j < lText.length(); j += 1) {
		          char lChar = lText.charAt(j);
		          if (lChar == '(' || lChar == '[' || lChar == '{') {
		            lStack.push("" + lChar + j); // push a String containing the char and the offset
		          }
		          else if (lChar == ')' || lChar == ']' || lChar == '}') {
		          	if(lStack.empty()) {
		          		addMarker(lFile, "Found closing '"+lChar+"' with no opening '"+matchingBracket(lChar)+"'.", lLineNumber, IMarker.SEVERITY_WARNING, lCharStart, lCharEnd, null);
				      		continue;
		          	}
		          	else {
			            String peek = lStack.peek();
			            if (matchingBracket(peek.charAt(0))==lChar) { // does it match?
			              lStack.pop();
			            } else { // mismatch
					      		addMarker(lFile, "Unexpected  '"+lChar+"', expected '"+matchingBracket(peek.charAt(0))+"'.", lLineNumber, IMarker.SEVERITY_WARNING, lCharStart, lCharEnd, null);
			            }
		          	}
		          }
		        }
		        while(!lStack.empty()) { // anything left in the stack is a mismatch
	            String lPop = lStack.pop();
		      		addMarker(lFile, "Found opening '"+lPop.charAt(0)+"' but no matching '"+matchingBracket(lPop.charAt(0))+"'.", lLineNumber, IMarker.SEVERITY_WARNING, lCharStart, lCharEnd, null);
		        }
		        
		      }
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	class XMLErrorHandler extends DefaultHandler
	{
		private IFile mFile;

		public XMLErrorHandler(IFile pFile)
		{
			this.mFile = pFile;
		}

		private void addMarker(SAXParseException pException, int pSeverity)
		{
			FoxBuilder.this.addMarker(mFile, pException.getMessage(), pException.getLineNumber(), pSeverity, 0,0, null);
		}

		public void error(SAXParseException pException) throws SAXException
		{
			addMarker(pException, IMarker.SEVERITY_ERROR);
		}

		public void fatalError(SAXParseException pException) throws SAXException
		{
			addMarker(pException, IMarker.SEVERITY_ERROR);
		}

		public void warning(SAXParseException pException) throws SAXException
		{
			addMarker(pException, IMarker.SEVERITY_WARNING);
		}
	}

	public static final String BUILDER_ID = "FoxValidator.foxBuilder";

	private static final String MARKER_TYPE = "FoxValidator.xmlProblem";
	
	private SAXParserFactory mParserFactory;

	public static final String FOX_RESOLUTIONS = "FOX_RESOLUTIONS";
	private void addMarker(IFile pFile, String pMessage, int pLineNumber, int pSeverity, int pCharStart, int pCharEnd, List<String> pResolutions)
	{
		try
		{
			IMarker lMarker = pFile.createMarker(MARKER_TYPE);
			lMarker.setAttribute(IMarker.MESSAGE, pMessage);
			lMarker.setAttribute(IMarker.SEVERITY, pSeverity);
			if (pLineNumber == -1)
			{
				pLineNumber = 1;
			}
			lMarker.setAttribute(IMarker.LINE_NUMBER, pLineNumber);
			if(pCharStart !=0 ) {
				lMarker.setAttribute(IMarker.CHAR_START, pCharStart);
			}
			if(pCharEnd != 0) {
				lMarker.setAttribute(IMarker.CHAR_END, pCharEnd);
			}
			if(pResolutions != null) {
				lMarker.setAttribute(FOX_RESOLUTIONS, implode(pResolutions));
			}
		}
		catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int pKind, Map pArgs, IProgressMonitor pMonitor) throws CoreException
	{
		try
		{
			if (pKind == FULL_BUILD) {
				fullBuild(pMonitor);
			}
			else
			{
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(pMonitor);
				}
				else {
					incrementalBuild(delta, pMonitor);
				}
			}
		}
		catch(OperationCanceledException e)
		{
			forgetLastBuiltState();
			throw e;
		}
		return new IProject[]{getProject()};
	}

	private void deleteMarkers(IFile file)
	{
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		}
		catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor pMonitor) throws CoreException
	{
		try {
			getProject().accept(new FoxVisitor(getProject(),pMonitor));
		}
		catch (CoreException e) {
		}
	}

	private SAXParser getParser() throws ParserConfigurationException, SAXException
	{
		if (mParserFactory == null) {
			mParserFactory = SAXParserFactory.newInstance();
		}
		return mParserFactory.newSAXParser();
	}

	protected void incrementalBuild(IResourceDelta pDelta, IProgressMonitor pMonitor) throws CoreException
	{
		// the visitor does the work.
		pDelta.accept(new FoxVisitor(pDelta,pMonitor));
	}
}
