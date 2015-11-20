package simplypay;

@SuppressWarnings("serial")
public class SimplyPayException extends Exception {
	
	public SimplyPayException(String str) {
		super(str);
	}
	
	public SimplyPayException(Throwable e) {
		super(e);
	}
}
