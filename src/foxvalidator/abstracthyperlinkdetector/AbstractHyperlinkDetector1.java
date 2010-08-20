package foxvalidator.abstracthyperlinkdetector;

import static foxvalidator.X.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.internal.text.rules.SimpleStructuredRegion;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import foxvalidator.CachedFile;

public class AbstractHyperlinkDetector1 extends AbstractHyperlinkDetector {
	
	static {
		p("Hyperlink Detector");
	}

	public AbstractHyperlinkDetector1() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("restriction")
  @Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion pRegion, boolean canShowMultipleHyperlinks) {
		p("detectHyperlinks");
		IWorkbench lWorkbench = PlatformUI.getWorkbench();
		IWorkbenchWindow lWorkbenchWindow = lWorkbench.getActiveWorkbenchWindow();
		IWorkbenchPage lWorkbenchPage = lWorkbenchWindow.getActivePage();
		IEditorPart lEditorPart = lWorkbenchPage.getActiveEditor();
		IFile lCurrentFile = (IFile) lEditorPart.getAdapter(IResource.class);
		
		CachedFile lCachedFile = getCachedFile(lCurrentFile);
		Node lNode = getNodeAtPosition(lCachedFile.mDocElem, pRegion.getOffset());

		return new IHyperlink[]{new WorkspaceFileHyperlink(new SimpleStructuredRegion(0, lNode.getNodeValue().length()), lCurrentFile)};
	}

//	@Override
//	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
//		// for now, only capable of creating 1 hyperlink
//		List hyperlinks = new ArrayList(0);
//
//		if ((region != null) && (textViewer != null)) {
//			IDocument document = textViewer.getDocument();
//			Node currentNode = getCurrentNode(document, region.getOffset());
//			if (currentNode != null) {
//				String uriString = null;
//				if (currentNode.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
//					// doctype nodes
//					uriString = getURIString(currentNode, document);
//				}
//				else if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
//					// element nodes
//					Attr currentAttr = getCurrentAttrNode(currentNode, region.getOffset());
//					if (currentAttr != null) {
//						// try to find link for current attribute
//						// resolve attribute value
//						uriString = getURIString(currentAttr, document);
//						// verify validity of uri string
//						if ((uriString == null) || !isValidURI(uriString)) {
//							// reset current attribute
//							currentAttr = null;
//						}
//					}
//					if (currentAttr == null) {
//						// try to find a linkable attribute within element
//						currentAttr = getLinkableAttr((Element) currentNode);
//						if (currentAttr != null) {
//							uriString = getURIString(currentAttr, document);
//						}
//					}
//					currentNode = currentAttr;
//				}
//				// try to create hyperlink from information gathered
//				if ((uriString != null) && (currentNode != null) && isValidURI(uriString)) {
//					IRegion hyperlinkRegion = getHyperlinkRegion(currentNode);
//					IHyperlink hyperlink = createHyperlink(uriString, hyperlinkRegion, document, currentNode);
//					if (hyperlink != null) {
//						hyperlinks.add(hyperlink);
//					}
//				}
//			}
//		}
//		if (hyperlinks.size() == 0) {
//			return null;
//		}
//		return (IHyperlink[]) hyperlinks.toArray(new IHyperlink[0]);
//	}

}
