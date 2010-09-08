package foxvalidator.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;



public class XMLErrorHandler extends DefaultHandler
{
	private IFile mFile;

	public XMLErrorHandler(IFile pFile)
	{
		this.mFile = pFile;
	}

	private void addMarker(SAXParseException pException, int pSeverity)
	{
		FoxBuilder.addMarker(mFile, pException.getMessage(), pException.getLineNumber(), pSeverity, 0,0, null);
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