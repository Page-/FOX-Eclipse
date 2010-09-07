/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package foxvalidator.abstracthyperlinkdetector;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;

/**
 * Hyperlink for files within the workspace. (As long as there is an IFile,
 * this can be used) Opens the default editor for the file.
 */
class WorkspaceFileHyperlink implements IHyperlink {
	// copies of this class exist in:
	// org.eclipse.wst.xml.ui.internal.hyperlink
	// org.eclipse.wst.html.ui.internal.hyperlink
	// org.eclipse.jst.jsp.ui.internal.hyperlink

	private IRegion fRegion;
	private IFile fFile;
	private IRegion fHighlightRange;

	public WorkspaceFileHyperlink(IRegion region, IFile file) {
		fRegion = region;
		fFile = file;
	}

	public WorkspaceFileHyperlink(IRegion region, IFile file, IRegion range) {
		fRegion = region;
		fFile = file;
		fHighlightRange = range;
	}

	public IRegion getHyperlinkRegion() {
		return fRegion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
	 */
	public String getHyperlinkText() {
		String path = fFile.getFullPath().toString();
		if (path.length() > 60) {
			path = path.substring(0, 25) + "..." + path.substring(path.length() - 25, path.length());
		}
		return "Open in "+path;
	}

	public void open() {
		if ((fFile != null) && fFile.exists()) {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart editor = IDE.openEditor(page, fFile, true);
				// highlight range in editor if possible
				if (fHighlightRange != null) {
					if(editor instanceof ITextEditor) {
						((ITextEditor) editor).setHighlightRange(fHighlightRange.getOffset(), fHighlightRange.getLength(), true);
					}
					else if(editor instanceof MultiPageEditorPart) {
						Object lSelectedPage = ((MultiPageEditorPart)editor).getSelectedPage();
						if(lSelectedPage instanceof ITextEditor) {
							((ITextEditor)lSelectedPage).setHighlightRange(fHighlightRange.getOffset(), fHighlightRange.getLength(), true);
						}
					}
				}
			}
			catch (PartInitException pie) {
				pie.printStackTrace();
			}
		}
	}

}
