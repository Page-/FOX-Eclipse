package foxvalidator.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class FoxNature implements IProjectNature
{
	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = "FoxValidator.foxNature";

	private IProject mProject;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException
	{
		IProjectDescription lDesc = mProject.getDescription();
		ICommand[] lCommands = lDesc.getBuildSpec();

		for (int i = 0; i < lCommands.length; ++i)
		{
			if (lCommands[i].getBuilderName().equals(FoxBuilder.BUILDER_ID)) {
				return;
			}
		}

		ICommand[] lNewCommands = new ICommand[lCommands.length + 1];
		System.arraycopy(lCommands, 0, lNewCommands, 0, lCommands.length);
		ICommand command = lDesc.newCommand();
		command.setBuilderName(FoxBuilder.BUILDER_ID);
		lNewCommands[lNewCommands.length - 1] = command;
		lDesc.setBuildSpec(lNewCommands);
		mProject.setDescription(lDesc, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException
	{
		IProjectDescription description = getProject().getDescription();
		ICommand[] lCommands = description.getBuildSpec();
		for (int i = 0; i < lCommands.length; ++i)
		{
			if (lCommands[i].getBuilderName().equals(FoxBuilder.BUILDER_ID))
			{
				ICommand[] lNewCommands = new ICommand[lCommands.length - 1];
				System.arraycopy(lCommands, 0, lNewCommands, 0, i);
				System.arraycopy(lCommands, i + 1, lNewCommands, i, lCommands.length - i - 1);
				description.setBuildSpec(lNewCommands);
				mProject.setDescription(description, null);			
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject()
	{
		return mProject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject pProject)
	{
		this.mProject = pProject;
	}

}
