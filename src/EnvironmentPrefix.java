
public enum EnvironmentPrefix {
	LOCAL ("www.local"),
	QA ("www.qa"),
	TEST ("www.test"),
	STAGING ("www.staging"),
	PRODUCTION("www");
	
	private final String _envName;
	
	private EnvironmentPrefix(String envName){
		_envName = envName;
	}
	
	public String toString(){
		return _envName;
	}
}
