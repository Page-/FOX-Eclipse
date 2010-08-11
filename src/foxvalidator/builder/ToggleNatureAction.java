package foxvalidator.builder;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ToggleNatureAction implements IObjectActionDelegate {

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
			for (Iterator it = ((IStructuredSelection) mSelection).iterator(); it.hasNext();)
			{
				Object lElement = it.next();
				IProject lProject = null;
				if (lElement instanceof IProject) {
					lProject = (IProject) lElement;
				}
				else if (lElement instanceof IAdaptable) {
					lProject = (IProject) ((IAdaptable) lElement).getAdapter(IProject.class);
				}
				if (lProject != null) {
					toggleNature(lProject);
				}
			}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction pAction, IWorkbenchPart pTargetPart)
	{
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param pProject
	 *            to have sample nature added or removed
	 */
	private void toggleNature(IProject pProject)
	{
		try
		{
			IProjectDescription lDescription = pProject.getDescription();
			String[] lNatures = lDescription.getNatureIds();

			for (int i = 0; i < lNatures.length; ++i)
			{
				if (FoxNature.NATURE_ID.equals(lNatures[i]))
				{
					// Remove the nature
					String[] lNewNatures = new String[lNatures.length - 1];
					System.arraycopy(lNatures, 0, lNewNatures, 0, i);
					System.arraycopy(lNatures, i + 1, lNewNatures, i, lNatures.length - i - 1);
					lDescription.setNatureIds(lNewNatures);
					pProject.setDescription(lDescription, null);
					return;
				}
			}

			// Add the nature
			String[] lNewNatures = new String[lNatures.length + 1];
			System.arraycopy(lNatures, 0, lNewNatures, 0, lNatures.length);
			lNewNatures[lNatures.length] = FoxNature.NATURE_ID;
			lDescription.setNatureIds(lNewNatures);
			pProject.setDescription(lDescription, null);
		}
		catch (CoreException e) {
		}
	}

}
