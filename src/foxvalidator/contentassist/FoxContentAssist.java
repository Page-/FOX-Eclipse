package foxvalidator.contentassist;

import static foxvalidator.X.*;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLRelevanceConstants;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLTemplatesCompletionProposalComputer;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;

@SuppressWarnings("restriction")
public class FoxContentAssist extends XMLTemplatesCompletionProposalComputer {
	
	private static final Image proposalIcon = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ENUM);

	private void addFoxProposals(ContentAssistRequest pContentAssistRequest, String pAttributeName, String pMatchString)
	{
		IDOMNode lNode = (IDOMNode) pContentAssistRequest.getNode();
		int lReplaceOffset = pContentAssistRequest.getReplacementBeginPosition();
		int lReplaceLength = pContentAssistRequest.getReplacementLength();
    String[][] lValidOptions = getValidOptions(lNode,lReplaceOffset,lReplaceLength,pAttributeName,pMatchString,pContentAssistRequest.getRegion());
    
    for(String[] lValidOption : lValidOptions) {
			pContentAssistRequest.addProposal(new CustomCompletionProposal(lValidOption[0], lReplaceOffset, lReplaceLength, lValidOption[0].length() + 1, proposalIcon, lValidOption[0], null, lValidOption[1], XMLRelevanceConstants.R_XML_ATTRIBUTE_VALUE));
    }
    p("Proposals count: "+pContentAssistRequest.getProposals().size());
	}
	
	@Override
	protected void addAttributeValueProposals(ContentAssistRequest pContentAssistRequest, CompletionProposalInvocationContext pContext)
	{
		IDOMNode lNode = (IDOMNode) pContentAssistRequest.getNode();
		// Find the attribute region and name for which this position should
		// have a value proposed
		IStructuredDocumentRegion lOpen = lNode.getFirstStructuredDocumentRegion();
		ITextRegionList lOpenRegions = lOpen.getRegions();
		int i = lOpenRegions.indexOf(pContentAssistRequest.getRegion());
		if (i < 0) {
			return;
		}
		ITextRegion lNameRegion = null;
		while (i >= 0)
		{
			lNameRegion = lOpenRegions.get(i--);
			if (lNameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				break;
			}
		}

		// the name region is REQUIRED to do anything useful
		if (lNameRegion != null)
		{
			String pAttributeName = lOpen.getText(lNameRegion);
			
			String lMatchString = pContentAssistRequest.getMatchString();
			if (lMatchString == null) {
				lMatchString = "";
			}
			if ((lMatchString.length() > 0) && (lMatchString.startsWith("\"") || lMatchString.startsWith("'"))) {
				lMatchString = lMatchString.substring(1);
			}
			
			addFoxProposals(pContentAssistRequest, pAttributeName, lMatchString);
		}
	}
	

	@Override
	protected void addEntityProposals(ContentAssistRequest pContentAssistRequest, ITextRegion pCompletionRegion, IDOMNode pTreeNode, CompletionProposalInvocationContext pContext)
	{
		addFoxProposals(pContentAssistRequest, null, null);
	}
	@Override
	protected void addEmptyDocumentProposals(ContentAssistRequest pContentAssistRequest, CompletionProposalInvocationContext pContext)
	{
	}
	@Override
	protected void addTagInsertionProposals(ContentAssistRequest pContentAssistRequest, int childPosition, CompletionProposalInvocationContext pContext)
	{
	}
//	@Override
//	public ICompletionProposal[] computeCompletionProposals(ITextViewer textViewer, int documentPosition) {
//
//		fTextViewer = textViewer;
//
//		IndexedRegion treeNode = ContentAssistUtils.getNodeAt(textViewer, documentPosition);
//
//		Node node = (Node) treeNode;
//
//		ContentAssistRequest contentAssistRequest = null;
//
//		IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
//		ITextRegion completionRegion = getCompletionRegion(documentPosition, node);
//
//		contentAssistRequest = newContentAssistRequest((Node) treeNode, node.getParentNode(), sdRegion, completionRegion, documentPosition, node.getNodeValue().length(), node.getNodeValue()); //$NON-NLS-1$
//
//		addAttributeValueProposals(contentAssistRequest);
//
//		return contentAssistRequest.getCompletionProposals();
//	}
}
