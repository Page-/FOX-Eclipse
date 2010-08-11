package foxvalidator;

import static foxvalidator.X.*;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class HackNamespaces implements IObjectActionDelegate {

	private ISelection mSelection;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction pAction)
	{
		if (mSelection instanceof IStructuredSelection)
		{
			for (Iterator lIterator = ((IStructuredSelection) mSelection).iterator(); lIterator.hasNext();)
			{
				Object lElement = lIterator.next();
				if (lElement instanceof IFile) {
					this.hack((IFile) lElement);
				}
			}
		}
	}
	
	private void hack(IFile pFile)
	{
		String lFileContents;
    try
    {
	    lFileContents = inputStreamToString(pFile.getContents());
			if(lFileContents.contains("http://www.og.dti.gov/fox2\"") || lFileContents.contains("http://www.og.dti.gov/fox_global2\"")) {
				lFileContents = mergeNamespaces(lFileContents);
			}
			else {
				lFileContents = hackNamespaces(lFileContents);
			}
      pFile.setContents(stringToInputStream(lFileContents), IFile.KEEP_HISTORY, null);
    }
    catch (CoreException e) {
      e.printStackTrace();
    }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction pAction, ISelection pSelection)
	{
		this.mSelection = pSelection;
	}

	@Override
  public void setActivePart(IAction pAction, IWorkbenchPart pTargetPart)
	{
	  // TODO Auto-generated method stub
  }
}