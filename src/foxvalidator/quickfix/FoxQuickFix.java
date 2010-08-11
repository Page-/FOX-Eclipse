package foxvalidator.quickfix;

import static foxvalidator.X.explode;
import static foxvalidator.X.p;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import foxvalidator.builder.FoxBuilder;

public class FoxQuickFix implements IMarkerResolutionGenerator {
	
	static{
		p("FoxQuickFix");
	}

	@Override
  public IMarkerResolution[] getResolutions(IMarker pMarker)
	{
	  try
	  {
			p("Getting resolutions");
	    String[] pResolutions = explode((String)pMarker.getAttribute(FoxBuilder.FOX_RESOLUTIONS));
	    if(pResolutions==null || pResolutions.length==0) {
	    	return new IMarkerResolution[0];
	    }
	    IMarkerResolution[] lRes = new IMarkerResolution[pResolutions.length];
	    for(int i=0;i<pResolutions.length;i++) {
	    	lRes[i] = new FoxResolution(pResolutions[i]);
	    }
	    return lRes;
    }
	  catch (CoreException e) {
	    e.printStackTrace();
    }
	  return new IMarkerResolution[0];
  }

}

class FoxResolution implements IMarkerResolution
{
	private String mReplacementText;
	
	public FoxResolution(String pReplacementText)
	{
		this.mReplacementText = pReplacementText;
	}
	
	@Override
  public String getLabel()
	{
	  return "Switch to "+mReplacementText;
  }

	@Override
  public void run(IMarker pMarker)
	{
//	  marker.getAttribute(IMarker.CHAR_START);
//	  IFile x = (IFile)pMarker.getResource();
	  //TODO: Something?
  }
	
}
