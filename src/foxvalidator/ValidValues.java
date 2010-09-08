package foxvalidator;

import org.w3c.dom.Node;

public class ValidValues {
	public final Node mNode;
	public final String mMessage;
	public ValidValues(Node pNode, String pMessage) {
    this.mNode = pNode;
    this.mMessage = pMessage;
  }
	
}